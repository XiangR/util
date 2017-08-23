package com.joker.payment;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
	public final static String MD5(byte[] data) throws NoSuchAlgorithmException {
		// 获得MD5摘要算法的 MessageDigest 对象
		MessageDigest mdInst = MessageDigest.getInstance("MD5");
		// 使用指定的字节更新摘要
		mdInst.update(data);
		// 获得密文
		byte[] md = mdInst.digest();
		// 把密文转换成十六进制的字符串形式
		int j = md.length;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < j; i++) {
			byte byte0 = md[i];
			builder.append(String.format("%02x", byte0));
		}
		return builder.toString();
	}

	public static String MD516(byte[] data) throws NoSuchAlgorithmException {
		String str = MD5(data);
		return str.substring(8, 24);
	}

	public final static String getMessageDigest(byte[] buffer) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(buffer);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}
}
