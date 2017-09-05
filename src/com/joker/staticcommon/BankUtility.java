package com.joker.staticcommon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class BankUtility {
	static Logger logger = LogManager.getLogger(BankUtility.class.getName());

	static String requestUrl = "https://ccdcapi.alipay.com/validateAndCacheCardInfo.json";
	static String imgUrl = "https://apimg.alipay.com/combo.png?d=cashier&t=";
	static String allBanks = "https://ab.alipay.com/i/yinhang.htm";

	public static void main(String[] args) {

		// JSONObject jsonObject = getCardNumber("1180001714297");// 错误测试
		// JSONObject jsonObject = getCardNumber("6217001180001714297");//
		// 储蓄卡
		// JSONObject jsonObject = getCardNumber("4835910066821607");// 信用卡
		// logger.info(jsonObject);
		// getAllBanks();
		getName();
	}

	public static String getAllBanks() {
		// String info = UrlDownloader.getContent(allBanks, null);
		try {
			Document doc = Jsoup.connect(allBanks).timeout(10000).get();
			Elements elementsByClass = doc.getElementsByClass("icon-box");
			logger.info("before: " + bankNames.size());
			if (elementsByClass.size() > 0) {
				for (Element classElement : elementsByClass) {
					Element element = classElement.children().get(0);
					String bankName = StringUtility.isNullOrEmpty(element.attr("title")) ? element.html() : element.attr("title");
					String bankUid = element.attr("class").replace("icon", "").trim();
					bankNames.put(bankUid, bankName);
					logger.info(bankName + " -> " + bankUid);
					FileOperation.appendFileTextLine("f:bank.txt", bankName + " -> " + bankUid);
				}
			}
			logger.info("after: " + bankNames.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void getBankImg(String filePath) {
		String realPath = RunTimeConfig.getRealPath(null);
		logger.info(realPath);
		filePath = filePath + "/photos/bank";
		FileOutputStream output = null;
		String nowUid = "";
		try {
			File fp = new File(filePath);
			if (!fp.exists()) { // 由路径创建文件夹
				fp.mkdir();
			}
			for (String bankUid : bankNames.keySet()) {
				nowUid = bankUid;
				String url = "https://apimg.alipay.com/combo.png?d=cashier&t=" + bankUid;
				byte[] responseBody = UrlDownloader.getByteArr(url);
				if (responseBody == null) {
					logger.error(nowUid + "下载失败");
					continue;
				}
				File storeFile = new File(filePath + "/" + nowUid + ".png");
				if (storeFile.exists()) {
					storeFile.delete();
				}
				output = new FileOutputStream(storeFile);
				output.write(responseBody);
			}
		} catch (Exception ex) {
			logger.error(nowUid + "下载失败", ex);
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static JSONObject getCardNumber(String cardNumber) {
		JSONObject result = new JSONObject();
		result.put("status", "ERROR");
		if (!StringUtility.isNullOrEmpty(cardNumber)) {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("_input_charset", "utf-8");
			params.put("cardNo", cardNumber);
			params.put("cardBinCheck", "true");
			try {
				String url = "https://ccdcapi.alipay.com/validateAndCacheCardInfo.json";
				JSONObject info = UrlDownloader.getInfo(url, params);
				logger.info(info);
				if (info != null && !info.isEmpty()) {
					JSONArray parseArray = JSONArray.parseArray(info.getString("messages"));
					result.put("messages", parseArray);
					result.put("key", info.getString("key"));
					result.put("validated", info.getBoolean("validated"));
					boolean hasError = parseArray.stream().anyMatch(k -> {
						return ((JSONObject) k).getString("errorCodes") != null && ((JSONObject) k).getString("errorCodes").length() > 0;
					});
					if (!hasError) {
						result.put("bankName", bankNames.get(info.getString("bank")));
						result.put("cardType", bankTypes.get(info.getString("cardType")));
						result.put("status", "SUCCESS");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static void getName() {
		List<String> fileNames = FileOperation.getFileNames("f:Logos");
		for (String name : fileNames) {
			if (!bankNames.containsKey(name.replace(".png", ""))) {
				logger.info(name);
			}
		}
	}

	static Map<String, String> bankTypes = new HashMap<String, String>() {
		private static final long serialVersionUID = -3900397910631368581L;
		{
			put("DC", "借记卡");
			put("CC", "信用卡");
		}
	};

	static Map<String, String> bankNames = new HashMap<String, String>() {
		private static final long serialVersionUID = -3900397910631368581L;
		{
			put("SRCB", "深圳农村商业银行");
			put("BGB", "广西北部湾银行");
			put("SHRCB", "上海农村商业银行");
			put("BJBANK", "北京银行");
			put("WHCCB", "威海市商业银行");
			put("BOZK", "周口银行");
			put("KORLABANK", "库尔勒市商业银行");
			put("SPABANK", "平安银行");
			put("SDEB", "顺德农商银行");
			put("HURCB", "湖北省农村信用社");
			put("WRCB", "无锡农村商业银行");
			put("BOCY", "朝阳银行");
			put("CZBANK", "浙商银行");
			put("HDBANK", "邯郸银行");
			put("BOC", "中国银行");
			put("BOD", "东莞银行");
			put("CCB", "中国建设银行");
			put("ZYCBANK", "遵义市商业银行");
			put("SXCB", "绍兴银行");
			put("GZRCU", "贵州省农村信用社");
			put("ZJKCCB", "张家口市商业银行");
			put("BOJZ", "锦州银行");
			put("BOP", "平顶山银行");
			put("HKB", "汉口银行");
			put("SPDB", "上海浦东发展银行");
			put("NXRCU", "宁夏黄河农村商业银行");
			put("NYNB", "广东南粤银行");
			put("GRCB", "广州农商银行");
			put("BOSZ", "苏州银行");
			put("HZCB", "杭州银行");
			put("HSBK", "衡水银行");
			put("HBC", "湖北银行");
			put("JXBANK", "嘉兴银行");
			put("HRXJB", "华融湘江银行");
			put("BODD", "丹东银行");
			put("AYCB", "安阳银行");
			put("EGBANK", "恒丰银行");
			put("CDB", "国家开发银行");
			put("TCRCB", "江苏太仓农村商业银行");
			put("NJCB", "南京银行");
			put("ZZBANK", "郑州银行");
			put("DYCB", "德阳商业银行");
			put("YBCCB", "宜宾市商业银行");
			put("SCRCU", "四川省农村信用");
			put("KLB", "昆仑银行");
			put("LSBANK", "莱商银行");
			put("YDRCB", "尧都农商行");
			put("CCQTGB", "重庆三峡银行");
			put("FDB", "富滇银行");
			put("JSRCU", "江苏省农村信用联合社");
			put("JNBANK", "济宁银行");
			put("CMB", "招商银行");
			put("JINCHB", "晋城银行JCBANK");
			put("FXCB", "阜新银行");
			put("WHRCB", "武汉农村商业银行");
			put("HBYCBANK", "湖北银行宜昌分行");
			put("TZCB", "台州银行");
			put("TACCB", "泰安市商业银行");
			put("XCYH", "许昌银行");
			put("CEB", "中国光大银行");
			put("NXBANK", "宁夏银行");
			put("HSBANK", "徽商银行");
			put("JJBANK", "九江银行");
			put("NHQS", "农信银清算中心");
			put("MTBANK", "浙江民泰商业银行");
			put("LANGFB", "廊坊银行");
			put("ASCB", "鞍山银行");
			put("KSRB", "昆山农村商业银行");
			put("YXCCB", "玉溪市商业银行");
			put("DLB", "大连银行");
			put("DRCBCL", "东莞农村商业银行");
			put("GCB", "广州银行");
			put("NBBANK", "宁波银行");
			put("BOYK", "营口银行");
			put("SXRCCU", "陕西信合");
			put("GLBANK", "桂林银行");
			put("BOQH", "青海银行");
			put("CDRCB", "成都农商银行");
			put("QDCCB", "青岛银行");
			put("HKBEA", "东亚银行");
			put("HBHSBANK", "湖北银行黄石分行");
			put("WZCB", "温州银行");
			put("TRCB", "天津农商银行");
			put("QLBANK", "齐鲁银行");
			put("GDRCC", "广东省农村信用社联合社");
			put("ZJTLCB", "浙江泰隆商业银行");
			put("GZB", "赣州银行");
			put("GYCB", "贵阳市商业银行");
			put("CQBANK", "重庆银行");
			put("DAQINGB", "龙江银行");
			put("CGNB", "南充市商业银行");
			put("SCCB", "三门峡银行");
			put("CSRCB", "常熟农村商业银行");
			put("SHBANK", "上海银行");
			put("JLBANK", "吉林银行");
			put("CZRCB", "常州农村信用联社");
			put("BANKWF", "潍坊银行");
			put("ZRCBANK", "张家港农村商业银行");
			put("FJHXBC", "福建海峡银行");
			put("ZJNX", "浙江省农村信用社联合社");
			put("LZYH", "兰州银行");
			put("JSB", "晋商银行");
			put("BOHAIB", "渤海银行");
			put("CZCB", "浙江稠州商业银行");
			put("YQCCB", "阳泉银行");
			put("SJBANK", "盛京银行");
			put("XABANK", "西安银行");
			put("BSB", "包商银行");
			put("JSBANK", "江苏银行");
			put("FSCB", "抚顺银行");
			put("HNRCU", "河南省农村信用");
			put("COMM", "交通银行");
			put("XTB", "邢台银行");
			put("CITIC", "中信银行");
			put("HXBANK", "华夏银行");
			put("HNRCC", "湖南省农村信用社");
			put("DYCCB", "东营市商业银行");
			put("ORBANK", "鄂尔多斯银行");
			put("BJRCB", "北京农村商业银行");
			put("XYBANK", "信阳银行");
			put("ZGCCB", "自贡市商业银行");
			put("CDCB", "成都银行");
			put("HANABANK", "韩亚银行");
			put("CMBC", "中国民生银行");
			put("LYBANK", "洛阳银行");
			put("GDB", "广东发展银行");
			put("ZBCB", "齐商银行");
			put("CBKF", "开封市商业银行");
			put("H3CB", "内蒙古银行");
			put("CIB", "兴业银行");
			put("CRCBANK", "重庆农村商业银行");
			put("SZSBK", "石嘴山银行");
			put("DZBANK", "德州银行");
			put("SRBANK", "上饶银行");
			put("LSCCB", "乐山市商业银行");
			put("JXRCU", "江西省农村信用");
			put("ICBC", "中国工商银行");
			put("JZBANK", "晋中市商业银行");
			put("HZCCB", "湖州市商业银行");
			put("NHB", "南海农村信用联社");
			put("XXBANK", "新乡银行");
			put("JRCB", "江苏江阴农村商业银行");
			put("YNRCC", "云南省农村信用社");
			put("ABC", "中国农业银行");
			put("GXRCU", "广西省农村信用");
			put("PSBC", "中国邮政储蓄银行");
			put("BZMD", "驻马店银行");
			put("ARCU", "安徽省农村信用社");
			put("GSRCU", "甘肃省农村信用");
			put("LYCB", "辽阳市商业银行");
			put("JLRCU", "吉林农信");
			put("URMQCCB", "乌鲁木齐市商业银行");
			put("XLBANK", "中山小榄村镇银行");
			put("CSCB", "长沙银行");
			put("JHBANK", "金华银行");
			put("BHB", "河北银行");
			put("NBYZ", "鄞州银行");
			put("LSBC", "临商银行");
			put("BOCD", "承德银行");
			put("SDRCU", "山东农信");
			put("NCB", "南昌银行");
			put("TCCB", "天津银行");
			put("WJRCB", "吴江农商银行");
			put("CBBQS", "城市商业银行资金清算中心");
			put("HBRCU", "河北省农村信用社");
		}
	};

}
