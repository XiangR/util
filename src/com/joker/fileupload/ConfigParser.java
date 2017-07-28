package com.joker.fileupload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ConfigParser {
	private static Properties khcoinProperties;
	private static Properties paymentProperties;
	private static Properties searchProperties;
	private static Properties tdkProperties;

	static Logger logger = LogManager.getLogger(ConfigParser.class.getName());

	static Map<Integer, Integer> map;

	public static String getCommonProperty(String key) {
		if (khcoinProperties == null) {
			khcoinProperties = getProperties("/common.properties");
		}
		return khcoinProperties.getProperty(key);
	}

	public static String getPaymentProperty(String key) {
		if (paymentProperties == null) {
			paymentProperties = getProperties("/payment.properties");
		}
		return paymentProperties.getProperty(key);
	}

	public static String getSearchProperty(String key) {
		if (searchProperties == null) {
			searchProperties = getProperties("/search.properties");
		}
		return searchProperties.getProperty(key);
	}

	public static String getTdkProperty(String key) {
		if (tdkProperties == null) {
			tdkProperties = getProperties("/tdkmsg.properties");
		}
		return tdkProperties.getProperty(key);
	}

	private static Properties getProperties(String filePath) {
		Properties pro = new Properties();
		InputStream in = ConfigParser.class.getResourceAsStream(filePath);
		try {
			pro.load(in);
		} catch (IOException e) {
			logger.error(String.format("filePath:%s", filePath), e);
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error(String.format("filePath:%s", filePath), e);
					e.printStackTrace();
				}
			}
		}
		return pro;
	}

	public static String getResourceFileContent(String filePath, String encoding) {
		InputStream in = ConfigParser.class.getResourceAsStream(filePath);
		try {
			return IOUtils.toString(in, encoding);
		} catch (IOException e) {
			logger.error(String.format("filePath:%s", filePath), e);
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error(String.format("filePath:%s", filePath), e);
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}
