package com.joker.fileupload;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.byteSources.ByteSource;
import org.apache.sanselan.common.byteSources.ByteSourceFile;
import org.apache.sanselan.formats.jpeg.JpegImageParser;
import org.apache.sanselan.formats.jpeg.segments.UnknownSegment;

public class JpegReader {

	public static final int COLOR_TYPE_RGB = 1;
	public static final int COLOR_TYPE_CMYK = 2;
	public static final int COLOR_TYPE_YCCK = 3;

	private int colorType = COLOR_TYPE_RGB;
	private boolean hasAdobeMarker = false;
	Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

	public BufferedImage readImage(File file) throws IOException, ImageReadException {
		colorType = COLOR_TYPE_RGB;
		hasAdobeMarker = false;

		ImageInputStream stream = ImageIO.createImageInputStream(file);
		try {
			Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
			while (iter.hasNext()) {
				ImageReader reader = iter.next();
				reader.setInput(stream);

				BufferedImage image;
				try {
					image = reader.read(0);
				} catch (IIOException e) {
					try {
						colorType = COLOR_TYPE_CMYK;
						checkAdobeMarker(file);
						ICC_Profile profile = Sanselan.getICCProfile(file);// No
																			// dispose
																			// method.
						WritableRaster raster = (WritableRaster) reader.readRaster(0, null);// No
																							// dispose
																							// method.
						if (colorType == COLOR_TYPE_YCCK)
							convertYcckToCmyk(raster);
						if (hasAdobeMarker)
							convertInvertedColors(raster);
						image = convertCmykToRgb(raster, profile);
					} catch (Exception ex) {
						logger.error(String.format("file path:%s, file name:%s", file.getPath(), file.getName()), ex);
						throw ex;
					}
				} finally {
					reader.dispose();
				}
				while (iter.hasNext()) {
					iter.next().dispose();
				}
				return image;
			}
		} finally {
			stream.close();
		}

		return null;
	}

	public void checkAdobeMarker(File file) throws IOException, ImageReadException {
		JpegImageParser parser = new JpegImageParser();
		ByteSource byteSource = new ByteSourceFile(file);// No dispose method.
		@SuppressWarnings("rawtypes")
		ArrayList segments = parser.readSegments(byteSource, new int[] { 0xffee }, true);
		if (segments != null && segments.size() >= 1) {
			UnknownSegment app14Segment = (UnknownSegment) segments.get(0);// No
																			// dispose
																			// method.
			byte[] data = app14Segment.bytes;
			if (data.length >= 12 && data[0] == 'A' && data[1] == 'd' && data[2] == 'o' && data[3] == 'b' && data[4] == 'e') {
				hasAdobeMarker = true;
				int transform = app14Segment.bytes[11] & 0xff;
				if (transform == 2)
					colorType = COLOR_TYPE_YCCK;
			}
		}
	}

	public static void convertYcckToCmyk(WritableRaster raster) {
		int height = raster.getHeight();
		int width = raster.getWidth();
		int stride = width * 4;
		int[] pixelRow = new int[stride];
		for (int h = 0; h < height; h++) {
			raster.getPixels(0, h, width, 1, pixelRow);

			for (int x = 0; x < stride; x += 4) {
				int y = pixelRow[x];
				int cb = pixelRow[x + 1];
				int cr = pixelRow[x + 2];

				int c = (int) (y + 1.402 * cr - 178.956);
				int m = (int) (y - 0.34414 * cb - 0.71414 * cr + 135.95984);
				y = (int) (y + 1.772 * cb - 226.316);

				if (c < 0)
					c = 0;
				else if (c > 255)
					c = 255;
				if (m < 0)
					m = 0;
				else if (m > 255)
					m = 255;
				if (y < 0)
					y = 0;
				else if (y > 255)
					y = 255;

				pixelRow[x] = 255 - c;
				pixelRow[x + 1] = 255 - m;
				pixelRow[x + 2] = 255 - y;
			}

			raster.setPixels(0, h, width, 1, pixelRow);
		}
	}

	public static void convertInvertedColors(WritableRaster raster) {
		int height = raster.getHeight();
		int width = raster.getWidth();
		int stride = width * 4;
		int[] pixelRow = new int[stride];
		for (int h = 0; h < height; h++) {
			raster.getPixels(0, h, width, 1, pixelRow);
			for (int x = 0; x < stride; x++)
				pixelRow[x] = 255 - pixelRow[x];
			raster.setPixels(0, h, width, 1, pixelRow);
		}
	}

	public static BufferedImage convertCmykToRgb(Raster cmykRaster, ICC_Profile cmykProfile) throws IOException {
		if (cmykProfile == null)
			cmykProfile = ICC_Profile.getInstance(JpegReader.class.getResourceAsStream("/ISOcoated_v2_300_eci.icc"));

		if (cmykProfile.getProfileClass() != ICC_Profile.CLASS_DISPLAY) {
			byte[] profileData = cmykProfile.getData();

			if (profileData[ICC_Profile.icHdrRenderingIntent] == ICC_Profile.icPerceptual) {
				intToBigEndian(ICC_Profile.icSigDisplayClass, profileData, ICC_Profile.icHdrDeviceClass); // Header
																											// is
																											// first

				cmykProfile = ICC_Profile.getInstance(profileData);
			}
		}

		ICC_ColorSpace cmykCS = new ICC_ColorSpace(cmykProfile);
		BufferedImage rgbImage = new BufferedImage(cmykRaster.getWidth(), cmykRaster.getHeight(), BufferedImage.TYPE_INT_RGB);
		WritableRaster rgbRaster = rgbImage.getRaster();// No dispose method.
		ColorSpace rgbCS = rgbImage.getColorModel().getColorSpace();// No
																	// dispose
																	// method.
		ColorConvertOp cmykToRgb = new ColorConvertOp(cmykCS, rgbCS, null);// No
																			// dispose
																			// method.
		cmykToRgb.filter(cmykRaster, rgbRaster);
		return rgbImage;
	}

	static void intToBigEndian(int value, byte[] array, int index) {
		array[index] = (byte) (value >> 24);
		array[index + 1] = (byte) (value >> 16);
		array[index + 2] = (byte) (value >> 8);
		array[index + 3] = (byte) (value);
	}
}
