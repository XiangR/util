package com.joker.fileupload;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.im4java.process.ProcessExecutor;

import com.joker.fileupload.ImageSizer.CommandInfo;
import com.joker.fileupload.PhotoProcessUtil.Internal.ImageProcessI;

@SuppressWarnings("rawtypes")
public class PhotoProcessUtil {
	static Logger logger = LogManager.getLogger(PhotoProcessUtil.class.getSimpleName());

	public static void processPhotos(Iterable photos, Boolean notWait, List<ImageProcessI> processes) {
		if (notWait == null || !notWait) {
			processPhotos(photos, processes, Internal.overwrite);
		} else {
			List<ImageProcessI> needWait = new ArrayList<ImageProcessI>();
			List<ImageProcessI> notNeedWait = new ArrayList<ImageProcessI>();
			for (ImageProcessI process : processes) {
				if (process.getNeedWait()) {
					needWait.add(process);
				} else {
					notNeedWait.add(process);
				}
			}
			processPhotos(photos, needWait, Internal.overwrite);
			ThreadProcess process = new ThreadProcess(photos, notNeedWait, Internal.overwrite);
			new Thread(process).start();
		}
	}

	public static void processPhotos(Iterable photos, List<ImageProcessI> processes, boolean overwrite) {
		if (photos == null || processes == null) {
			return;
		}
		for (ImageProcessI process : processes) {
			logger.info(String.format("%tT 开始处理：%s", new Date(), process.getDetail()));
			ProcessExecutor exec = new ProcessExecutor(Internal.threadCount);
			try {
				for (Object photo : photos) {
					CommandInfo pt = null;
					if (photo instanceof File) {
						pt = process.process((File) photo, overwrite);
					} else if (photo instanceof Entry) {
						@SuppressWarnings("unchecked")
						Entry<Integer, File> pair = (Entry<Integer, File>) photo;
						pt = process.process(pair.getKey(), pair.getValue(), overwrite);
					}
					if (null == pt) {
						continue;
					}
					exec.execute(pt.getProcessTask());
					if (exec.getTaskCount() % 1000 == 0)
						logger.info(String.format("%tT %d of %d added！ %s", new Date(), exec.getCompletedTaskCount(), exec.getTaskCount(), process.getDetail()));
				}
				exec.shutdown();
				while (true) {
					logger.info(String.format("%tT %d of %d running！ %s", new Date(), exec.getCompletedTaskCount(), exec.getTaskCount(), process.getDetail()));
					if (exec.awaitTermination(60, TimeUnit.SECONDS)) {
						break;
					}
				}
			} catch (Exception e) {
				logger.error("图片处理失败！=========================================================", e);
			}
			logger.info(String.format("%tT 处理完毕！ %s", new Date(), process.getDetail()));
		}
	}

	private static class ThreadProcess implements Runnable {
		Iterable photos;
		List<ImageProcessI> processes;
		boolean overwrite;

		public ThreadProcess(Iterable photos, List<ImageProcessI> processes, boolean overwrite) {
			this.photos = photos;
			this.processes = processes;
			this.overwrite = overwrite;
		}

		@Override
		public void run() {
			processPhotos(photos, processes, overwrite);
		}

	}

	public static class Internal {
		public static Boolean overwrite = true;
		public static String originalSymbol = "/original/";
		public static Integer threadCount = 8;

		public static interface ImageProcessI {
			public String getDetail();

			public String getType();

			public Boolean getNeedWait();

			public void setType(String type);

			public CommandInfo process(File photo, boolean overwrite);

			public CommandInfo process(Integer id, File photo, boolean overwrite);
		}

		public static abstract class ImageProcessBase implements ImageProcessI {
			protected String type;
			private Boolean needWait;

			protected ImageProcessBase(String type, Boolean needWait) {
				this.setType(type);
				this.setNeedWait(needWait);
			}

			public String getType() {
				return type;
			}

			public void setType(String type) {
				this.type = type;
			}

			public Boolean getNeedWait() {
				return needWait;
			}

			public void setNeedWait(Boolean needWait) {
				this.needWait = needWait;
			}
		}

		public static class ZipProcess extends ImageProcessBase {
			int maxWidth;

			int maxHeight;

			public int getMaxWidth() {
				return maxWidth;
			}

			public void setMaxWidth(int maxWidth) {
				this.maxWidth = maxWidth;
			}

			public int getMaxHeight() {
				return maxHeight;
			}

			public void setMaxHeight(int maxHeight) {
				this.maxHeight = maxHeight;
			}

			public ZipProcess(String type, int maxWidth, int maxHeight) {
				this(type, true, maxWidth, maxHeight);
			}

			public ZipProcess(String type, Boolean needWait, int maxWidth, int maxHeight) {
				super(type, needWait);
				this.maxWidth = maxWidth;
				this.maxHeight = maxHeight;
			}

