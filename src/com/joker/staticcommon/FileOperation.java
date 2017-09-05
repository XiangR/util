package com.joker.staticcommon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * 
 * @author xiangR
 * @date 2017年7月28日上午9:47:28
 *
 */
public class FileOperation {

	static Logger logger = LogManager.getLogger(FileOperation.class.getName());

	public static void overwriteFileText(String fileName, String content) {
		File file = new File(fileName);
		if (file.exists()) {
			file.delete();
		}
		FileOperation.appendFileText(fileName, content);
	}

	public static void appendFileText(String fileName, String content) {
		if (StringUtility.isNullOrEmpty(content)) {
			return;
		}
		Writer out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true), "UTF-8"));
			out.write(content);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String readFileToString(String ProductDateFile) {
		try {
			File file = new File(ProductDateFile);
			if (file.exists())
				return FileUtils.readFileToString(file, "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void copyFileByReleatePath(String src, String dest) {
		copyFile(RunTimeConfig.getRealPath(src), RunTimeConfig.getRealPath(dest));
	}

	public static void copyFile(String src, String dest) {
		copyFile(src, dest, true);
	}

	public static void copyFile(String src, String dest, boolean overwrite) {
		File srcFile = new File(src);
		if (srcFile.exists() && srcFile.isFile()) {
			File destFile = new File(dest);
			if (!destFile.exists()) {
				File folder = destFile.getParentFile();
				if (!folder.exists()) {
					folder.mkdirs();
				}
			} else if (overwrite) {
				destFile.delete();
			} else {
				return;
			}
			try {
				Files.copy(Paths.get(src), Paths.get(dest), StandardCopyOption.COPY_ATTRIBUTES);
			} catch (IOException e) {
				logger.error(String.format("source file:%s,dest file:%s", src, dest), e);
				e.printStackTrace();
			}
		}
	}

	public static void moveFileByReleatePath(String src, String dest) {
		moveFile(RunTimeConfig.getRealPath(src), RunTimeConfig.getRealPath(dest));
	}

	public static void moveFile(String src, String dest) {
		moveFile(src, dest, true);
	}

	public static void moveFile(String src, String dest, boolean overwrite) {
		File srcFile = new File(src);
		if (srcFile.exists() && srcFile.isFile()) {
			File destFile = new File(dest);
			if (!destFile.exists()) {
				File folder = destFile.getParentFile();
				if (!folder.exists()) {
					folder.mkdirs();
				}
			} else if (overwrite) {
				destFile.delete();
			} else {
				return;
			}
			try {
				try {
					Files.move(Paths.get(src), Paths.get(dest), StandardCopyOption.ATOMIC_MOVE);
				} catch (AtomicMoveNotSupportedException ex) {
					Files.copy(Paths.get(src), Paths.get(dest), StandardCopyOption.COPY_ATTRIBUTES);
					srcFile.delete();
				}
			} catch (IOException e) {
				logger.error(String.format("source file:%s,dest file:%s", src, dest), e);
				e.printStackTrace();
			}
		}
	}

	public static void deleteFile(String path) {
		File file = new File(path);
		if (file.exists() && file.isFile()) {
			file.delete();
		}
	}

	/**
	 * 拿到指定目录下的所有以 jpg 结尾的文件名
	 * 
	 * @param strPath
	 */
	public static void getFileList(String strPath) {
		File dir = new File(strPath);
		File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (files[i].isDirectory()) { // 判断是文件还是文件夹
					getFileList(files[i].getAbsolutePath()); // 获取文件绝对路径
				} else if (fileName.endsWith("jpg")) { // 判断文件名是否以.avi结尾
					String strFileName = files[i].getAbsolutePath();
					System.out.println("---" + strFileName);
					String realPath = strFileName.replaceAll("\\\\", "/");
					String resultPath = realPath.substring(realPath.indexOf("/") + 1, realPath.length());
					appendFileTextLine("f:filePath.txt", strFileName);
					appendFileTextLine("f:realPath.txt", resultPath);
				} else {
					continue;
				}
			}
		}
	}

	public static void main(String[] args) {
		updateFileNames("f:Logos", null);
	}

	public static void updateFileNames(String parentFilePath, String index) {// 更改文件夹下所有文件的的名称
		File file = new File(parentFilePath);
		if (file.exists() && file.isDirectory()) {
			File[] childFiles = file.listFiles();
			String path = file.getAbsolutePath();
			for (File childFile : childFiles) {
				// 如果是文件
				if (childFile.isFile()) {
					String oldName = childFile.getName();
					String newName = oldName.replace("@2x", "");
					// String newName =
					// oldName.substring(oldName.indexOf(index));
					logger.info(oldName + "-> " + newName);
					childFile.renameTo(new File(path + "\\" + newName));
				}
			}
		}
	}

	public static List<String> getFileNames(String parentFilePath) {// 更改文件夹下所有文件的的名称
		List<String> list = new ArrayList<String>();
		File file = new File(parentFilePath);
		if (file.exists() && file.isDirectory()) {
			File[] childFiles = file.listFiles();
			for (File childFile : childFiles) {
				// 如果是文件
				if (childFile.isFile()) {
					String oldName = childFile.getName();
					list.add(oldName);
				}
			}
		}
		return list;
	}

	/**
	 * 插入文件并 newLine
	 * 
	 * @param fileName
	 *            文件名
	 * @param content
	 *            文字
	 */
	public static void appendFileTextLine(String fileName, String content) {
		if (content == null || content.length() == 0) {
			return;
		}
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true), "UTF-8"));
			out.newLine();
			out.write(content);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 按行读取文件
	 * 
	 * @param fileName
	 * @return
	 */
	public static List<String> readFileWithLines(String fileName) {
		List<String> result = new ArrayList<String>();
		// BufferedReader 是可以按行读取文件
		FileInputStream inputStream = null;
		BufferedReader bufferedReader = null;
		try {
			inputStream = new FileInputStream(fileName);
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				if (!StringUtility.isNullOrEmpty(str)) {
					result.add(str);
				}
			}
			inputStream.close();
			bufferedReader.close();
		} catch (Exception e) {

		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static void exportExcel(String title, List<String> headers, List<List<String>> datas, HttpServletResponse response) {
		String fileName = title + "导出" + TimeUtility.toString("yyyyMMddHHmmss", new Date()) + ".xls";
		HSSFWorkbook wb = null;
		try {
			wb = new HSSFWorkbook();
			response.setContentType("application/vnd.ms-excel;charset=utf-8");
			response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
			HSSFSheet sheet = wb.createSheet(title + "记录");
			// 4.创建单元格，设置值表头，设置表头居中
			HSSFCellStyle style = wb.createCellStyle();
			// 居中格式
			style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

			// 3.在sheet中添加表头第0行，老版本poi对excel行数列数有限制short
			HSSFRow row = sheet.createRow((int) 0);
			HSSFCell cell = null;
			for (int i = 0; i < headers.size(); ++i) {
				cell = row.createCell(i);
				cell.setCellValue(headers.get(i));
				cell.setCellStyle(style);
			}
			for (int i = 0; i < datas.size(); ++i) {
				row = sheet.createRow((int) i + 1);
				List<String> data = datas.get(i);
				for (int j = 0; j < data.size(); ++j) {
					row.createCell(j).setCellValue(data.get(j));
				}
			}
			wb.write(response.getOutputStream());
			response.getOutputStream().close();
		} catch (Exception e) {
			logger.info("=====导出excel异常====");
		} finally {
			if (wb != null) {
				try {
					wb.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
