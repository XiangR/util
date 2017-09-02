package com.joker.wxpay;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.github.wxpay.sdk.WXPayConfig;
import com.joker.fileupload.ConfigParser;

public class MyConfig implements WXPayConfig {
	static Logger logger = LogManager.getLogger(MyConfig.class.getName());

	static String APPID = ConfigParser.getPaymentProperty("appid");
	static String KEY = ConfigParser.getPaymentProperty("key");
	static String MCHID = ConfigParser.getPaymentProperty("mchid");

	private byte[] certData;
	private static MyConfig INSTANCE;

	public MyConfig() {
		logger.info(String.format("APPID -> %s, KEY -> %s, MCHID -> %s", APPID, KEY, MCHID));
		try {
			String certPath = "/path/to/apiclient_cert.p12";
			File file = new File(certPath);
			InputStream certStream = new FileInputStream(file);
			this.certData = new byte[(int) file.length()];
			certStream.read(this.certData);
			certStream.close();
		} catch (IOException e) {
			// logger.error("初始化MyConfig 失败.", e);
		}
	}

	public static MyConfig getInstance() {
		if (INSTANCE == null) {
			synchronized (MyConfig.class) {
				if (INSTANCE == null) {
					INSTANCE = new MyConfig();
				}
			}
		}
		return INSTANCE;
	}

	@Override
	public String getAppID() {
		return APPID;
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getMchID() {
		return MCHID;
	}

	public InputStream getCertStream() {
		ByteArrayInputStream certBis = new ByteArrayInputStream(this.certData);
		return certBis;
	}

	public int getHttpConnectTimeoutMs() {
		return 8000;
	}

	public int getHttpReadTimeoutMs() {
		return 10000;
	}
}
