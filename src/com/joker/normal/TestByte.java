package com.joker.normal;

import java.io.UnsupportedEncodingException;

public class TestByte {

	public static void main(String[] args) {
		byte[] test = new byte[] { (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x34, (byte) 0x30, (byte) 0x2e, (byte) 0x37, (byte) 0x39, (byte) 0x39, (byte) 0x2c, (byte) 0x56,
				(byte) 0x2c, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x2e, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x2c, (byte) 0x0, (byte) 0x2c, (byte) 0x0, (byte) 0x0,
				(byte) 0x0, (byte) 0x0, (byte) 0x2e, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x2c, (byte) 0x0, (byte) 0x2c, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30,
				(byte) 0x2e, (byte) 0x30, (byte) 0x2c, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x2e, (byte) 0x30, (byte) 0x2c, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30,
				(byte) 0x30, (byte) 0x2c, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x2e, (byte) 0x30, (byte) 0x2c, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x2e, (byte) 0x30, (byte) 0x30,
				(byte) 0x2c, (byte) 0x6e, (byte) 0x6f, (byte) 0x72, (byte) 0x6d, (byte) 0x61, (byte) 0x6c, (byte) 0x2c, (byte) 0x62, (byte) 0x75, (byte) 0x73, (byte) 0x79, (byte) 0x2c, (byte) 0x31,
				(byte) 0x31, (byte) 0x2e, (byte) 0x30, (byte) 0x30, (byte) 0x2c, (byte) 0x20, (byte) 0x6f, (byte) 0x6e };

		int uploadDataCount = 0;
		byte[] uploadData = new byte[2];
		for (int k = 0; k < test.length; k++) {
			// 将一部分的test字符串数组转到一个新的数组中
			if (k >= 89 && k < test.length) {
				uploadData[uploadDataCount] = test[k];
				uploadDataCount++;
				// System.out.println(uploadDataCount);
			}
		}

		// System.out.println("数组" + Normal.byte2hex(uploadData));

		String s;
		try {
			// 发来数据中存的是 ASCII码 转成字符串
			s = new String(test, "ASCII");
			System.out.println("长度" + test.length);
			System.out.println(s);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}
}
