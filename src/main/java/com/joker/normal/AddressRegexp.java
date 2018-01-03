package com.joker.normal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.joker.staticcommon.StringUtility.chineseNumberToInt;

public class AddressRegexp {

    public static void main(String[] args) {

        // replace();
        replace2();
    }

    private static void replace2() {
        String content = "两百公里";
        // String regexpHigh = "(^[a-zA-Z0-9]+[,，]*[a-zA-Z0-9]*[\u4e00-\u9fa5]+)";
        String regexpHigh = "[\u4e00-\u9fa5]+[公里|米]";
        // String regexpHigh = "^[a-zA-Z]*{1}[0-9]*[\u4e00-\u9fa5]+";
        Pattern p = Pattern.compile(regexpHigh);
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
        // String regexp = "^([\\u4e00-\\u9fa5]+)([0-9]*)(号{1}|米{1})";
        // String regexp = "^([\\u4e00-\\u9fa5]+)([0-9]*)(号{1}|米{1}|栋{1})";
        // String regexp = "^([\\u4e00-\\u9fa5]+)(交叉口)([0-9]+)(号{1}|米{1}|栋{1})";
        // String regexp = "请保持直行(?:,|，|,,|，，)走(左|右)侧车道([\\u4e00-\\u9fa5]+[路|大道]*)";
        String regexp = "测试([上午|下午][\u4e00-\u9fa5]+点整)测试";
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(content);
        while (m.find()) {
            System.out.println("总过匹配的个数： " + m.groupCount());
            System.out.println("总过匹配的结果： " + m.group());
            int count = m.groupCount();
            for (int i = 1; i <= count; i++) {
                System.out.println("分组" + i + ": " + m.group(i));
                String str = m.group(i);
                System.out.println(replaceNowTime(str));
            }
        }
    }

    private static String replaceNowTime(String src) {
        String number = src.substring(src.indexOf("午") + 1, src.indexOf("点"));
        return src.replace("上午", "چۇشتىن بۇرۇن").replace("下午", "چۈشتىن كىيىن").replace("点整", "").replace(number, " سائەت " + chineseNumberToInt(number));
    }
}
