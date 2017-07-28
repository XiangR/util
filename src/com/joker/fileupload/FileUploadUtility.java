package com.joker.fileupload;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.joker.enumcommon.PhotoType;
import com.joker.model.Model;
import com.joker.staticcommon.FileOperation;
import com.joker.staticcommon.StringUtility;

/**
 * 
 * @author xiangR
 * @date 2017年7月28日上午9:47:11
 *
 */
public class FileUploadUtility {

	Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

	static {
		File tempPath = new File(RunTimeConfig.getRealPath(PhotoType.getPath(PhotoType.TEMP)));
		if (!tempPath.exists()) {
			tempPath.mkdirs();
		}
	}

	/**
	 * 通过request来上传文件
	 * 
	 * @param request
	 * @return
	 */
	public Map<Object, Object> commonFile(HttpServletRequest request) {
		Map<Object, Object> map = new HashMap<Object, Object>();
		try {
			if (!(request instanceof MultipartHttpServletRequest)) {
				map.put("success", 1);
			} else {
				MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
				MultipartFile file = multipartRequest.getFile("file");
				String category = request.getParameter("category");
				if (file != null) {
					String name = request.getParameter("name");
					if (StringUtility.isNullOrEmpty(name)) {
						name = "" + new Date().getTime() + (int) Math.ceil(Math.random() * 1000) + ".jpg";
					}
					String dstPath = RunTimeConfig.getRealPath(PhotoType.getPath(PhotoType.TEMP)) + "/" + name;
					File dstFile = new File(dstPath);
					if (dstFile.exists()) {
						dstFile.delete();
						dstFile = new File(dstPath);
					}
					copy(file.getInputStream(), dstFile);
					PhotoUtility.uploadCommonPhoto(dstFile, category, null, null);
					uploadCommon(category, null, null, multipartRequest);
					map.put("success", 0);
					map.put("name", name);
				} else {
					map.put("success", 1);
				}
			}
		} catch (Exception e) {
			map.put("success", 1);
			logger.error("上传文件错误", e);
		}
		return map;
	}

	/**
	 * 上传excel
	 * 
	 * @param request
	 * @return
	 */
	public Map<Object, Object> commonFileExcel(HttpServletRequest request) {
		Map<Object, Object> map = new HashMap<Object, Object>();
		try {
			if (!(request instanceof MultipartHttpServletRequest)) {
				map.put("success", 1);
			} else {
				MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
				MultipartFile file = multipartRequest.getFile("file");
				if (file != null) {
					String name = request.getParameter("name");
					if (StringUtility.isNullOrEmpty(name)) {
						name = file.getOriginalFilename();
					}
					String dstPath = RunTimeConfig.getRealPath(PhotoType.getPath(PhotoType.EXCEL)) + "/" + name;
					File dstFile = new File(dstPath);
					// 文件已存在（上传了同名的文件）
					if (dstFile.exists()) {
						dstFile.delete();
						dstFile = new File(dstPath);
					}
					copy(file.getInputStream(), dstFile);
					map.put("success", 0);
				} else {
					map.put("success", 1);
				}
			}
		} catch (IOException e) {
			map.put("success", 1);
		}
		return map;
	}

