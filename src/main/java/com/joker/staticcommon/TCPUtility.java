package com.joker.staticcommon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TCPUtility {

	public static byte getXOR(Byte[] items) {
		byte ChkSum = 0;
		for (int i = 0; i < items.length; i++) {
			ChkSum = (byte) (ChkSum ^ items[i]);
		}
		return ChkSum;
	}

	public static Byte[] short2byte(short res) {
		Byte[] targets = new Byte[2];
		targets[0] = (byte) (res & 0xff);// 最低位
		targets[1] = (byte) ((res >> 8) & 0xff);// 次低位
		return targets;
	}

	// 这里有可能报异常 要进行处理
	public static List<Byte> getSendByte(String interCode, String version, byte[] data, byte[] re) {
		List<Byte> sendData = new ArrayList<Byte>();
		List<Byte> byteXor = new ArrayList<Byte>();
		Integer start = 0xFE;
		sendData.add(start.byteValue());// 开始
		String[] replaceAll = version.split("[.]");
		for (int i = 0; i < replaceAll.length; i++) {
			byte parseByte = Byte.parseByte(replaceAll[i]);
			sendData.add(parseByte); // 版本号
			byteXor.add(parseByte);
		}
		char[] interCodeByte = interCode.toCharArray();
		for (int i = 0; i < interCodeByte.length; i++) {
			byte parseByte = Byte.parseByte(interCodeByte[i] + "");
			sendData.add(parseByte);// 国际识别码
			byteXor.add(parseByte);
		}
		Integer len = sendData.size() + data.length + re.length + 2 + 2; // 后面的加2是补上亦或校验和结束的字符
		System.out.println();
		short s = Short.parseShort(len.toString());
		Byte[] short2byte = short2byte(s);
		sendData.addAll(Arrays.asList(short2byte));
		byteXor.addAll(Arrays.asList(short2byte));
		for (int i = 0; i < data.length; i++) {
			sendData.add((data[i]));// 命令
			byteXor.add(data[i]);
		}
		for (int i = 0; i < re.length; i++) {
			sendData.add((re[i]));// 命令参数
			byteXor.add(re[i]);
		}
		byte xor = getXOR(byteXor.toArray(new Byte[byteXor.size()]));
		sendData.add(xor);// 异或校验
		sendData.add((byte) 0xFD);// 结束
		return sendData;
	}

	/**
	 * 由byteList 转成String
	 * 
	 * @param byteArray
	 * @return
	 */
	public static String logForByte(List<Byte> byteArray) {
		StringBuffer logbuffer = new StringBuffer();
		for (Byte byte1 : byteArray) {
			// System.out.print(Integer.toHexString(Byte.hashCode(byte1)) + "
			// ");
			logbuffer.append(Integer.toHexString(Byte.hashCode(byte1)) + " ");
		}
		return logbuffer.toString();
	}

	/**
	 * 由byteArray 转成String
	 * 
	 * @param byteArray
	 * @return
	 */
	public static String logForByteArr(byte[] byteArray) {
		StringBuffer logbuffer = new StringBuffer();
		for (Byte byte1 : byteArray) {
			logbuffer.append(Integer.toHexString(Byte.hashCode(byte1)) + " ");
		}
		return logbuffer.toString();
	}

	/**
	 * 由byte[] 转成十六进制字符串并转成10进制
	 * 
	 * @param bytes
	 * @return
	 */
	public static int BinaryToHexString(byte[] bytes) {
		String hexStr = "0123456789ABCDEF";
		String result = "";
		String hex = "";
		for (int i = 0; i < bytes.length; i++) {
			// 字节高4位
			hex = String.valueOf(hexStr.charAt((bytes[i] & 0xF0) >> 4));
			// 字节低4位
			hex += String.valueOf(hexStr.charAt(bytes[i] & 0x0F));
			result += hex + ",";
		}
		String[] split = result.split(",");
		return Integer.valueOf(split[1] + split[0], 16);
	}

	/**
	 * 由byte[] 转成十六进制字符串
	 * 
	 * @param bytes
	 * @return
	 */
	public static String byte2hex(byte[] buffer) {
		String h = "";
		for (int i = 0; i < buffer.length; i++) {
			String temp = Integer.toHexString(buffer[i] & 0xFF);
			if (temp.length() == 1) {
				temp = "0" + temp;
			}
			h = h + " " + temp;
		}
		return h;
	}

	public static void main(String[] args) {

		String interCode = "0861 5100 3003 4000".replace(" ", "");
		String version = "1.2";
		byte[] data = new byte[] { 0x07, 0x00 };
		byte[] re = new byte[] { 0x11, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		List<Byte> sendStr = TCPUtility.getSendByte(interCode, version, data, re);
		StringBuffer buffer = new StringBuffer();
		for (Byte byte1 : sendStr) {
			System.out.print(Integer.toHexString(Byte.hashCode(byte1)) + " ");
			buffer.append(Integer.toHexString(Byte.hashCode(byte1)) + " ");
		}
		System.out.println();
		System.out.println(buffer.toString());
	}
}
