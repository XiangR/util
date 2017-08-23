package com.joker.normal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressRegexp {

	public static void main(String[] args) {

		// replace();
		replace2();
	}

	private static void replace2() {
		String content = "两百公里";
		// String repxHigh = "(^[a-zA-Z0-9]+[,，]*[a-zA-Z0-9]*[\u4e00-\u9fa5]+)";
		String repxHigh = "[\u4e00-\u9fa5]+[公里|米]";
		// String repxHigh = "^[a-zA-Z]*{1}[0-9]*[\u4e00-\u9fa5]+";
		Pattern p = Pattern.compile(repxHigh);
		Matcher m = p.matcher(content);
		while (m.find()) {
			System.out.println("总过匹配的个数： " + m.groupCount());
			System.out.println("总过匹配的结果： " + m.group());
			int count = m.groupCount();
			for (int i = 1; i <= count; i++) {
				System.out.println("分组" + i + ": " + m.group(i));
				String str = m.group(i);
				System.out.println(String.format("str -> %s", str));
			}
		}

	}

	public static void replace() {
		/*
		 * 改进的逻辑匹配完成后使用分组的形式进行获取
		 */
		String content = "测试下午六点整测试";
		// String repx = "^([\\u4e00-\\u9fa5]+)([0-9]*)(号{1}|米{1})";
		// String repx = "^([\\u4e00-\\u9fa5]+)([0-9]*)(号{1}|米{1}|栋{1})";
		// String repx = "^([\\u4e00-\\u9fa5]+)(交叉口)([0-9]+)(号{1}|米{1}|栋{1})";
		// String repx =
		// "请保持直行(?:,|，|,,|，，)走(左|右)侧车道([\\u4e00-\\u9fa5]+[路|大道]*)";
		String repx = "测试([上午|下午][\u4e00-\u9fa5]+点整)测试";
		Pattern p = Pattern.compile(repx);
		Matcher m = p.matcher(content);
		while (m.find()) {
			System.out.println("总过匹配的个数： " + m.groupCount());
			System.out.println("总过匹配的结果： " + m.group());
			int count = m.groupCount();
			for (int i = 1; i <= count; i++) {
				System.out.println("分组" + i + ": " + m.group(i));
				String str = m.group(i);
				replaceNowTime(str);
			}
		}
	}

	private static String replaceNowTime(String src) {
		String number = src.substring(src.indexOf("午") + 1, src.indexOf("点"));
		String result = src.replace("上午", "چۇشتىن بۇرۇن").replace("下午", "چۈشتىن كىيىن").replace("点整", "").replace(number, " سائەت " + chineseNumberToInt(number));
		System.out.println(result);
		return src;
	}

	private static int chineseNumberToInt(String chineseNumber) {
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