	/**
	 * 支持上传文件到UEditor
	 * 
	 * @param request
	 * @return
	 */
	public Object raidersPhotoUpload(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			if (!(request instanceof MultipartHttpServletRequest)) {
				map.put("success", 1);
			} else {
				MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
				MultipartFile file = multipartRequest.getFile("upfile");
				String category = "raiders";
				String name = "" + new Date().getTime() + (int) Math.ceil(Math.random() * 1000) + ".jpg";
				if (file != null) {
					String dstPath = RunTimeConfig.getRealPath(PhotoType.getPath(PhotoType.TEMP)) + "/" + name;
					File dstFile = new File(dstPath);
					// 文件已存在（上传了同名的文件）
					if (dstFile.exists()) {
						dstFile.delete();
						dstFile = new File(dstPath);
					}
					copy(file.getInputStream(), dstFile);
					PhotoUtility.uploadRaidersPhotoCloud(dstFile, category, null, null);
					map.put("state", "SUCCESS");
					// map.put("url", "photos/raiders/img/" + name);
					map.put("url", "http://honghuworld.s3.amazonaws.com/photos/raiders/img/" + name);
					map.put("size", file.getSize());
					map.put("original", file.getOriginalFilename());
					map.put("title", file.getName());
					map.put("type", file.getContentType());
				} else {
					map.put("success", 1);
				}
			}
		} catch (Exception e) {
			map.put("success", 1);
		}
		return map;
	}

	/**
	 * 由List 生成excel
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public Map<Object, Object> export(HttpServletRequest request, HttpServletResponse response, List<Model> list) {
		list = new ArrayList<Model>();
		List<List<String>> datas = list.stream().map(item -> {
			ArrayList<String> data = new ArrayList<String>();
			data.add(item.getId() == null ? null : item.getId().toString());
			data.add(item.getModule_id_name());
			data.add(item.getMenuName() == null ? null : item.getMenuName().toString());
			data.add(item.getModelName() == null ? null : item.getModelName().toString());
			data.add(item.getIdx() == null ? null : item.getIdx().toString());
			return data;
		}).collect(Collectors.toList());

		List<String> headers = java.util.Arrays.asList("编号", "大模块", "菜单名称", "模块名称", "排序", "状态");
		FileOperation.exportExcel("后台模块", headers, datas, response);
		return null;
	}

	/**
	 * plupload/photo/aws
	 * 
	 * @param request
	 * @return
	 */
	public Object raidersPhotoUploadCommo(HttpServletRequest request) {
		Map<Object, Object> map = new HashMap<Object, Object>();
		try {
			if (!(request instanceof MultipartHttpServletRequest)) {
				map.put("success", 1);
			} else {
				MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
				MultipartFile file = multipartRequest.getFile("file");
				String category = request.getParameter("category");
				String name = "" + new Date().getTime() + (int) Math.ceil(Math.random() * 1000) + ".jpg";
				if (file != null) {
					String dstPath = RunTimeConfig.getRealPath(PhotoType.getPath(PhotoType.TEMP)) + "/" + name;
					File dstFile = new File(dstPath);
					// 文件已存在（上传了同名的文件）
					if (dstFile.exists()) {
						dstFile.delete();
						dstFile = new File(dstPath);
					}
					copy(file.getInputStream(), dstFile);
					PhotoUtility.uploadRaidersPhotoCloud(dstFile, category, null, null);
				} else {
					map.put("success", 1);
				}
				map.put("success", 0);
				map.put("name", "http://honghuworld.s3.amazonaws.com/photos/" + category + "/img/" + name);
			}
		} catch (Exception e) {
			map.put("success", 1);
			e.printStackTrace();
		}
		return map;
	}

	private String uploadCommon(String categroy, String path, String id, MultipartHttpServletRequest request) {

		int chunk = 0;
		int chunks = 1;
		try {
			File completedFile = uploadMedia(chunk, chunks, request);
			if (completedFile != null) {
				PhotoUtility.uploadCommonPhoto(completedFile, categroy, path, id);
			}
		} catch (Exception e) {
			logger.error("Process error.", e);
			e.printStackTrace();
		}
		return "pages/common/head";
	}

	public File uploadMedia(int chunk, int chunks, MultipartHttpServletRequest request) {
		try {
			MultipartFile file = request.getFile("file");
			if (file != null) {
				String name = request.getParameter("name");
				if (StringUtility.isNullOrEmpty(name)) {
					name = file.getOriginalFilename();
				}
				String dstPath = RunTimeConfig.getRealPath(PhotoType.getPath(PhotoType.TEMP)) + "/" + name;
				File dstFile = new File(dstPath);

				// 文件已存在（上传了同名的文件）
				if (chunk == 0 && dstFile.exists()) {
					dstFile.delete();
					dstFile = new File(dstPath);
				}
				copy(file.getInputStream(), dstFile);
				if (chunk == chunks - 1) {
					return dstFile;// 完成一整个文件;
				}
			}
		} catch (IOException e) {
			logger.error("upload file save error.", e);
			/*
			 * 曾经这里在catch到异常之后 抛出到上层
			 * 上层uploadMedia catch 到异常时继续抛出
			 * 保留旧代码的实现所以在这里注释掉
			 * 
			 * throw e;
			 */
		}
		return null;
	}

	private static final int BUFFER_SIZE = 256 * 1024;

	private void copy(InputStream in, File dst) {
		OutputStream out = null;
		try {
			if (dst.exists()) {
				out = new BufferedOutputStream(new FileOutputStream(dst, true), BUFFER_SIZE);
			} else {
				if (!dst.getParentFile().exists()) {
					dst.getParentFile().mkdirs();
				}
				out = new BufferedOutputStream(new FileOutputStream(dst), BUFFER_SIZE);
			}
			byte[] buffer = new byte[BUFFER_SIZE];
			int len = 0;
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
		} catch (Exception e) {
			logger.error("Process error.", e);
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error("Process error.", e);
					e.printStackTrace();
				}
			}
			if (null != out) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error("Process error.", e);
					e.printStackTrace();
				}
			}
		}
	}

}
