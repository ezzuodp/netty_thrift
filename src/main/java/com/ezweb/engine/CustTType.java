package com.ezweb.engine;

/**
 * 类型,最大值 0b1111
 * @author : zuodp
 * @version : 1.10
 */
public interface CustTType {
	byte CALL = 1;
	byte REPLY = 2;
	byte ONEWAY = 3;    // 请求没有近回值.
}
