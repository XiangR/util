package com.joker.normal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class JavaMoveFile {
    public static void main(String[] args) {

        // 实际使用
        @SuppressWarnings("unused")
        List<String> cityIds = Arrays.asList("110100", "120100", "130100", "130200", "130300", "130400", "130500", "130600", "130700", "130800", "130900", "131000", "131100", "140100", "140200",
                "140300", "140400", "140500", "140600", "140700", "140800", "140900", "141000", "141100", "150100", "150200", "150300", "150400", "150500", "150600", "150700", "150800", "150900",
                "152200", "152500", "152900", "210100", "210200", "210300", "210400", "210500", "210600", "210700", "210800", "210900", "211000", "211100", "211200", "211300", "211400", "220100",
                "220200", "220300", "220400", "220500", "220600", "220700", "220800", "222400", "230100", "230200", "230300", "230400", "230500", "230600", "230700", "230800", "230900", "231000",
                "231100", "231200", "232700", "310100", "320100", "320200", "320300", "320400", "320500", "320600", "320700", "320800", "320900", "321000", "321100", "321200", "321300", "330100",
                "330200", "330300", "330400", "330500", "330600", "330700", "330800", "330900", "331000", "331100", "340100", "340200", "340300", "340400", "340500", "340600", "340700", "340800",
                "341000", "341100", "341200", "341300", "341500", "341600", "341700", "341800", "350100", "350200", "350300", "350400", "350500", "350600", "350700", "350800", "350900", "360100",
                "360200", "360300", "360400", "360500", "360600", "360700", "360800", "360900", "361000", "361100", "370100", "370200", "370300", "370400", "370500", "370600", "370700", "370800",
                "370900", "371000", "371100", "371200", "371300", "371400", "371500", "371600", "371700", "410100", "410200", "410300", "410400", "410500", "410600", "410700", "410800", "410900",
                "411000", "411100", "411200", "411300", "411400", "411500", "411600", "411700", "419000", "420100", "420200", "420300", "420500", "420600", "420700", "420800", "420900", "421000",
                "421100", "421200", "421300", "422800", "429000", "430100", "430200", "430300", "430400", "430500", "430600", "430700", "430800", "430900", "431000", "431100", "431200", "431300",
                "433100", "440100", "440200", "440300", "440400", "440500", "440600", "440700", "440800", "440900", "441200", "441300", "441400", "441500", "441600", "441700", "441800", "441900",
                "442000", "445100", "445200", "445300", "450100", "450200", "450300", "450400", "450500", "450600", "450700", "450800", "450900", "451000", "451100", "451200", "451300", "451400",
                "460100", "460300", "469000", "500100", "510100", "510300", "510400", "510500", "510600", "510700", "510800", "510900", "511000", "511100", "511300", "511400", "511500", "511600",
                "511700", "511800", "511900", "512000", "513200", "513300", "513400", "520100", "520200", "520300", "520400", "520500", "520600", "522300", "522600", "522700", "530100", "530300",
                "530400", "530500", "530600", "530700", "530800", "530900", "532300", "532500", "532600", "532800", "532900", "533100", "533300", "533400", "540100", "542100", "542200", "542300",
                "542400", "542500", "542600", "610100", "610200", "610300", "610400", "610500", "610600", "610700", "610800", "610900", "611000", "620100", "620300", "620400", "620500", "620600",
                "620700", "620800", "620900", "621000", "621100", "621200", "622900", "623000", "630100", "632100", "632200", "632300", "632500", "632600", "632700", "632800", "640100", "640200",
                "640300", "640400", "640500", "650100", "650200", "652100", "652200", "652300", "652700", "652800", "652900", "653000", "653100", "653200", "654000", "654200", "654300", "659000");
        // 文件提取路径
        String fileHeadPath = "f:/data/";
        // 由路径创建文件夹
        String realPath = "f:/zip";
        File file = new File(realPath);
        if (file.exists()) {
            file.delete();
        }
        file.mkdir();
        List<String> list = Arrays.asList("110100", "120100", "120100");
        for (String name : list) {
            String copyfilePath = realPath + "/" + name;
            copyFile(fileHeadPath + name, copyfilePath);
        }
        // 压缩文件夹
        ZipMultiFile(realPath, realPath + ".zip");
    }

    public static void copyFile(String oldPath, String newPath) {
        try {
            int byteread = 0;
            File oldfile = new File(oldPath);
            File newFile = new File(newPath);
            if (newFile.exists()) { // 文件存在时
                newFile.delete();
            }
            if (oldfile.exists()) { // 文件存在时
                InputStream inStream = new FileInputStream(oldPath); // 读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[(int) oldfile.length()];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                fs.close();
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
        }
    }

    /*
     * 压缩文件夹
     */
    public static void ZipMultiFile(String filepath, String zippath) {
        ZipOutputStream zipOut = null;
        try {
            File file = new File(filepath);// 要被压缩的文件夹
            File zipFile = new File(zippath);
            if (zipFile.exists()) {
                zipFile.delete();
            }
            InputStream input = null;
            zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; ++i) {
                    input = new FileInputStream(files[i]);
                    zipOut.putNextEntry(new ZipEntry(file.getName() + File.separator + files[i].getName()));
                    int temp = 0;
                    while ((temp = input.read()) != -1) {
                        zipOut.write(temp);
                    }
                    input.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (zipOut != null) {
                    zipOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * 压缩一个文件
     */
    public static void ZipFile(String filepath, String zippath) {
        InputStream input = null;
        ZipOutputStream zipOut = null;
        try {
            File file = new File(filepath);
            File zipFile = new File(zippath);
            input = new FileInputStream(file);
            zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
            zipOut.putNextEntry(new ZipEntry(file.getName()));
            int temp = 0;
            while ((temp = input.read()) != -1) {
                zipOut.write(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (zipOut != null) {
                    zipOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