			@Override
			public String getDetail() {
				return String.format("压缩图片%s，最大宽度:%d，最大高度:%d", type, maxWidth, maxHeight);
			}

			@Override
			public CommandInfo process(File photo, boolean overwrite) {
				return process(null, photo, overwrite);
			}

			@Override
			public CommandInfo process(Integer id, File photo, boolean overwrite) {
				String originalFile = photo.getAbsolutePath().replace('\\', '/');
				String destPath = originalFile.replace(originalSymbol, "/" + type + "/");
				if (id != null) {
					destPath = destPath.substring(0, destPath.lastIndexOf("/") + 1) + id + ".jpg";
				} else if (!destPath.endsWith(".jpg")) {
					destPath = destPath.substring(0, destPath.lastIndexOf(".")) + ".jpg";
				}
				return ImageSizer.getZipCommand(originalFile, destPath, maxWidth, maxHeight, overwrite);
			}
		}

		public static class PressProcess extends ImageProcessBase {
			String pressName;

			public PressProcess(String type, String pressName) {
				this(type, true, pressName);
			}

			public PressProcess(String type, Boolean needWait, String pressName) {
				super(type, needWait);
				this.pressName = pressName;
			}

			@Override
			public String getDetail() {
				return String.format("增加水印%s，水印文件:%s", type, pressName);
			}

			@Override
			public CommandInfo process(File photo, boolean overwrite) {
				return process(null, photo, overwrite);
			}

			@Override
			public CommandInfo process(Integer id, File photo, boolean overwrite) {
				String originalFile = photo.getAbsolutePath().replace('\\', '/');
				String basePath = photo.getParentFile().getParentFile().getParent().replace('\\', '/') + "/";
				String waterMark = basePath + pressName;
				String destPath = originalFile.replace(originalSymbol, "/" + type + "/");
				if (id != null) {
					destPath = destPath.substring(0, destPath.lastIndexOf("/") + 1) + id + ".jpg";
				} else if (!destPath.endsWith(".jpg")) {
					destPath = destPath.substring(0, destPath.lastIndexOf(".")) + ".jpg";
				}
				return ImageSizer.getWatermarkCommand(destPath, waterMark);
			}
		}

		public static class CropProcess extends ImageProcessBase {
			int maxWidth;

			float rate;

			public int getMaxWidth() {
				return maxWidth;
			}

			public void setMaxWidth(int maxWidth) {
				this.maxWidth = maxWidth;
			}

			public CropProcess(String type, float rate, int maxWidth) {
				this(type, true, rate, maxWidth);
			}

			public CropProcess(String type, Boolean needWait, float rate, int maxWidth) {
				super(type, needWait);
				this.maxWidth = maxWidth;
				this.rate = rate;
			}

			@Override
			public String getDetail() {
				return String.format("剪切图片%s，最大宽度:%d，比例:%f", type, maxWidth, rate);
			}

			@Override
			public CommandInfo process(File photo, boolean overwrite) {
				return process(null, photo, overwrite);
			}

			@Override
			public CommandInfo process(Integer id, File photo, boolean overwrite) {
				String originalFile = photo.getAbsolutePath().replace('\\', '/');
				String destPath = originalFile.replace(originalSymbol, "/" + type + "/");
				if (id != null) {
					destPath = destPath.substring(0, destPath.lastIndexOf("/") + 1) + id + ".jpg";
				} else if (!destPath.endsWith(".jpg")) {
					destPath = destPath.substring(0, destPath.lastIndexOf(".")) + ".jpg";
				}
				return ImageSizer.getCropCommand(originalFile, destPath, rate, maxWidth, overwrite);
			}
		}

		public static class CropWaterMarkProcess extends ImageProcessBase {
			int buttomHeight;

			public CropWaterMarkProcess(String type, int buttomHeight) {
				this(type, true, buttomHeight);
			}

			public CropWaterMarkProcess(String type, Boolean needWait, int buttomHeight) {
				super(type, needWait);
				this.buttomHeight = buttomHeight;
			}

			@Override
			public String getDetail() {
				return String.format("剪切图片%s，水印高度:%d", type, buttomHeight);
			}

			@Override
			public CommandInfo process(File photo, boolean overwrite) {
				return process(null, photo, overwrite);
			}

			@Override
			public CommandInfo process(Integer id, File photo, boolean overwrite) {
				String originalFile = photo.getAbsolutePath().replace('\\', '/');
				String destPath = originalFile.replace(originalSymbol, "/" + type + "/").replace("//", "/");
				return ImageSizer.getCropCommand(originalFile, destPath, buttomHeight, overwrite);
			}
		}

