package com.joker.staticcommon;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

/**
 * 生成QRCode 二维码
 * 
 * @author xiangR
 * @date 2017年9月26日上午11:10:20
 *
 */
public class QRCodeUtility {

	static Logger logger = LogManager.getLogger(QRCodeUtility.class.getName());
	private static final int BLACK = 0x00000000;
	private static final int WHITE = 0xFFFFFFFF;

	public static byte[] getImg(String url, Integer size) throws IOException, WriterException {
		size = 600;
		Map<EncodeHintType, String> hints = new HashMap<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		BitMatrix matrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, size, size, hints);
		BufferedImage image = toBufferedImage(matrix);
		return toByteArray(image);
	}

	private static byte[] toByteArray(BufferedImage image) {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ImageIO.write(image, "JPEG", output);
			return output.toByteArray();
		} catch (Exception e) {
			logger.error("二维码图片产生出现错误", e);
		}
		return null;
	}

	public static BufferedImage toBufferedImage(BitMatrix matrix) {
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, matrix.get(x, y) == true ? BLACK : WHITE);
			}
		}
		image.flush();
		return image;
	}
}
