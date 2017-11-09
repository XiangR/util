package com.joker.wx;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.joker.staticcommon.UrlDownloader;

public class AccessToken {

	static Logger logger = LogManager.getLogger(AccessToken.class.getSimpleName());

	private static final String requestTokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential";
	private static String APPID = "wxb562be6f5d1fd50d";// ConfigParser.getPaymentProperty("html_appid");
	private static String APPSECRET = "51b90fcd632211004b7a0571c406593a";// ConfigParser.getPaymentProperty("html_appsecret");

	private static String access_token; // 正确获取到 access_token 时有值
	private static Integer errcode; // 出错时有值
	private static String errmsg; // 出错时有值
	private static LocalDateTime expiredTime = LocalDateTime.now();

	static {
		try {
			logger.info(String.format("static 代码块请求 refreshAccessToken -> appid: %s, appSecret: %s", APPID, APPSECRET));
			AccessToken.refreshAccessToken();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void refreshAccessToken() {
		JSONObject result = AccessToken.requestAccessToken();
		if (result == null) {
			return;
		}
		AccessToken.access_token = result.getString("access_token");
		long expiresIn = result.getLongValue("expires_in");
		AccessToken.errcode = result.getInteger("errcode");
		AccessToken.errmsg = result.getString("errmsg");
		AccessToken.expiredTime = expiredTime.plusSeconds(expiresIn);
	}

	private synchronized static JSONObject requestAccessToken() {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("appid", APPID);
		params.put("secret", APPSECRET);
		logger.info(String.format("请求 requestAccessToken -> appid: %s, appSecret: %s, time: %s", APPID, APPSECRET, LocalDateTime.now().toString()));
		JSONObject result = UrlDownloader.getInfo(requestTokenUrl, params);
		if (result == null || result.getString("access_token") == null) {
			return null;
		}
		return result;
	}

	public static boolean isAvailable() {
		if (errcode != null)
			return false;
		if (expiredTime == null || expiredTime.isBefore(LocalDateTime.now()))
			return false;
		return access_token != null;
	}

	public static String getAccess_token() {
		if (isAvailable()) {
			return access_token;
		} else {
			AccessToken.refreshAccessToken();
			return access_token;
		}
	}

	public Integer getErrcode() {
		return errcode;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public LocalDateTime getExpiredTime() {
		return expiredTime;
	}

	public static void main(String[] args) {
		String access_token2 = AccessToken.getAccess_token();
		System.out.println(access_token2);
	}
}