		public static List<ImageProcessI> getCasePhotoProcessor() {
			List<ImageProcessI> processes = new ArrayList<ImageProcessI>();
			processes.add(new CropProcess("m", 1.333f, 450));
			processes.add(new CropProcess("m1", 1f, 253));
			processes.add(new CropProcess("m2", 1f, 126));
			processes.add(new ZipProcess("xl", 800, 800));
			processes.add(new PressProcess("xl", "watermark.png"));

			processes.add(new ZipProcess("full", false, 1440, 960));
			processes.add(new PressProcess("full", false, "watermark-big.png"));
			processes.add(new CropProcess("m0", false, 1f, 488));
			processes.add(new CropProcess("m0x", false, 1f, 406));
			processes.add(new CropProcess("m1x", false, 1f, 209));
			processes.add(new CropProcess("m2x", false, 1f, 104));
			processes.add(new CropProcess("m10", false, 346 / (239f), 346));
			processes.add(new CropProcess("m10x", false, 287 / (198f), 287));
			return processes;
		}

		public static List<ImageProcessI> getUserPhotoProcessor() {
			List<ImageProcessI> processes = new ArrayList<ImageProcessI>();
			processes.add(new ZipProcess("full", 500, 500));
			processes.add(new CropProcess("l", 1f, 140));
			processes.add(new CropProcess("m", 1f, 50));
			processes.add(new CropProcess("s", 1f, 30));
			processes.add(new CropProcess("m10", 2f, 200));
			// processes.add(new CropProcess("xs", 1f, 24));
			return processes;
		}

		public static List<ImageProcessI> getProductProcessor() {
			List<ImageProcessI> processes = new ArrayList<ImageProcessI>();
			processes.add(new CropProcess("full", 1f, 800));
			processes.add(new CropProcess("m0", 1f, 229));
			processes.add(new CropProcess("m1", 1f, 140));
			return processes;
		}

		public static List<ImageProcessI> getCompanyBannerProcessor() {
			List<ImageProcessI> processes = new ArrayList<ImageProcessI>();
			processes.add(new CropProcess("m1-2", 2f, 90));
			return processes;
		}

		public static List<ImageProcessI> getInfoWaterMarkProcessor() {
			List<ImageProcessI> processes = new ArrayList<ImageProcessI>();
			processes.add(new CropWaterMarkProcess("", 60));
			return processes;
		}

		public static List<ImageProcessI> getSuperVisionProcessor() {
			List<ImageProcessI> processes = new ArrayList<ImageProcessI>();
			processes.add(new CropProcess("m", 220 / 164f, 220));
			processes.add(new ZipProcess("full", 600, -1));
			return processes;
		}

		public static List<ImageProcessI> getProductStdProcessor() {
			List<ImageProcessI> processes = new ArrayList<ImageProcessI>();
			processes.add(new CropProcess("m480", 1f, 480));
			processes.add(new CropProcess("m245", 1f, 245));
			processes.add(new CropProcess("m90", 1f, 90));
			processes.add(new CropProcess("l510", 504 / 350f, 510));
			processes.add(new CropProcess("l400", 504 / 350f, 400));
			processes.add(new CropProcess("l220", 504 / 350f, 220));
			return processes;
		}

		public static List<ImageProcessI> getProductDeckProcessor() {
			List<ImageProcessI> processes = new ArrayList<ImageProcessI>();
			processes.add(new ZipProcess("l245", 245, -1));
			processes.add(new ZipProcess("l400", 400, -1));
			return processes;
		}

		public static List<ImageProcessI> getProductSymbolProcessor() {
			List<ImageProcessI> processes = new ArrayList<ImageProcessI>();
			processes.add(new CropProcess("m25", 1f, 25));
			processes.add(new ZipProcess("l25", 25, -1));
			return processes;
		}

		public static List<ImageProcessI> getCommonProcessor() {
			List<ImageProcessI> processes = new ArrayList<ImageProcessI>();
			processes.add(new CropProcess("full", 1f, 300));
			processes.add(new ZipProcess("img", 1140, 860));
			return processes;
		}

		public static List<ImageProcessI> getCommonBannerProcessor() {
			List<ImageProcessI> processes = new ArrayList<ImageProcessI>();
			processes.add(new CropProcess("full", 1f, 300));
			processes.add(new ZipProcess("img", 1140, 860));
			processes.add(new ZipProcess("banner", 1920, 600));
			return processes;
		}

		public static List<ImageProcessI> getActivityTeamProcessor() {
			List<ImageProcessI> processes = new ArrayList<ImageProcessI>();
			processes.add(new CropProcess("full", 1f, 300));
			processes.add(new CropProcess("img", 560 / 360f, 560));
			processes.add(new CropProcess("m1", 800 / 600f, 800));
			return processes;
		}

		public static List<ImageProcessI> getUserProcessor() {
			List<ImageProcessI> processes = new ArrayList<ImageProcessI>();
			processes.add(new CropProcess("full", 1f, 96));
			processes.add(new ZipProcess("zip", 800, 800));
			return processes;
		}

		public static List<ImageProcessI> getNormalProcessor() {
			List<ImageProcessI> processes = new ArrayList<ImageProcessI>();
			processes.add(new ZipProcess("l245", 245, -1));
			return processes;
		}
	}
}
