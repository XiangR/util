package com.joker.tcp;

import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ConcurrentMap;

public interface TCPServiceI {
	/**
	 * 发送命令
	 * 
	 * @param key
	 *            国际识别号
	 * @param value
	 *            命令
	 * @return
	 */
	boolean sendTCP(String key, byte[] value);

	/**
	 * 获取socket map
	 * 
	 * @return
	 */
	public ConcurrentMap<String, Socket> getConcurrentMap();

	/**
	 * 获取socket Vector
	 * 
	 * @return
	 */
	public Vector<Socket> getVector();

	boolean sendTCPSynchronized(String key, byte[] value);
}
