package com.joker.normal;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPattern {

	public static void main(String[] args) throws ParseException {

		Pattern p = Pattern.compile("([\\u4e00-\\u9fa5]+)");
		Matcher m = p.matcher("金云 ئاسراش سارىيى");
		// Matcher m = p.matcher("长乐西路");
		boolean find = m.find();
		System.out.println(find);

		// replaceCss();
		// replace();
		// test();
		// replace2();
		// replace3();
	}

	public static void replace1() {
		String destAuto = "ئبا竜چىسى چوڭ كوچىسى";
		boolean flag = destAuto.matches("[\\u4E00-\\u9FA5]+");
		System.out.println(flag);
	}

	public static void replace2() {
		Pattern p = Pattern.compile("([\\u4e00-\\u9fa5]+)");
		// Matcher m = p.matcher("长乐西路2号双鱼大厦一层和(西京医院)");
		Matcher m = p.matcher("ئباچىسى چ張وڭ كوچى");
		System.out.println(m.find());
		System.out.println(m.group(1));
	}

	public static void test() {
		Pattern p = Pattern.compile("^([\\u4e00-\\u9fa5]+)([0-9]+)号{1}([\\u4e00-\\u9fa5]+)和{1}\\({1}[\\u4e00-\\u9fa5]+\\){1}");
		Matcher m = p.matcher("长乐西路2号双鱼大厦一层和(西京医院)");
		System.out.println(m.find());
		System.out.println(m.group());
	}

	public static void replace() {
		/*
		 * 改进的逻辑匹配完成后使用分组的形式进行获取
		 */
		String content = "长乐西路99号双鱼大厦一层和(西京医院)";
		String repx = "^([\\u4e00-\\u9fa5]+)([0-9]*)(号{1})([\\u4e00-\\u9fa5]+)(和{1})\\({1}([\\u4e00-\\u9fa5]+)\\){1}";
		Pattern p = Pattern.compile(repx);
		Matcher m = p.matcher(content);
		while (m.find()) {
			System.out.println("总过匹配的个数： " + m.groupCount());
			System.out.println("总过匹配的结果： " + m.group());
			int count = m.groupCount();
			for (int i = 1; i <= count; i++) {
				System.out.println("分组" + i + ": " + m.group(i));
			}
		}
	}
}
