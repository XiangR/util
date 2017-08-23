package com.joker.staticcommon;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;

public class TransUtility {

	final static String transUrl = "http://www.mzywfy.org.cn/ajaxservlet";
	final static String transUrlXiaoNiuToUY = "http://218.75.34.138:8080/NiuTransServer/translation?from=zh&to=uy";
	final static String transUrlXiaoNiuToZH = "http://218.75.34.138:8080/NiuTransServer/translation?from=uy&to=zh";
	final static String transUrlLingYunToUY = "http://218.241.146.70:8080/NiuTransServer/translation?from=zh&to=uy";
	final static String transUrlLingYunToZH = "http://218.241.146.70:8080/NiuTransServer/translation?from=uy&to=zh";

	public static void main(String[] args) {
		// List<String> asList = Arrays.asList("جۇڭگو", "ياخشىمۇ سىز");
		// asList = transListXiaoNiu(asList, false);
		String line = transforOne("中国", true);
		System.out.println(line);
	}

	public static List<String> transMutiple(List<String> lines) {
		if (lines != null && lines.size() > 0) {
			// 请求参数
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("from", "zh");
			params.put("to", "uy");
			params.put("url", "2");
			try {
				String url = transUrl;
				params.put("src_text", URLEncoder.encode(StringUtility.join(lines, "\n"), "utf-8"));
				JSONObject info = UrlDownloader.getInfo(url, params);
				if (info == null || info.getString("tgt_text") == null || info.isEmpty()) {
					return new ArrayList<String>();
				}
				String[] result_sec = info.getString("tgt_text").split("<br>");
				return Arrays.asList(result_sec).stream().map(s -> replaceStr(s)).collect(Collectors.toList());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ArrayList<String>();
	}

	/**
	 * 翻译一个字符串
	 * 
	 * @param lines
	 *            翻译内容
	 * @param toWeiyu
	 *            true：中文 -> 维语 false：维语 -> 中文
	 * @return
	 */
	public static String transforOne(String lines, Boolean toWeiyu) {
		String result = transforOneOld(lines, toWeiyu);
		if (StringUtility.isNullOrEmpty(result)) {
			result = transforOneXiaoNiu(lines, toWeiyu);
		}
		return result;
	}

	public static String transforOneOld(String lines, Boolean toWeiyu) {
		if (StringUtility.isNullOrEmpty(lines) || toWeiyu == null) {
			return null;
		}
		// 请求参数
		HashMap<String, String> params = new HashMap<String, String>();
		// 检查type
		if (toWeiyu) {
			params.put("from", "zh");
			params.put("to", "uy");
			params.put("url", "2");
		} else {
			params.put("from", "uy");
			params.put("to", "zh");
			params.put("url", "2");
		}

		int tryCount = 3;
		try {
			String url = transUrl;
			params.put("src_text", URLEncoder.encode(lines, "utf-8"));
			while (--tryCount > 0) {
				JSONObject info = UrlDownloader.getInfo(url, params);
				if (info == null || info.getString("tgt_text") == null || info.isEmpty()) {
					Thread.sleep(300);
					continue;
				}
				String[] result_sec = info.getString("tgt_text").split("<br>");
				String result = replaceStr(result_sec[0]);
				return result;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	public static String transforOneXiaoNiu(String lines, Boolean toWeiyu) {
		if (!StringUtility.isNullOrEmpty(lines)) {
			// 请求参数
			HashMap<String, String> params = new HashMap<String, String>();
			String urlXiaoNiu = "";
			String urlLingYun = "";
			if (toWeiyu) {
				urlXiaoNiu = transUrlXiaoNiuToUY;
				urlLingYun = transUrlLingYunToUY;
			} else {
				urlXiaoNiu = transUrlXiaoNiuToZH;
				urlLingYun = transUrlLingYunToZH;
			}

			try {
				params.put("url", "16");
				params.put("src_text", lines);

				int tryCount = 4;
				while (--tryCount > 0) {
					String real_url = urlXiaoNiu;
					if (tryCount % 2 == 0) {
						real_url = urlLingYun;
					}
					JSONObject info = UrlDownloader.getInfo(real_url, params);
					if (info == null || info.getString("tgt_text") == null || info.isEmpty()) {
						continue;
					}
					String[] result_sec = info.getString("tgt_text").split("\n");
					String result = replaceStr(result_sec[0]);
					return result;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 翻译一个字符串的集合
	 * 
	 * @param srcList
	 *            翻译内容
	 * @param toWeiyu
	 *            true：中文 -> 维语 false：维语 -> 中文
	 * @return
	 */
	public static List<String> transList(List<String> srcList, Boolean toWeiyu) {

		List<String> result = transListOld(srcList, toWeiyu);
		if (result == null || result.size() == 0) {
			result = transListXiaoNiu(srcList, toWeiyu);
		}
		return result;
	}

	public static List<String> transListOld(List<String> srcList, Boolean toWeiyu) {
		if (srcList != null && srcList.size() > 0 && toWeiyu != null) {
			String url = transUrl;
			// ZHConverter converter =
			// ZHConverter.getInstance(ZHConverter.SIMPLIFIED);
			HashMap<String, String> params = new HashMap<String, String>();
			if (toWeiyu) {
				// 正译
				params.put("from", "zh");
				params.put("to", "uy");
				params.put("url", "2");
			} else {
				// 反译
				params.put("from", "uy");
				params.put("to", "zh");
				params.put("url", "2");
			}

			try {
				String line = StringUtility.join(srcList, "\n");
				// String line_convert = converter.convert(line);

				params.put("src_text", URLEncoder.encode(line, "utf-8"));
				JSONObject info = UrlDownloader.getInfoIndex(url, params);

				if (info == null || info.getString("tgt_text") == null || info.getString("error_code") != null || info.getString("error_msg") != null) {
					return new ArrayList<String>();
				} else {
					String[] result = info.getString("tgt_text").split("<br>");
					return Arrays.asList(result).stream().map(s -> replaceStr(s)).collect(Collectors.toList());
				}

			} catch (Exception e) {
				e.printStackTrace();
				return new ArrayList<String>();
			}
		}
		return new ArrayList<String>();
	}

	public static List<String> transListXiaoNiu(List<String> srcList, Boolean toWeiyu) {
		if (srcList != null && srcList.size() > 0 && toWeiyu != null) {
			HashMap<String, String> params = new HashMap<String, String>();
			try {
				String urlXiaoNiu = "";
				String urlLingYun = "";
				if (toWeiyu) {
					urlXiaoNiu = transUrlXiaoNiuToUY;
					urlLingYun = transUrlLingYunToUY;
				} else {
					urlXiaoNiu = transUrlXiaoNiuToZH;
					urlLingYun = transUrlLingYunToZH;
				}
				// ZHConverter converter =
				// ZHConverter.getInstance(ZHConverter.SIMPLIFIED);
				params.put("url", "16");
				params.put("src_text", StringUtility.join(srcList, "\n"));
				int tryCount = 4;
				while (--tryCount > 0) {
					String real_url = urlXiaoNiu;
					if (tryCount % 2 == 0) {
						real_url = urlLingYun;
					}
					JSONObject info = UrlDownloader.getInfo(real_url, params);
					if (info == null || info.getString("tgt_text") == null || info.getString("error_code") != null) {
						continue;
					} else {
						String[] result = info.getString("tgt_text").split("\n");
						return Arrays.asList(result).stream().map(s -> replaceStr(s)).collect(Collectors.toList());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return new ArrayList<String>();
	}

	/**
	 * 对于比较复杂的情况 进行一个拆后的翻译
	 * 
	 * @param str
	 * @return
	 */
	public static String cutConversation(String str) {

		char[] strByte = str.toCharArray();
		List<String> list = new ArrayList<String>();
		for (int j = 0; j < strByte.length; j++) {
			list.add(String.valueOf(strByte[j]));
		}

		try {
			List<String> result = transList(list, true);
			StringBuffer resultBuffer = new StringBuffer();
			for (int k = 0; k < result.size(); k++) {
				String result_str = result.get(k);
				if (result_str.contains("；")) {
					result_str = result_str.substring(0, result_str.indexOf("；"));
				}
				resultBuffer.append(result_str.trim().replace(",", "").replace("10", "1"));
			}
			String finalResult = resultBuffer.toString().replace("-", "").replace(".", "").replace(")", "").replace("(", "").replace("“", "").replace("”", "").replace("(", "").replace("。", "");
			return finalResult;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String replaceStr(String str) {
		String result = str.trim().replace("<", "").replace(">", "").replace("，", "").replace("。", "").replace("“", "").replace("”", "");
		if (result.contains("；")) {
			result = result.substring(0, result.indexOf("；"));
		}
		return result;
	}
}
