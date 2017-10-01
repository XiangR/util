package com.joker.staticcommon;

import java.util.HashMap;
import java.util.Map;

import com.joker.fileupload.ConfigParser;

public class CommonRunTimeConfig {
	public static boolean isDebug = true;
	public static final String CMSCERT = "32833439749570232-0853058945720379743";
	public static String LOGO_PATH = "images/cms_login1.png";

	public static Double providerReturnPercent = 0.79; // 扣点模式返回的百分比
	public static Double commonPointPercent = 0.01;// 公共消费积分返还百分比
	public static Double groupAwardPercent = 0.8; // 公共团购返回奖金百分比
	public static Double groupPercent = 0.7; // 公共团购的百分比
	public static Integer groupLimit = 10; // 公共团购的最低人数
	public static Double signUpPoint = 10d; // 签到奖励积分
	public static Integer paymentDeadline = 30; // 订单最大支付时长（分钟）
	public static Integer refundDeadline = 14; // 全额退款时长（天数）
	public static String imgServerPath = ConfigParser.getCommonProperty("imgServerPath"); // 图片服务器路径
	public static String serverPath = ConfigParser.getCommonProperty("serverPath"); // 服务器路径

	// 状态表记录
	public static Map<String, Map<Integer, String>> statusMaps = new HashMap<String, Map<Integer, String>>();
}
