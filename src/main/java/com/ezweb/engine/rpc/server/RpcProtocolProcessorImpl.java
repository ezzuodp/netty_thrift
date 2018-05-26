package com.ezweb.engine.rpc.server;

import com.ezweb.engine.CustTMessage;
import com.ezweb.engine.exception.TBizException;
import com.ezweb.engine.rpc.*;
import com.google.common.collect.Maps;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentMap;

/**
 * @author : zuodp
 * @version : 1.10
 */
public class RpcProtocolProcessorImpl implements RpcProtocolProcessor {
	private ConcurrentMap<Byte, RpcProtocolCode> rpcProtocols = Maps.newConcurrentMap();
	private RpcHandler rpcHandler = null;

	public RpcProtocolProcessorImpl() {
	}

	@Override
	public void addRpcProtocol(Byte codeType, RpcProtocolCode rpcProtocol) {
		rpcProtocols.put(codeType, rpcProtocol);
	}

	@Override
	public void setRpcHandler(RpcHandler rpcHandler) {
		this.rpcHandler = rpcHandler;
	}

	protected RpcHandler getRpcHandler() {
		return rpcHandler;
	}

	protected RpcProtocolCode getRpcProtocol(Byte protoType) {
		return rpcProtocols.get(protoType);
	}

	@Override
	public void doProcessOneWay(CustTMessage reqmsg) throws Exception {
		// one way 请求直接不处理
	}

	@Override
	public CustTMessage doProcess(CustTMessage reqmsg) throws Exception {
		RpcProtocolCode rpcCodeProtocol = getRpcProtocol(reqmsg.getCodeType());
		RpcHandler rpcHandler = getRpcHandler();

		RpcRequest req = rpcCodeProtocol.decodeRequest(reqmsg.getBody());
		RpcResponse res = null;
		try {
			res = rpcHandler.doRequest(req);
		} catch (Exception e) {
			res = new RpcResponse();
			res.setException(new TBizException(e.getMessage()));
		}

		ByteBuffer byteBuf = rpcCodeProtocol.encodeResponse(res);

		CustTMessage resmsg = CustTMessage.newResponseMessage();
		resmsg.setCodeType(reqmsg.getCodeType()); // rpcCodeProtocol encodeRes
		resmsg.setSeqId(reqmsg.getSeqId());
		resmsg.setLen(byteBuf.limit());
		resmsg.setBody(byteBuf);
		return resmsg;
	}
}
