/**
 * Copyright (c) 2011-2014, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.joker.wx;

import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.joker.staticcommon.UrlDownloader;

/**
 * 模板消息 API
 * 文档地址：http://mp.weixin.qq.com/wiki/17/304c1885ea66dbedf7dc170d84999a9d.html
 */
public class TemplateMsgApi {
	private static String sendApiUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";

	/**
	 * 发送模板消息
	 * 
	 * @param jsonStr
	 *            json字符串
	 * @return {ApiResult}
	 */
	public static String send() {

		String quary = getQuaryString();
		String token = "Fn2pDleTxTrELX5N9rQW98Uw8N0C2IBPkIW-CukGZ-7389ZkqvItg15ZNASpotDmpFa6WB9Pf5RJPRls8Lw4KGvBp3DAdhaMUDpmFKOzxEpeTnHor2NxhstTcOEHSuJ4MNWeACAHIR";
		String jsonResult = "";
		String content = UrlDownloader.getContentOkHttp(sendApiUrl + token, quary);
		System.out.println(content);
		return new String(jsonResult);
	}

	public static String getQuaryString() {
		String build = TemplateData.New()
				// 消息接收者
				.setTouser("om8brws_zywfUG5Hxsz28PKtUdA4")
				// 模板id
				.setTemplate_id("BLjiTp4vg1G2-EAROVOXKiRxL7vF98BDy__7hYnDql4")
				// .setUrl("http://m.xxxx.cn/qrcode/t/xxxxxx")
				// 模板参数
				.add("name", "服务-千岛湖温泉。\n", "#999").add("remark", "消费码为：17101119551569。", "#999").build();
		System.out.println(build);
		return build;
	}

	public static void main(String[] args) {
		// String token = getToken();
		// System.out.println(token);
		send();
	}

	public static String getToken() {
		String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential";
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("appid", "wxb562be6f5d1fd50d");
		params.put("secret", "51b90fcd632211004b7a0571c406593a");
		JSONObject content = UrlDownloader.getInfo(url, params);
		System.out.println(content.toJSONString());
		return content.getString("access_token");
	}
}
