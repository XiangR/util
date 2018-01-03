package com.joker.enumcommon;

/**
 * Created by xiangrui on 2018/1/3.
 *
 * @author xiangrui
 * @date 2018/1/3
 */
public enum ExcelEnum {

    XLS("xls"),

    XLSX("xlsx");

    private String type;

    ExcelEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
