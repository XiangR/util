package com.joker.wxpay;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.github.wxpay.sdk.WXPayConfig;
import com.joker.fileupload.ConfigParser;

public class WXPayConfigHtmlImpl implements WXPayConfig {
	static Logger logger = LogManager.getLogger(WXPayConfigHtmlImpl.class.getName());

	static String APPID = ConfigParser.getPaymentProperty("html_appid");
	static String KEY = ConfigParser.getPaymentProperty("html_key");
	static String MCHID = ConfigParser.getPaymentProperty("html_mchid");

	private byte[] certData;
	private static WXPayConfigHtmlImpl INSTANCE;

	public WXPayConfigHtmlImpl() {
		logger.info(String.format("APPID -> %s, KEY -> %s, MCHID -> %s", APPID, KEY, MCHID));
		InputStream inputStream = null;
		try {
			// String certPath = "/path/to/apiclient_cert.p12";
			// File file = new File(certPath);
			// InputStream certStream = new FileInputStream(file);
			// this.certData = new byte[(int) file.length()];
			// certStream.read(this.certData);
			// certStream.close();
			String certPath = "/apiclient_cert_html.p12";
			inputStream = WXPayConfigHtmlImpl.class.getResourceAsStream(certPath);
			this.certData = IOUtils.toByteArray(inputStream);

			logger.info(this.certData);

		} catch (Exception e) {
			logger.error("初始化MyConfig 失败.", e);

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

	public static WXPayConfigHtmlImpl getInstance() {
		if (INSTANCE == null) {
			synchronized (WXPayConfigHtmlImpl.class) {
				if (INSTANCE == null) {
					INSTANCE = new WXPayConfigHtmlImpl();
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
