package com.joker.staticcommon;

import java.io.UnsupportedEncodingException;
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

	public static String commonTeamEmailFormat(String content) {
		if (isNullOrEmpty(content)) {
			return content;
		} else {
			String result = "尊敬的用户您好:\n\n" + appendPeriod(content) + "\r\n详细内容请参考网站信息，谢谢！" + "\n\n如需关闭邮箱接收可参考：“个人中心”-->“设置”-->“账户安全” →“是否接收所在团队的活动通知”。"
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

}
