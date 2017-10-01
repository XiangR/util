package com.joker.wxpay;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.github.wxpay.sdk.WXPayConfig;
import com.joker.fileupload.ConfigParser;

public class WXPayConfigImpl implements WXPayConfig {
	static Logger logger = LogManager.getLogger(WXPayConfigImpl.class.getName());

	static String APPID = ConfigParser.getPaymentProperty("appid");
	static String KEY = ConfigParser.getPaymentProperty("key");
	static String MCHID = ConfigParser.getPaymentProperty("mchid");

	private byte[] certData;
	private static WXPayConfigImpl INSTANCE;

	public WXPayConfigImpl() {
		logger.info(String.format("APPID -> %s, KEY -> %s, MCHID -> %s", APPID, KEY, MCHID));
		InputStream inputStream = null;
		try {
			// String certPath = "/path/to/apiclient_cert.p12";
			// File file = new File(certPath);
			// InputStream certStream = new FileInputStream(file);
			// this.certData = new byte[(int) file.length()];
			// certStream.read(this.certData);
			// certStream.close();
			String certPath = "/apiclient_cert.p12";
			inputStream = WXPayConfigImpl.class.getResourceAsStream(certPath);
			this.certData = IOUtils.toByteArray(inputStream);
			logger.info(this.certData);
		} catch (IOException e) {
			// logger.error("初始化MyConfig 失败.", e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public static WXPayConfigImpl getInstance() {
		if (INSTANCE == null) {
			synchronized (WXPayConfigImpl.class) {
				if (INSTANCE == null) {
					INSTANCE = new WXPayConfigImpl();
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
