package com.joker.fileupload;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.joker.enumcommon.PhotoSize;
import com.joker.enumcommon.PhotoType;
import com.joker.fileupload.PhotoProcessUtil.Internal.ImageProcessI;
import com.joker.staticcommon.AmazonAWSUtility;
import com.joker.staticcommon.FileOperation;
import com.joker.staticcommon.RunTimeConfig;

public class PhotoUtility {
	static Logger logger = LogManager.getLogger(PhotoUtility.class.getSimpleName());

	public static void uploadCommonPhoto(File file, String type, String subPath, String id) {
		if (file == null) {
			return;
		}
		List<File> photos = new ArrayList<File>();
		List<ImageProcessI> processes = new ArrayList<ImageProcessI>();
		String relativePath = moveCommonOriginalFile(file, type, subPath, id, photos);
		String[] pathArr = relativePath.split("/");
		
		if (pathArr[1].equals("common_config")) {
			processes = PhotoProcessUtil.Internal.getCommonBannerProcessor();
		} else {
			processes = PhotoProcessUtil.Internal.getCommonProcessor();
		}
		PhotoProcessUtil.processPhotos(photos, processes, true);
		String url = ConfigParser.getCommonProperty("imgServerPath") + relativePath.substring(relativePath.indexOf("/photos/") + 1);
		waitPhotoSync(url, 10);
	}

	public static void uploadActivityTeamPhoto(File file, String type, String subPath, String id) {
		if (file == null) {
			return;
		}
		List<File> photos = new ArrayList<File>();
		String relativePath = moveCommonOriginalFile(file, type, subPath, id, photos);
		List<ImageProcessI> processes = PhotoProcessUtil.Internal.getActivityTeamProcessor();
		PhotoProcessUtil.processPhotos(photos, processes, true);
		String url = ConfigParser.getCommonProperty("imgServerPath") + relativePath.substring(relativePath.indexOf("/photos/") + 1);
		waitPhotoSync(url, 10);
	}

	private static String moveCommonOriginalFile(File file, String type, String subPath, String id, List<File> photos) {
		String parentPath = PhotoType.getPath(PhotoType.PARENT) + type;
		return moveOriginalFile(file, parentPath, subPath, id, photos);
	}

	private static String moveOriginalFile(File file, String parentPath, String subPath, String id, List<File> photos) {
		String realName = file.getName();
		// realName = realName.substring(0, realName.lastIndexOf(".")) + ".jpg";
		if (subPath == null) {
			subPath = "";
		}
		subPath = subPath.trim();
		if (subPath.length() > 0) {
			subPath = "/" + subPath;
		}
		String relativePath = String.format("%s%s/%s%s/%s", parentPath, subPath, PhotoSize.ORIGINAL.getPath(), id == null ? "" : "/" + id, realName);
		String originalPath = getAbsolutePath(relativePath);
		FileOperation.moveFile(file.getAbsolutePath(), originalPath);
		photos.add(new File(originalPath));
		return relativePath;
	}

	public static void movePhotos(String originalName, String realName, Integer userId, Integer originalUserId) {
		String parentPath = getAbsolutePath(String.format("%s/%s/%d/%s", "photos/users/certificate", PhotoSize.FULL.getPath(), originalUserId, originalName));
		String destPath = getAbsolutePath(String.format("%s/%s/%d/%s", "photos/users/certificate", PhotoSize.FULL.getPath(), userId, realName));
		FileOperation.moveFile(parentPath, destPath);
	}

	private static String getAbsolutePath(String relatePath) {
		return RunTimeConfig.getRealPath(relatePath);
	}

	public static boolean waitPhotoSync(String realUrl, int maxSeconds) {
		while (--maxSeconds > 0) {
			try {
				URL url = new URL(realUrl);
				URLConnection conn = url.openConnection();
				conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
				InputStream inputStream = conn.getInputStream(); // 通过输入流获得图片数据
				inputStream.close();
				logger.info(String.format("%s is avaible after %d second left.", realUrl, maxSeconds));
				return true;
			} catch (Exception ex) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		logger.error(String.format("%s not available after %d second.", realUrl, maxSeconds));
		return false;
	}

	/* 发布文件到云端 */
	public static void uploadRaidersPhotoCloud(File file, String type, String subPath, String id) {
		if (file == null) {
			return;
		}
		List<File> photos = new ArrayList<File>();
		String relativePath = moveCommonOriginalFile(file, type, subPath, id, photos);
		List<ImageProcessI> processes = PhotoProcessUtil.Internal.getActivityTeamProcessor();
		PhotoProcessUtil.processPhotos(photos, processes, true);
		uploadToAWSCload(processes, relativePath);
	}

	/**
	 * 上传图片到 Amazon AWS
	 * 先使用process,relativePath拿到切图后上传到服务器的所有文件路径
	 * 再将这些文件上传到AWS
	 * 最后删除服务器中的文件
	 * 
	 * @param processes 切图后的文件
	 * @param relativePath 真实地址
	 * @return
	 */
	public static List<String> uploadToAWSCload(List<ImageProcessI> processes, String relativePath) {
		List<String> paths = new ArrayList<String>();
		for (ImageProcessI processe : processes) {
			String type = processe.getType();
			String imgPath = relativePath.replace("original", type);
			paths.add(imgPath);
		}
		// 加上本身original
		// paths.add(relativePath);

		for (String imgPath : paths) {
			String realPath = getAbsolutePath(imgPath);
			logger.info(String.format("上传云端key -> %s, 本地文件 -> %s", imgPath, realPath));
			AmazonAWSUtility.uploadFile(imgPath, realPath);
		}

		// 删除本地文件
		// for (String imgPath : paths) {
		// FileOperation.deleteFile(getAbsolutePath(imgPath));
		// }
		return paths;
	}

}
