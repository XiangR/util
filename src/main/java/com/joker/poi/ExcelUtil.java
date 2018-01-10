package com.joker.poi;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.IntStream;

import com.joker.enumcommon.ExcelEnum;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import com.joker.staticcommon.StringUtility;

/**
 * excel 文件导入导出工具
 * <p>
 * mac 文件读取方式 -> "Users/xiangrui/.."
 *
 * @author xiangR
 * @date 2018年1月10日上午9:47:11
 */
public class ExcelUtil {

    static Logger logger = LogManager.getLogger(ExcelUtil.class.getName());

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static DecimalFormat df = new DecimalFormat("#");
    private static final int XLS_SHEET_MAX_ROWS = 65530;

    public static void main(String[] args) {

        String s = "/Users/xiangrui/normal/test_excel.xlsx";
        File file = new File(s);
        readExcel(file);
    }

    /**
     * 导出文档
     *
     * @param title    导出文件名称
     * @param headers  标题
     * @param dataList 内容
     */
    public static void exportExcel2(String title, List<String> headers, List<List<String>> dataList, ExcelEnum type) {
        String fileName = "导出" + title + "." + type.getType();
        logger.info(fileName);
        Workbook wb = null;
        try {
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
            wb = type == ExcelEnum.XLSX ? new XSSFWorkbook() : new HSSFWorkbook();
            Sheet sheet = wb.createSheet(title + "记录");
            // 4.创建单元格，设置值表头，设置表头居中
            CellStyle style = wb.createCellStyle();
            // 居中格式
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            // 3.在sheet中添加表头第0行，老版本poi对excel行数列数有限制short
            Row row = sheet.createRow(0);
            Cell cell;
            for (int i = 0; i < headers.size(); ++i) {
                cell = row.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(style);
            }
            refineRow(sheet, dataList);
            wb.write(new FileOutputStream(fileName));
        } catch (Exception e) {
            e.printStackTrace();
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

    /**
     * 导出文档
     *
     * @param title    导出文件名称
     * @param headers  标题
     * @param dataList 内容
     */
    public static void exportExcel(String title, List<String> headers, List<List<String>> dataList) {
        String fileName = title + "导出.xlsx";
        XSSFWorkbook wb = null;
        try {
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
            wb = new XSSFWorkbook();

            XSSFSheet sheet = wb.createSheet(title + "记录");
            // 4.创建单元格，设置值表头，设置表头居中
            XSSFCellStyle style = wb.createCellStyle();
            // 居中格式
            style.setAlignment(XSSFCellStyle.ALIGN_CENTER);

            // 3.在sheet中添加表头第0行，老版本poi对excel行数列数有限制short
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell;
            for (int i = 0; i < headers.size(); ++i) {
                cell = row.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(style);
            }
            refineRow(sheet, dataList);
            wb.write(new FileOutputStream(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (wb != null) {

                    wb.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Integer srcIndex = null;
    private static Integer destIndex = null;

    /**
     * 读取 Excel 数据
     *
     * @param file 文件
     */
    public static void readExcel(File file) {
        try {

            InputStream inputStream = new FileInputStream(file);
            String fileName = file.getName();
            Workbook wb = null;
            if (fileName.endsWith("xls")) {
                wb = new HSSFWorkbook(inputStream);// 解析 xls 格式
            } else if (fileName.endsWith("xlsx")) {
                wb = new XSSFWorkbook(inputStream);// 解析 xlsx 格式
            }
            if (wb == null || wb.getNumberOfSheets() == 0) {
                return;
            }
            Sheet sheet = wb.getSheetAt(0);// 第一个工作表
            Iterator<Row> iterator = sheet.iterator();
            Row title = iterator.next(); // 得到title
            for (int i = 0; i < title.getPhysicalNumberOfCells(); i++) {
                if (title.getCell(i).getStringCellValue().equals("name")) {
                    srcIndex = i;
                } else if (title.getCell(i).getStringCellValue().equals("src")) {
                    destIndex = i;
                }
            }
            if (srcIndex == null || destIndex == null) {
                return; // 格式不正确
            }
            Map<String, String> map = new HashMap<>();
            while (iterator.hasNext()) {
                Row row = iterator.next();
                int numberOfCells = row.getPhysicalNumberOfCells();
                if (numberOfCells >= destIndex && numberOfCells > srcIndex) {
                    Cell cellKey = row.getCell(srcIndex);
                    Cell cellValue = row.getCell(destIndex);
                    String key = getCellValue(cellKey);
                    String value = getCellValue(cellValue);
                    if (!StringUtility.isNullOrEmpty(key) && !StringUtility.isNullOrEmpty(value)) {
                        map.put(key, value);
                        System.out.println(key + " - " + value);
                    }
                }
            }
            System.out.println(map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readExcelSql(File file) {
        Workbook wb = null;
        try {
            try {
                wb = new HSSFWorkbook(new FileInputStream(file));// 2003excel
            } catch (Exception e) {
                wb = new XSSFWorkbook(new FileInputStream(file));// 2007excel
            }
            Sheet sheet = wb.getSheetAt(0);
            // 得到总行数
            int rowNum = sheet.getLastRowNum();
            int cellNum;
            Row row;
            Cell cell;
            String columns = "";

            for (int i = 1; i <= rowNum; i++) {
                StringBuilder value = new StringBuilder();
                row = sheet.getRow(i);
                cellNum = row.getLastCellNum();// 列
                String table = "table";
                for (int j = 0; j < cellNum; j++) {// 对一行的每个列进行解析
                    cell = row.getCell(j);
                    if (i == 1) {
                        if (columns.equals("")) {// 全部都以字符串形式取出
                            if (cell != null) {
                                cell.setCellType(Cell.CELL_TYPE_STRING);
                                columns = table + "." + cell.getStringCellValue();// 保险起见以表名.列名
                            }
                        } else {
                            if (cell != null) {// 全部都以字符串形式取出
                                cell.setCellType(Cell.CELL_TYPE_STRING);
                                columns = columns + "," + table + "." + cell.getStringCellValue();// 保险起见以表名.列名
                            }
                        }
                    } else {
                        if (value.toString().equals("")) {// 全部都以字符串形式取出
                            if (cell != null) {
                                // cell.setCellType(Cell.CELL_TYPE_STRING);
                                value = new StringBuilder("'" + getCellValue(cell) + "'");
                            }
                        } else {
                            if (cell != null) {// 全部都以字符串形式取出
                                value.append("," + "'").append(getCellValue(cell)).append("'");
                            }
                        }
                    }
                }

                if (i != 1) {
                    System.out.println("insert into " + table + "(" + columns + ") values (" + value + ")");
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("未找到指定路径的文件!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (wb != null)
                try {
                    wb.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * 插入数据
     *
     * @param sheet    页面
     * @param dataList 数据
     */
    private static void refineRow(Sheet sheet, List<List<String>> dataList) {
        Row row;
        for (int i = 0; i < dataList.size(); ++i) {
            row = sheet.createRow(i + 1);
            List<String> data = dataList.get(i);
            for (int j = 0; j < data.size(); ++j) {
                row.createCell(j).setCellValue(data.get(j));
            }
        }
    }

    private static String getCellValue(Cell cell) {
        String cellValue;
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                cellValue = cell.getRichStringCellValue().getString().trim();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                SimpleDateFormat sdf;
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 日期
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    double value = cell.getNumericCellValue();
                    Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value);
                    cellValue = sdf.format(date);
                } else {
                    cellValue = df.format(cell.getNumericCellValue());
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                cellValue = String.valueOf(cell.getBooleanCellValue()).trim();
                break;
            case Cell.CELL_TYPE_FORMULA:
                cellValue = cell.getCellFormula();
                break;
            default:
                cellValue = "";
        }
        return cellValue;
    }
}
