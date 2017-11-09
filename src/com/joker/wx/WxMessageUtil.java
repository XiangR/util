package com.joker.wx;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.joker.staticcommon.UrlDownloader;

public class WxMessageUtil {
	static Logger logger = LogManager.getLogger(WxMessageUtil.class.getSimpleName());

	private static String sendApiUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";
	private static String color = "#999";

	/**
	 * 构造模板消息发送参数
	 * 
	 * @param openId
	 *            微信openId
	 * @param templateId
	 *            微信模板Id
	 * @param params
	 *            参数键值对
	 * @return
	 */
	public static String getQuaryString(String openId, String templateId, Map<String, String> params) {
		TemplateData setTemplate_id = TemplateData.New()
				// 消息接收者
				.setTouser(openId)
				// 模板id
				.setTemplate_id(templateId);
		// .setUrl("http://m.xxxx.cn/qrcode/t/xxxxxx")
		for (String key : params.keySet()) { // 模板参数
			setTemplate_id.add(key, params.get(key), color);
		}
		String build = setTemplate_id.build();
		return build;
	}

	/**
	 * 请求发送消息
	 * 
	 * @param openId
	 *            微信openId
	 * @param templateId
	 *            微信模板Id
	 * @param params
	 *            参数键值对
	 * @return
	 */
	public static boolean send(String openId, String templateId, Map<String, String> params) {
		String quary = getQuaryString(openId, templateId, params);
		JSONObject infoOkHttp = UrlDownloader.getInfoOkHttp(sendApiUrl + AccessToken.getAccess_token(), quary);
		logger.info("发送结果：" + infoOkHttp);
		if (infoOkHttp == null || infoOkHttp.getInteger("errcode") == null || !infoOkHttp.getInteger("errcode").equals(0)) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("name", "服务-千岛湖温泉。\n");
		params.put("remark", "消费码为：17101119551569。");
		boolean send = send("om8brws_zywfUG5Hxsz28PKtUdA4", "BLjiTp4vg1G2-EAROVOXKiRxL7vF98BDy__7hYnDql4", params);
		System.out.println(send);
	}
}
