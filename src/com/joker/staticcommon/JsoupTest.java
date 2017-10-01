package com.joker.staticcommon;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupTest {

	static String host = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2016/";
	static String hostXinJiang = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2016/65/";

	public static void main(String[] args) {
		getDcument();
	}

	public static void parseContent() {
		// String str = "<p><span style=\\\"font-family:&#39;微软雅黑
		// Light&#39;,&#39;sans-serif&#39;\\\">具有国际一流水平的千鹤湾温泉风情小镇，以长者健康、舒心、快乐为己任，舒适高档的居住环境，卫生营养的可口美食，温暖周到的“保姆式”服务，还有强身健体、陶冶情操的各类活动，如：先进的健身器材、门球、棋牌类、音乐、歌舞、书画、垂钓等，更有为长者健康保驾护航的老年专科医院。千鹤湾还规划占地800亩的旅游景区，设有温泉浴场、机动游乐公园、欢乐水世界、农家乐、长者生活特色一条街等，是长者休闲、度假、养生、养老的绝佳选择。</span></p><p><br/></p>";
		String str = "<p><img src=\\\"photos/ueditor/m3/1504677686097337.jpg\\\" title=\\\"upfile\\\" alt=\\\"QQ截图20151114125621.png\\\"/></p><p>大家可以畅所欲言</p><p>了解更多关于海贼王的知识</p>";
		Document doc = Jsoup.parse(str);
		String textNodes = doc.text();
		System.out.println(textNodes);
	}

	public static void getDcument() {
		String parentUrl = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2016/65.html";
		try {
			/*
			 * 1：每次http请求之前等待，减少connect time out | read time out
			 * 2：Jsoup 的connect、read 公用同一个time out，设置足够的时间
			 * 3：启用代理 userAgent
			 */
			Thread.sleep(300);
			Document document1 = Jsoup.connect(parentUrl).userAgent("Mozilla").timeout(50000).get();
			Elements firstClass = document1.getElementsByClass("citytr");
			for (Element first : firstClass) {
				String attr = first.select("a").get(0).attr("href");
				String firstUrl = host + attr;
				Thread.sleep(300);
				Document ductment2 = Jsoup.connect(firstUrl).userAgent("Mozilla").timeout(50000).get();
				Elements secondClass = ductment2.getElementsByClass("countytr");
				for (Element second : secondClass) {
					Elements select = second.select("a");
					if (select.size() > 0) {
						String attr2 = select.get(0).attr("href");
						String temp = attr2.substring(0, attr2.indexOf("/")) + "/";
						String secondUrl = hostXinJiang + attr2;
						Thread.sleep(300);
						Document document3 = Jsoup.connect(secondUrl).userAgent("Mozilla").timeout(50000).get();
						Elements thirdClass = document3.getElementsByClass("towntr");
						for (Element third : thirdClass) {
							String attr3 = third.select("a").get(0).attr("href");
							String fourUrl = hostXinJiang + temp + attr3;
							Thread.sleep(300);
							Document document4 = Jsoup.connect(fourUrl).userAgent("Mozilla").timeout(50000).get();
							Elements fourClass = document4.getElementsByClass("villagetr");
							for (Element four : fourClass) {
								Elements result = four.select("td");
								if (result.size() > 2) {
									String txt = result.get(0).text().substring(0, 6) + "," + result.get(2).text();
									FileOperation.appendFileTextLine("f:/villagetr/data.txt", txt);
									System.out.println(four);
								}
							}
						}
					}
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
