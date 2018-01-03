package com.joker.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.joker.staticcommon.StringUtility;
import com.joker.staticcommon.TCPUtility;

/**
 * 
 * @author xiangR
 * @date 2017年7月28日下午1:40:52
 *
 *       自动任务中来对tcp的连接请求进行解析
 */
@Service
public class CommonServiceImpl {
	Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

	static int TCP_CYCLE = 1;
	static Timer tcpTimer;

	TCPServiceI tcpServiceI;

	public CommonServiceImpl() {
		init();
	}

	public void init() {

		if (tcpTimer == null) {
			tcpTimer = new Timer();
			tcpTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					ConcurrentMap<String, Socket> concurrentMap = tcpServiceI.getConcurrentMap();
					Set<String> keySet = concurrentMap.keySet();
					for (String key : keySet) {
						Socket socket = concurrentMap.get(key);
						if (socket != null) {
							try {
								InputStream inputStream = socket.getInputStream();
								logger.info("进入了concurrentMap的遍历");
								if (inputStream.available() > 0) {
									logger.info("开始了 concurrentMap中数据的读取");
									checkSocket(socket);
								} else {
									logger.info("concurrentMap 中没有读到数据");
									// socket.close();
									// concurrentMap.remove(key);
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

					Vector<Socket> vector = tcpServiceI.getVector();
					Socket socket = null;

					Iterator<Socket> sockets = vector.iterator();
					while (sockets.hasNext()) {
						logger.info("进入了vector的遍历");
						socket = sockets.next();
						try {
							InputStream inputStream = socket.getInputStream();
							if (inputStream.available() > 0) {
								logger.info("开始了 vector中数据的读取");
								checkSocket(socket);
								continue;
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						// 本次的socket 解析后将其删除
						sockets.remove();
						logger.info("此socket已删除: " + socket);
					}
				}
			}, 1000, TCP_CYCLE * 60 * 1000);
		}
	}

	public boolean checkSocket(Socket socket) {
		InputStream inputStream;
		ConcurrentMap<String, Socket> concurrentMap = tcpServiceI.getConcurrentMap();
		try {
			inputStream = socket.getInputStream();
			if (inputStream.available() > 0) {
				logger.info("开始检测此socket: " + socket);

				byte[] byteStart = new byte[1];
				inputStream.read(byteStart, 0, 1);
				logger.info("byteStart: " + byteStart[0]);
				logger.info("byteStartTest: " + TCPUtility.byte2hex(byteStart));
				if (byteStart[0] == (byte) 0x01) {
					// 检测心跳包
					logger.info("进入了小端法的检测");
					byte[] byteHeart = new byte[17];
					inputStream.read(byteHeart, 0, 17);
					if (byteHeart[0] == (byte) 0xF0) {
						logger.info("开始检测心跳包");
					}
					logger.info("心跳包 - byteHeart: " + TCPUtility.logForByteArr(byteHeart));
					String interCode = "";
					for (int i = 1; i < byteHeart.length; i++) {
						interCode += byteHeart[i];
					}
					logger.info("检查心跳包成功  interCode: " + interCode);
					concurrentMap.put(interCode, socket);
					return true;
				} else if (byteStart[0] == (byte) 0xFE) {
					// 检测上传数据
					logger.info("开始检测上传数据");
					String version = "";
					String interCode = "";
					String data = "";
					byte[] byteFirst = new byte[20];
					byte[] byteXor = new byte[1];
					inputStream.read(byteFirst, 0, 20);
					logger.info("上传数据 - byteFirst: " + TCPUtility.logForByteArr(byteFirst));
					int dataLength = TCPUtility.BinaryToHexString(new byte[] { byteFirst[18], byteFirst[19] }); // 得到数据长度
					logger.info("检查上传数据成功，上传数据长度为  dataLength： " + dataLength);
					byte[] byteData = new byte[dataLength - 3];
					for (int i = 0; i < byteFirst.length; i++) {
						byteData[i] = byteFirst[i];
					}
					inputStream.read(byteData, 20, dataLength - 23); // 读到用来亦或校验的数据（所有的有效数据）
					logger.info("上传数据 - byteData: " + TCPUtility.logForByteArr(byteData));
					inputStream.read(byteXor, 0, 1); // 得到亦或校验值
					logger.info("亦或校验值 - byteXor: " + TCPUtility.logForByteArr(byteXor));
					Byte[] readerByte = new Byte[dataLength - 3]; // 有效的数据
																	// 不含头尾字符和亦或
					for (int j = 0; j < byteData.length; j++) {
						readerByte[j] = byteData[j];
					}

					int uploadDataCount = 0;
					byte[] uploadCommand = new byte[2]; // 得到命令
					byte[] versionByte = new byte[2]; // 得到版本
					byte[] uploadData = new byte[dataLength - 25]; // 得到数据
					byte[] replyCommand = new byte[] { 0x01, (byte) 0xD0 }; // 命令
					byte[] replyPara = new byte[] { (byte) 0x81 }; // 参数
					if (TCPUtility.getXOR(readerByte) == byteXor[0]) {
						// 通过亦或校验
						for (int k = 0; k < readerByte.length; k++) {
							if (k == 0) {
								// 得到版本号
								versionByte[0] = readerByte[k];
							}
							if (k == 1) {
								versionByte[1] = readerByte[k];
							}
							if (k >= 2 && k <= 17) {
								// 得到机号
								interCode = interCode + (readerByte[k]);
							}
							// 18 19 为数据长度 不需要
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
						version = versionByte[0] + "." + versionByte[1];
						logger.info("检查上传数据成功，获取数据上传 interCode: " + interCode);
						logger.info("检查上传数据成功，获取数据上传 version: " + version);
						logger.info("检查上传数据成功，获取数据上传 - 命令: " + TCPUtility.logForByteArr(uploadCommand));
						logger.info("检查上传数据成功，获取数据上传 - 数据: " + TCPUtility.logForByteArr(uploadData));
						logger.info("checkSocket - data :" + data);
						concurrentMap.put(interCode, socket);
						if (uploadCommand[0] == (byte) 0x01 && uploadCommand[1] == (byte) 0xA0) {
							logger.info("检测上传数据的命令成功");
							String uploadStr = new String(uploadData, "ASCII");
							logger.info("检测上传数据的命令：" + uploadStr);
							String[] splitData = uploadStr.split(",");
							if (splitData.length == 15) {

								logger.info("检测上传数据格式正确");
								String isValid = splitData[1];
								Double voltage = Double.parseDouble(splitData[13]); // 电压
								String gpsStr = null;
								if (isValid.toUpperCase().equals("A")) { // 经纬度是否可用
									logger.info("检测上传数据经纬度可用，去解析经纬度");
									gpsStr = StringUtility.processLocation(splitData[4], splitData[2]); // GPS信息
																										// 经纬度
								}
								String workModel = splitData[12];
								logger.info("voltage：" + voltage + ", GPS：" + gpsStr + ", workModel: " + workModel + ", isValid： " + isValid);

								if (workModel.equals("idle")) {
									// TODO 业务逻辑处理
								}
								// 检测到上传数据时返还数据到客户端
								replyPara[0] = (byte) 0x80;
								List<Byte> sendByte = TCPUtility.getSendByte(interCode, version, replyCommand, replyPara);
								logger.info("checkSocket - sendByte: " + TCPUtility.logForByte(sendByte));
								byte[] send = new byte[sendByte.size()];
								for (int k = 0; k < sendByte.size(); k++) {
									send[k] = sendByte.get(k);
								}
								tcpServiceI.sendTCP(interCode, send);
								return true;
							}
						}
						if (uploadCommand[0] == (byte) 0x07 && uploadCommand[1] == (byte) 0x80) {
							logger.info("收到了回复");
						}
					}
				} else {
					logger.info("没有匹配到任何一种类型");
				}
			}
		} catch (Exception e) {
			logger.error("发生异常", e);
			e.printStackTrace();
		}
		return false;
	}

	static Pattern parten = Pattern.compile("[0-9]{16}");

	public Boolean openBike(String interCode) {
		try {
			String version = "1.2";
			byte[] data = new byte[] { 0x07, 0x00 };
			byte[] re = new byte[] { 0x11, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
			List<Byte> sendStr = TCPUtility.getSendByte(interCode, version, data, re);
			logger.info("openBike - sendStr :" + TCPUtility.logForByte(sendStr));

			byte[] send = new byte[sendStr.size()];
			for (int k = 0; k < sendStr.size(); k++) {
				send[k] = sendStr.get(k);
			}
			logger.info("openBike - interCode :" + interCode);
			logger.info("openBike - send: " + TCPUtility.logForByteArr(send));

			// 这里发送请求去开车则返回失败
			if (parten.matcher(interCode).find()) {
				boolean sendSuccess = tcpServiceI.sendTCPSynchronized(interCode, send);
				logger.info("openBike - sendTCPSynchronized - sendSuccess :" + sendSuccess);
				return sendSuccess;
			} else {
				boolean isSuccess = tcpServiceI.sendTCP(interCode, send);
				logger.info("openBike - sendTCP - isSuccess :" + isSuccess);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Boolean closeBike(String interCode) {
		try {
			String version = "1.2";
			byte[] data = new byte[] { 0x07, 0x00 };
			byte[] re = new byte[] { 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }; // 0x10
																						// 是关闭
			List<Byte> sendStr = TCPUtility.getSendByte(interCode, version, data, re);
			logger.info("closeBike - sendStr :" + TCPUtility.logForByte(sendStr));
			logger.info("closeBike - interCode :" + interCode);

			byte[] send = new byte[sendStr.size()];
			for (int k = 0; k < sendStr.size(); k++) {
				send[k] = sendStr.get(k);
			}

			if (parten.matcher(interCode).find()) {
				boolean sendSuccess = tcpServiceI.sendTCPSynchronized(interCode, send);
				logger.info("closeBike - sendTCPSynchronized - sendSuccess :" + sendSuccess);
				return sendSuccess;
			} else {
				boolean isSuccess = tcpServiceI.sendTCP(interCode, send);
				logger.info("closeBike - sendTCP - isSuccess :" + isSuccess);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
