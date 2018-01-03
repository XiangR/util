package com.joker.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.joker.staticcommon.TCPUtility;

@Service
public class TCPServiceImpl implements TCPServiceI {

	Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

	private static ConcurrentMap<String, Socket> concurrentMap = new ConcurrentHashMap<String, Socket>();

	private static Vector<Socket> vector = new Vector<Socket>();

	public ConcurrentMap<String, Socket> getConcurrentMap() {
		return concurrentMap;
	}

	public void setConcurrentMap(ConcurrentMap<String, Socket> concurrentMap) {
		TCPServiceImpl.concurrentMap = concurrentMap;
	}

	public Vector<Socket> getVector() {
		return vector;
	}

	public void setVector(Vector<Socket> vector) {
		TCPServiceImpl.vector = vector;
	}

	private ServerSocket serverSocket;

	public TCPServiceImpl() {
		try {
			if (serverSocket == null) {
				serverSocket = new ServerSocket(10087);
			}
			new ReceiveThread().start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class ReceiveThread extends Thread {

		@Override
		public void run() {
			try {
				while (true) {
					logger.info("监听socket---");
					Socket socket = serverSocket.accept();
					logger.info("监听到新socket：" + socket);
					vector.add(socket);
				}
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * 发送命令
	 */
	@Override
	public boolean sendTCP(String key, byte[] value) {
		Socket socket = TCPServiceImpl.concurrentMap.get(key);
		if (socket == null) {
			logger.error("sendTCP: socket为空");
			TCPServiceImpl.concurrentMap.remove(key);
			return false;
		}
		try {
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(value);
			outputStream.flush();
			// outputStream.close();
			// outputStream.flush();
			// socket.shutdownOutput();
			logger.info("sendTCP: 发送成功");
			return true;
		} catch (IOException e) {
			logger.error(e);
			return false;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

	/**
	 * 发送命令，同步返回结果
	 */
	@Override
	public boolean sendTCPSynchronized(String key, byte[] value) {
		Socket socket = TCPServiceImpl.concurrentMap.get(key);

		if (socket == null) {
			logger.error("sendTCPSynchronized: socket为空");
			TCPServiceImpl.concurrentMap.remove(key);
			return false;
		}

		int count = 4;
		boolean isSuccess = true;
		while (--count > 0) {
			isSuccess = sendTCPSynchronizedChild(key, value);
			logger.info("isSuccess -> " + isSuccess + ", count -> " + count);
			if (isSuccess) {
				logger.info("isSuccess -> true 发送成功");
				return isSuccess;
			}
		}

		if (!isSuccess) {
			try {
				logger.info("isSuccess -> false 发送失败 socket 已删除");
				socket.close();
				TCPServiceImpl.concurrentMap.remove(key);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return isSuccess;
	}

	/**
	 * 执行连接
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Boolean sendTCPSynchronizedChild(String key, byte[] value) {

		Socket socket = TCPServiceImpl.concurrentMap.get(key);

		try {
			TCPServiceImpl.concurrentMap.remove(key);
			logger.info("sendTCPSynchronized: remove");

			readCacheData(socket);
			logger.info("sendTCPSynchronized: readCacheData");

			OutputStream outputStream = socket.getOutputStream();
			logger.info("sendTCPSynchronized: getOutputStream");

			outputStream.write(value);
			logger.info("sendTCPSynchronized: write");

			outputStream.flush();
			logger.info("sendTCPSynchronized: flush");

			long startTime = System.currentTimeMillis();
			long endTime;
			while (true) {
				/*
				 * 30 秒的响应时间 若是对方 down 机了，此方法就变成了死循环。
				 */
				endTime = System.currentTimeMillis();
				if (endTime - startTime > 30000) {
					logger.info("长时间未有数据，强制返回");
					concurrentMap.put(key, socket);
					return false;
				}

				InputStream inputStream = socket.getInputStream();
				if (inputStream.available() > 0) {
					logger.info("检测socket同步返回: " + socket);
					byte[] byteStart = new byte[1];

					logger.info("检查返回的头数据: " + TCPUtility.logForByteArr(byteStart));
					inputStream.read(byteStart, 0, 1);
					if (byteStart[0] == (byte) 0x01) {
						// 检测心跳包
						logger.info("返回数据为心跳包");
						byte[] byteHeart = new byte[17];
						inputStream.read(byteHeart, 0, 17);
					} else if (byteStart[0] == (byte) 0xFE) {
						// 检测上传数据
						logger.info("开始检测同步返回数据");
						String data = "";
						byte[] byteFirst = new byte[20];
						byte[] byteXor = new byte[1];
						inputStream.read(byteFirst, 0, 20);
						int dataLength = TCPUtility.BinaryToHexString(new byte[] { byteFirst[18], byteFirst[19] }); // 得到数据长度
						byte[] byteData = new byte[dataLength - 3];
						for (int i = 0; i < byteFirst.length; i++) {
							byteData[i] = byteFirst[i];
						}
						inputStream.read(byteData, 20, dataLength - 23); // 读到用来亦或校验的数据（所有的有效数据）
						inputStream.read(byteXor, 0, 1); // 得到亦或校验值
						Byte[] readerByte = new Byte[dataLength - 3]; // 有效的数据
																		// 不含头尾字符和亦或
						for (int j = 0; j < byteData.length; j++) {
							readerByte[j] = byteData[j];
						}

						int uploadDataCount = 0;
						byte[] uploadCommand = new byte[2]; // 得到命令
						byte[] uploadData = new byte[dataLength - 25]; // 得到数据
						if (TCPUtility.getXOR(readerByte) == byteXor[0]) {
							// 通过亦或校验
							for (int k = 0; k < readerByte.length; k++) {
								if (k == 20) {
									// 得到命令
									uploadCommand[0] = readerByte[k];
								}
								if (k == 21) {
									// 得到命令
									uploadCommand[1] = readerByte[k];
								}
								if (k >= 22 && k < readerByte.length) {
									// 得到数据
									data = data + (readerByte[k]);
									uploadData[uploadDataCount] = readerByte[k];
									uploadDataCount++;
								}
							}
							if (uploadCommand[0] == (byte) 0x07 && uploadCommand[1] == (byte) 0x80) {
								logger.info("回复数据： " + TCPUtility.byte2hex(uploadData));
								if (uploadData[0] == (byte) 0x10) {
									logger.info("sendTCPSynchronined: 命令发送成功--" + key);
									concurrentMap.put(key, socket);
									return true;
								} else if (uploadData[0] == (byte) 0x11) {
									logger.info("sendTCPSynchronined: 命令发送成功--此指令已在运行不需要执行操作--" + key);
								} else {
									logger.info("sendTCPSynchronined: 命令回复无法检测--" + key);
									concurrentMap.put(key, socket);
									return false;
								}
							}
						}
					} else {
						logger.info("没有匹配到任何一种类型，命令请求失败");
					}
				}
			}
		} catch (Exception e) {
			concurrentMap.put(key, socket);
			logger.error(e);
			return false;
		}
		/*
		 * 不能close 否则socket 会close finally { try { if(outputStream != null) {
		 * outputStream.close(); } if(inputStream != null) {
		 * inputStream.close(); } } catch (IOException e) { e.printStackTrace();
		 * } }
		 */
	}

	/**
	 * 尝试读掉socket的inputStream缓存数据
	 * 
	 * @param socket
	 */
	private void readCacheData(Socket socket) {
		InputStream inputStream;
		try {
			inputStream = socket.getInputStream();
			Integer count = inputStream.available();
			if (count > 0) {
				byte[] cacheData = new byte[count]; // 得到数据
				inputStream.read(cacheData, 0, count);
				logger.info("缓存数据： " + TCPUtility.byte2hex(cacheData));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 判断socket是否已经断开
	 * 
	 * @param socket
	 * @return close return false
	 */
	public Boolean isServerClose(Socket socket) {
		try {
			logger.info("发送1字节参数");
			socket.sendUrgentData(0xFF);// 发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
			return true;
		} catch (Exception se) {
			logger.error("长时间未连接返回错误。", se);
			return false;
		}
	}
}
