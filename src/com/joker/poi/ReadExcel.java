package com.joker.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.joker.staticcommon.StringUtility;

public class ReadExcel {

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static DecimalFormat df = new DecimalFormat("#");

    public static void main(String[] args) {

        File file = new File("f:/test.xlsx");
        if (!file.exists()) {
            System.out.println("文件不存在");
            return;
        }
        ReadExcel rf = new ReadExcel();
        rf.readExcel(file);
    }

    private static Integer srcIndex = null;
    private static Integer destIndex = null;

    /**
     * 读取 Excel 数据
     *
     * @param file 文件
     */
    public void readExcel(File file) {
        try {
            Map<String, String> map = new HashMap<>();
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
            while (iterator.hasNext()) {
                Row row = iterator.next();
                int numberOfCells = row.getPhysicalNumberOfCells();
                if (numberOfCells >= destIndex && numberOfCells > srcIndex) {
                    Cell cellKey = row.getCell(srcIndex);
                    Cell cellValue = row.getCell(destIndex);
                    String key = this.getCellValue(cellKey);
                    String value = this.getCellValue(cellValue);
                    if (!StringUtility.isNullOrEmpty(key) && !StringUtility.isNullOrEmpty(value)) {
                        map.put(key, value);
                        System.out.println(key + " - " + value);
                    }
                }
            }
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

    private String getCellValue(Cell cell) {
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
