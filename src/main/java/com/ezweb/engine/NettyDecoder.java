package com.ezweb.engine;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.ReferenceCountUtil;

import java.nio.ByteBuffer;

/**
 * @author : zuodp
 * @version : 1.10
 */
public class NettyDecoder extends LengthFieldBasedFrameDecoder {

	public NettyDecoder() {
		super(Integer.MAX_VALUE, 0, NettyEncoder.FRAME_LEN, 0, NettyEncoder.FRAME_LEN);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		ByteBuf frame = (ByteBuf) super.decode(ctx, in);
		if (null == frame) {
			return null;
		}
		// 读取头信息.
		int magic = frame.readUnsignedShort();
		if (magic != CustTMessage.MAGIC) {
			frame.discardReadBytes();
			frame.release();
			throw new Exception("协议Magic无效!");
		}
		byte ver_type = frame.readByte();

		byte ver = (byte) ((ver_type & 0b11110000) >>> 4);
		byte type = (byte) (ver_type & 0b00001111);

		byte protoType = frame.readByte();

		if (ver == CustTVersion.VERSION_1) {
			int seqId = frame.readInt();
			int len = frame.readInt();
			if (len > CustTMessage.BODY_MAX_LEN) {
				frame.discardReadBytes();
				frame.release();
				throw new Exception("body.len 无效!");
			}
			// 读取crc32.
			long crc32 = frame.readUnsignedInt();
			// 读取余下的body.
			ByteBuffer body = ByteBuffer.allocate(len);
			frame.readBytes(body);
			body.flip();      // [0, position]

			long crc32_v = NettyEncoder.calcCrc32(ver, type, protoType, seqId, len, body);
			if (crc32 != crc32_v) {
				frame.discardReadBytes();
				frame.release();
				throw new Exception("crc32 失败");
			}

			CustTMessage msg = new CustTMessage();
			msg.setVersion(ver);
			msg.setType(type);
			msg.setProtoType(protoType);
			msg.setSeqId(seqId);
			msg.setLen(len);
			msg.setBody(body);

			try {
				return msg;
			} finally {
				frame.release(); // 放掉缓存.
			}
		} else {
			frame.discardReadBytes();
			ReferenceCountUtil.release(frame);
			throw new Exception("协议Version无效!");
		}
	}
}
