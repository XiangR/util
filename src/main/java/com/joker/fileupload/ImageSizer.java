package com.joker.fileupload;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.im4java.core.CompositeCmd;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;
import org.im4java.core.ImageCommand;
import org.im4java.core.Operation;
import org.im4java.process.ProcessTask;

import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.joker.staticcommon.StringUtility;

public class ImageSizer {
	public static Logger logger = LogManager.getLogger(ImageSizer.class.getSimpleName());

	static String gmPath = null;
	static {
		gmPath = ConfigParser.getCommonProperty("gmPath");
	}

	public static void doZip(String srcFile, String destFile, int new_w, int new_h) {
		doZip(srcFile, destFile, new_w, new_h, false);
	}

	public static void doZip(String srcFile, String destFile, int new_w, int new_h, boolean overwrite) {
		CommandInfo cmd = getZipCommand(srcFile, destFile, new_w, new_h, overwrite);
		if (cmd != null)
			cmd.run();
	}

	public static void doWatermark(String srcFile, String press) {
		CommandInfo cmd = getWatermarkCommand(srcFile, press);
		if (cmd != null)
			cmd.run();
	}

	public static void doCrop(String srcFile, String destFile, float widthHeightRate, int maxWidth) {
		doCrop(srcFile, destFile, widthHeightRate, maxWidth, false);
	}

	public static void doCrop(String srcFile, String destFile, float widthHeightRate, int maxWidth, boolean overwrite) {
		CommandInfo cmd = getCropCommand(srcFile, destFile, widthHeightRate, maxWidth, overwrite);
		if (cmd != null)
			cmd.run();
	}

	public static class CommandInfo {
		public static Logger logger = LogManager.getLogger(CommandInfo.class.getSimpleName());

		ImageCommand command;
		Operation op;
		Object[] args;

		public CommandInfo(ImageCommand command, Operation op, Object... args) {
			this.command = command;
			this.op = op;
			this.args = args;
		}

		public ProcessTask getProcessTask() {
			try {
				return command.getProcessTask(op, args);
			} catch (Exception e) {
				if (command.getErrorText() != null && command.getErrorText().size() > 0) {
					StringBuilder error = new StringBuilder();
					for (String txt : command.getErrorText()) {
						error.append(txt);
						error.append("\n");
					}
					logger.error("GM 运行失败. Log:" + error);
				}
				logger.error("GM 运行失败", e);
			}
			return null;
		}

		public void run() {
			try {
				command.run(op, args);
			} catch (Exception e) {
				if (command.getErrorText() != null && command.getErrorText().size() > 0) {
					StringBuilder error = new StringBuilder();
					for (String txt : command.getErrorText()) {
						error.append(txt);
						error.append("\n");
					}
					logger.error("GM 运行失败. Log:" + error);
				}
				logger.error("GM 运行失败", e);
			}
		}
	}

