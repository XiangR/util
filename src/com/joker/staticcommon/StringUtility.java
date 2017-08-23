package com.joker.staticcommon;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.joker.fileupload.ConfigParser;

/**
 * 
 * @author xiangR
 * @date 2017年7月27日下午2:28:33
 *
 */
public class StringUtility {
	static Logger logger = LogManager.getLogger(StringUtility.class.getSimpleName());

	/**
	 * 判断字符串是否为null或者是空串 ""
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNullOrEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static boolean equals(String str1, String str2) {
		if (str1 == str2) {
			return true;
		}
		return str1 != null && str1.equals(str2);
	}

	public static int getDisplaySize(String str) {
		int size = str.length();
		for (char c : str.toCharArray()) {
			if (isChinese(c))
				++size;
		}
		return size;
	}

	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}

	public static boolean hasChinese(String str) {
		for (int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			if (isChinese(c)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isInteger(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	public static String padLeft(String str, int minLength) {
		return padLeft(str, minLength, ' ');

	}

	public static String padLeft(String str, int minLength, char pad) {
		if (str.length() >= minLength) {
			return str;
		}
		return newString(pad, minLength - str.length()) + str;
	}

	public static String padRight(String str, int minLength) {
		return padRight(str, minLength, ' ');
	}

	public static String padRight(String str, int minLength, char pad) {
		if (str.length() >= minLength) {
			return str;
		}
		return str + newString(pad, minLength - str.length());
	}

	public static String newString(char c, int length) {
		char[] array = new char[length];
		for (int i = 0; i < length; ++i) {
			array[i] = c;
		}
		return new String(array);
	}

	/**
	 * 将字符串进行转换替换里面的引号和双引号
	 * 
	 * @param str
	 * @return
	 */
	public static String getJsString(String str) {
		if (!isNullOrEmpty(str))
			return str.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"");
		return EMPTY;
	}

	public static String EMPTY = "";

	/**
	 * descriptiton 特殊字符处理
	 * 
	 * @param str
	 * @return
	 */
	public static String string2Json(String str) {
		if (isNullOrEmpty(str))
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {

			char c = str.charAt(i);
			switch (c) {
			case '\"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '/':
				sb.append("\\/");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\'':
				sb.append("\\\'");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String decodeSearchContent(String search) {
		if (!StringUtility.isNullOrEmpty(search)) {
			logger.info(String.format("SEARCH Step1:%s", search));
			search = search.replace("_", "\\");
			search = StringUtility.unicodeToString(search);
			logger.info(String.format("SEARCH Step2:%s", search));
			if (search.startsWith("\\")) {
				search = search.replaceFirst("\\\\", "");
			}
			search = search.replace("-", "%");
			if (search.startsWith("%")) {
				try {
					search = java.net.URLDecoder.decode(search, "UTF-8");
					logger.info(String.format("SEARCH Step3:%s", search));
				} catch (UnsupportedEncodingException e) {
					logger.info(String.format("SEARCH Step4:%s", search), e);
					e.printStackTrace();
				}
			}

			search = search.trim();
		}
		return search;
	}

	@SuppressWarnings("rawtypes")
	public static String join(Iterable lines, String join) {
		if (lines == null) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		for (Object str : lines) {
			if (str != null) {
				result.append(str);
				result.append(join);
			}
		}
		if (result.length() > 0) {
			result.setLength(result.length() - join.length());
		}
		return result.toString();
	}

	public static String join(Object[] lines, String join) {
		if (lines == null) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		for (Object str : lines) {
			if (str != null) {
				result.append(str);
				result.append(join);
			}
		}
		if (result.length() > 0) {
			result.setLength(result.length() - join.length());
		}
		return result.toString();
	}

	public static String unicodeToString(String str) {

		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
		Matcher matcher = pattern.matcher(str);
		char ch;
		while (matcher.find()) {
			ch = (char) Integer.parseInt(matcher.group(2), 16);
			str = str.replace(matcher.group(1), ch + "");
		}
		return str;
	}

	public static String filter(String str) {
		String regEx = "[`~!@#$%^&*()\\-+={}':;,\\[\\].<>/?￥%…（）_+|【】‘；：”“’。，、？\\s]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim();
	}

	public static String replaceHtml(String disclaimer) {
		return disclaimer == null ? null : "<span>" + disclaimer.replaceAll("\\r\\n", "</span><span>") + "</span>";
	}

	// 消息添加超链接
	public static String appendActivityMsgHtml(Integer id, String title) {
		return "<a target=\"_blank\" href=\"/activity/show/" + id + "\">" + title + "</a>";
	}

	// 邮件添加超链接
	public static String appendActivityMsgEmail(Integer id, String title) {
		return "<a target=\"_blank\" href=\"" + ConfigParser.getCommonProperty("imgServerPath") + "activity/show/" + id + "\">" + title + "</a>";
	}

	/**
	 * 给内容添加句号
	 * 
	 * @param content
	 * @return
	 */
	public static String appendPeriod(String content) {

		String str = content.trim();
		String symbol = String.valueOf(str.charAt(str.length() - 1));
		if (symbol.equals("!") || symbol.equals(".") || symbol.equals("。") || symbol.equals("！") || symbol.equals(">")) {
			return content;
		} else {
			return content + "。";
		}
	}

	/**
	 * 取得html中所有的中文
	 * 
	 * @param content
	 * @return
	 */
	public static String regexpHtmlContent(String content) {
		/*
		 * 观察规律可以发现 ueditor 生成的html是以<p></p>作为一行 以</p>切分数组 再使用正则取出一行的中文添加上<br>即可
		 */
		Pattern pattern = Pattern.compile("([\\u4e00-\\u9fa5]+)");
		String[] split = content.split("</p>");
		int count = 0;
		if (split.length > 0) {
			StringBuffer resultBuffer = new StringBuffer();
			for (String single : split) {
				String result = "";
				Matcher matcher = pattern.matcher(single);
				if (matcher.find()) {
					result = matcher.group(1);
					System.out.println(result);
					resultBuffer.append(result);
					resultBuffer.append("<br>");
					if (++count == 5) {
						break;
					}
				}
			}
			return resultBuffer.toString();
		} else {
			return content;
		}
	}

	/**
	 * 将消息内容转化为固定的邮件格式
	 * 
	 * @param content
	 *            消息
	 * @return
	 */
	public static String commonEmailFormat(String content) {

		if (isNullOrEmpty(content)) {
			return content;
		} else {
			String result = "尊敬的用户您好:\n\n" + appendPeriod(content) + "\r\n详细内容请参考网站信息，谢谢！" + "\n\n如需关闭邮箱接收可参考：“个人中心”-->“设置”-->“账户安全” →“是否接收邮件提醒”。"
					+ "\n\nPlease don't reply directly to this automatically-generated e-mail message. To contact HongHu World, please do not reply to this message, but instead go to http://www.honghuworld.com for contact information."
					+ "\n\n 此为系统自动邮件，请勿回复。如需联系鸿鹄世界，请访问鸿鹄世界网站  http://www.honghuworld.com 获取联系方式";
			return result;
		}
	}

	public static String removeNonBmpUnicode(String str) {
		if (str == null) {
			return null;
		}
		str = str.replaceAll("[^\\u0000-\\uFFFF]", "");
		return str;
	}

	/**
	 * 经纬度的处理
	 * 
	 * @param lng
	 *            经度
	 * @param lat
	 *            纬度
	 * @return lng,lat
	 */
	public static String processLocation(String lng, String lat) {
		if (isNullOrEmpty(lat) || isNullOrEmpty(lng)) {
			return null;
		} else {
			return processLocation(lng) + "," + processLocation(lat);
		}
	}

	public static String processLocation(String lat) {

		String str1 = lat.substring(0, lat.indexOf(".") - 2);
		String str2 = lat.replace(str1, "");

		DecimalFormat format = new DecimalFormat("#.000000");
		String result = String.valueOf(format.format(Double.valueOf(str1) + Double.valueOf(str2) / 60));
		System.out.println("result: " + result);
		return result;
	}

	/**
	 * 根据提供的经度和纬度、以及半径，取得此半径内的最大最小经纬度
	 * 
	 * @param lat
	 *            纬度
	 * @param lon
	 *            经度
	 * @param raidus
	 *            半径（米）
	 * @return minLat, minLng, maxLat, maxLng
	 */
	public static double[] GetAround(double lat, double lon, int raidus) {

		double PI = 3.14159265;

		Double latitude = lat;
		Double longitude = lon;

		Double degree = (24901 * 1609) / 360.0;
		double raidusMile = raidus;

		Double dpmLat = 1 / degree;
		Double radiusLat = dpmLat * raidusMile;
		Double minLat = latitude - radiusLat;
		Double maxLat = latitude + radiusLat;

		Double mpdLng = degree * Math.cos(latitude * (PI / 180));
		Double dpmLng = 1 / mpdLng;
		Double radiusLng = dpmLng * raidusMile;
		Double minLng = longitude - radiusLng;
		Double maxLng = longitude + radiusLng;
		return new double[] { minLat, minLng, maxLat, maxLng };
	}

	/**
	 * 插入数据库utf-8时有时会报存在 emoji 表情错误 来替换掉string 中的类似字符
	 * 
	 * @param str
	 * @return
	 */
	public static String replaceStr(String str) {
		return str.replaceAll("[\\x{10000}-\\x{10FFFF}]", "");
	}

	/**
	 * 将大写数字改为阿拉伯数组
	 * 
	 * @param chineseNumber
	 * @return int
	 */
	public static String chineseToNumber(String chineseNumber) {
		chineseNumber = chineseNumber.replace("两", "二");
		if (chineseNumber.contains("点")) { // 对带点的数取整
			String str1 = chineseNumber.substring(0, chineseNumber.indexOf("点"));
			String str2 = chineseNumber.substring(chineseNumber.indexOf("点") + 1, chineseNumber.length());
			/// return chineseNumberToInt(str1) + " پۈتۈن ئوندەن " +
			/// chineseNumberToInt(str2);
			return chineseNumberToInt(str1) + "." + chineseNumberToInt(str2);
		} else {
			return chineseNumberToInt(chineseNumber) + "";
		}
	}

	public static Integer chineseNumberToInt(String chineseNumber) {
		chineseNumber = chineseNumber.replace("两", "二");
		int result = 0;
		int temp = 1;// 存放一个单位的数字如：十万
		int count = 0;// 判断是否有chArr
		char[] cnArr = new char[] { '一', '二', '三', '四', '五', '六', '七', '八', '九' };
		char[] chArr = new char[] { '十', '百', '千', '万', '亿' };
		for (int i = 0; i < chineseNumber.length(); i++) {
			boolean b = true;// 判断是否是chArr
			char c = chineseNumber.charAt(i);
			for (int j = 0; j < cnArr.length; j++) {// 非单位，即数字
				if (c == cnArr[j]) {
					if (0 != count) {// 添加下一个单位之前，先把上一个单位值添加到结果中
						result += temp;
						temp = 1;
						count = 0;
					}
					// 下标+1，就是对应的值
					temp = j + 1;
					b = false;
					break;
				}
			}
			if (b) {// 单位{'十','百','千','万','亿'}
				for (int j = 0; j < chArr.length; j++) {
					if (c == chArr[j]) {
						switch (j) {
						case 0:
							temp *= 10;
							break;
						case 1:
							temp *= 100;
							break;
						case 2:
							temp *= 1000;
							break;
						case 3:
							temp *= 10000;
							break;
						case 4:
							temp *= 100000000;
							break;
						default:
							break;
						}
						count++;
					}
				}
			}
			if (i == chineseNumber.length() - 1) {// 遍历到最后一个字符
				result += temp;
			}
		}
		return result;
	}
}
