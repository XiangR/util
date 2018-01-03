package com.joker.staticcommon;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 
 * @author xiangR
 * @date 2017年7月28日上午10:45:33
 * 
 *       生成验证码
 *
 */
public class RandomUtility {
	private ByteArrayInputStream image;// 图像
	private String str;// 验证码
	Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

	private RandomUtility() {
		init();// 初始化属性
	}

	/*
	 * 取得RandomNumUtil实例
	 */
	public static RandomUtility Instance() {
		return new RandomUtility();
	}

	/*
	 * 取得验证码图片
	 */
	public ByteArrayInputStream getImage() {
		return this.image;
	}

	/*
	 * 取得图片的验证码
	 */
	public String getString() {
		return this.str;
	}

	private void init() {
		// 在内存中创建图象
		int width = 125, height = 50;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		// 获取图形上下文
		Graphics g = image.getGraphics();
		// 生成随机类
		Random random = new Random();
		// 设定背景色
		g.setColor(getRandColor(180, 250));
		g.fillRect(0, 0, width, height);
		Font[] fonts = new Font[] { // new Font("宋体", Font.BOLD, 28),
				new Font("黑体", Font.BOLD, 48), new Font("楷体_GB2312", Font.BOLD, 46), new Font("仿宋_GB2312", Font.BOLD, 44), new Font("新宋体", Font.BOLD, 50) };

		// 设定字体

		// 随机产生155条干扰线，使图象中的认证码不易被其它程序探测到
		g.setColor(getRandColor(160, 200));
		g.drawRect(0, 0, width - 2, height - 2);
		for (int i = 0; i < 155; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int xl = random.nextInt(12);
			int yl = random.nextInt(12);
			g.drawLine(x, y, x + xl, y + yl);
		}
		// 取随机产生的认证码(6位数字)
		int length = 4;
		String base = "abcdefghijkmnopqrstuvwxyz0123456789";
		int size = base.length();
		String sRand = "";
		for (int i = 0; i < length; i++) {
			int start = random.nextInt(size);
			String rand = base.substring(start, start + 1);
			sRand += rand;
			// 将认证码显示到图象中
			g.setFont(fonts[random.nextInt(length)]);
			g.setColor(new Color(20 + random.nextInt(80), 20 + random.nextInt(100), 20 + random.nextInt(90)));
			// 调用函数出来的颜色相同，可能是因为种子太接近，所以只能直接生成
			g.drawString(rand, (20 + random.nextInt(9)) * i + 8, 26 + random.nextInt(22));
		}
		// 赋值验证码
		this.str = sRand;

		// 图象生效
		g.dispose();
		ByteArrayInputStream input = null;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			ImageOutputStream imageOut = ImageIO.createImageOutputStream(output);
			ImageIO.write(image, "JPEG", imageOut);
			imageOut.close();
			input = new ByteArrayInputStream(output.toByteArray());
		} catch (Exception e) {
			logger.error("", e);
			System.out.println("验证码图片产生出现错误：" + e.toString());
		}

		this.image = input;/* 赋值图像 */
	}

	/*
	 * 给定范围获得随机颜色
	 */
	private Color getRandColor(int fc, int bc) {
		Random random = new Random();
		if (fc > 255)
			fc = 255;
		if (bc > 255)
			bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}
}