	public static CommandInfo getZipCommand(String srcFile, String destFile, int new_w, int new_h, boolean overwrite) {
		File file = CreateFile(destFile);
		if (file.exists()) {
			if (!overwrite)
				return null;
		}
		try {
			IMOps op = new IMOperation();
			op.addImage();
			BufferedImage srcImg = new JpegReader().readImage(new File(srcFile));
			int width = srcImg.getWidth(null);
			int height = srcImg.getHeight(null);
			new_w = Math.min(new_w, width);
			if (new_h <= 0) {
				op.resize(Math.min(new_w, width), Math.min(new_w, width) * height / width);
			} else {
				op.resize(Math.min(new_w, width), Math.min(new_h, height));
			}
			op.addImage();
			ConvertCmd cmd = new ConvertCmd(true);
			if (!StringUtility.isNullOrEmpty(gmPath)) {
				cmd.setSearchPath(gmPath);
			}
			return new CommandInfo(cmd, op, srcFile, destFile);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static CommandInfo getWatermarkCommand(String srcFile, String press) {
		File file = CreateFile(srcFile);
		if (!file.exists()) {
			return null;
		}
		try {
			BufferedImage srcImg = new JpegReader().readImage(new File(srcFile));
			int width = srcImg.getWidth(null);
			int height = srcImg.getHeight(null);
			BufferedImage pressImg = new JpegReader().readImage(new File(press));
			int pressWidth = pressImg.getWidth(null);
			int pressHeight = pressImg.getHeight(null);
			int pw = (width - pressWidth) * 9 / 10;
			int ph = (height - pressHeight) * 9 / 10;
			IMOps op = new IMOperation();
			op.geometry(pressWidth, pressHeight, pw, ph);
			op.addImage();
			op.addImage();
			op.addImage();
			CompositeCmd cmd = new CompositeCmd(true);
			if (!StringUtility.isNullOrEmpty(gmPath)) {
				cmd.setSearchPath(gmPath);
			}
			return new CommandInfo(cmd, op, press, srcFile, srcFile);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static CommandInfo getCropCommand(String srcFile, String destFile, float widthHeightRate, int maxWidth, boolean overwrite) {
		File file = CreateFile(destFile);
		if (file.exists()) {
			if (!overwrite)
				return null;
		}
		try {
			BufferedImage srcImg = new JpegReader().readImage(new File(srcFile));
			if (srcImg == null) {
				System.out.println(srcFile + " cannot be open.");
				return null;
			}
			int width = srcImg.getWidth(null);
			int height = srcImg.getHeight(null);
			int x1, x2, y1, y2;
			if (width / (float) height == widthHeightRate) {
				return getZipCommand(srcFile, destFile, maxWidth, 0, overwrite);
			} else if (width / (float) height > widthHeightRate) {
				x1 = Math.round((width - height * widthHeightRate) / 2);
				x2 = width - x1;
				y1 = 0;
				y2 = height;
			} else {
				y1 = Math.round((height - width / widthHeightRate) / 2);
				y2 = height - y1;
				x1 = 0;
				x2 = width;
			}
			return getCropCommand(srcFile, destFile, x1, y1, x2 - x1, y2 - y1, maxWidth);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static CommandInfo getCropCommand(String srcFile, String destFile, int cropButtomHeight, boolean overwrite) {
		File file = CreateFile(destFile);
		if (file.exists()) {
			if (!overwrite)
				return null;
		}
		try {
			BufferedImage srcImg = new JpegReader().readImage(new File(srcFile));
			if (srcImg == null) {
				System.out.println(srcFile + " cannot be open.");
				return null;
			}
			int height = srcImg.getHeight(null);
			if (cropButtomHeight >= height)
				return null;
			int width = srcImg.getWidth(null);
			return getCropCommand(srcFile, destFile, 0, 0, width, height - cropButtomHeight, 0);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static CommandInfo getSubImageCommand(String srcFile, String destFile, int x, int y) {
		try {
			BufferedImage srcImg = new JpegReader().readImage(new File(srcFile));
			int w = srcImg.getWidth() - 2 * x;
			int h = srcImg.getHeight() - 2 * y;
			IMOps op = new IMOperation();
			op.addImage();
			op.crop(w, h, x, y);
			op.addImage();
			ConvertCmd cmd = new ConvertCmd(true);
			if (!StringUtility.isNullOrEmpty(gmPath)) {
				cmd.setSearchPath(gmPath);
			}
			return new CommandInfo(cmd, op, srcFile, destFile);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	public static CommandInfo getCropCommand(String srcFile, String destFile, int x, int y, int w, int h, int maxWidth) {
		if (maxWidth > w) {
			maxWidth = 0;
		}
		IMOps op = new IMOperation();
		op.addImage();
		op.crop(w, h, x, y).resize(maxWidth);
		op.addImage();
		ConvertCmd cmd = new ConvertCmd(true);
		if (!StringUtility.isNullOrEmpty(gmPath)) {
			cmd.setSearchPath(gmPath);
		}
		return new CommandInfo(cmd, op, srcFile, destFile);

	}

	public static int getRatateDegree(String file) {
		try {
			Metadata metadata = JpegMetadataReader.readMetadata(new File(file));
			for (Directory directory : metadata.getDirectories()) {
				if (directory.containsTag(ExifDirectoryBase.TAG_ORIENTATION)) {
					// Exif信息中方向　　
					int orientation = directory.getInt(ExifDirectoryBase.TAG_ORIENTATION);
					switch (orientation) {
					case 3:
						return 180;
					case 6:
						return 90;
					case 8:
						return 270;
					default:
						break;
					}
				}
			}
		} catch (ImageProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MetadataException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static CommandInfo getRatateCommand(String srcFile) {
		int degree = 0 - getRatateDegree(srcFile);
		return getRatateCommand(srcFile, srcFile, degree);
	}

	public static CommandInfo getRatateCommand(String srcFile, String destFile, double degree) {
		// 1.将角度转换到0-360度之间
		degree = degree % 360;
		if (degree <= 0) {
			degree = 360 + degree;
		}
		IMOperation op = new IMOperation();
		op.addImage();
		op.rotate(degree);
		op.addImage();
		ConvertCmd cmd = new ConvertCmd(true);
		return new CommandInfo(cmd, op, srcFile, destFile);

	}

	private static File CreateFile(String destFile) {
		if (!destFile.endsWith(".jpg")) {
			destFile = destFile.substring(0, destFile.lastIndexOf('.')) + ".jpg";
		}
		File tmp = new File(destFile);
		if (!tmp.getParentFile().exists()) {
			tmp.getParentFile().mkdirs();
		}
		return tmp;
	}
}