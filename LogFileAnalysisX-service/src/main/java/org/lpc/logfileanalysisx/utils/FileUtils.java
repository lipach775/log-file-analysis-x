package org.lpc.logfileanalysisx.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    public static String readFileToString(String filePath) {
        StringBuilder result = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(new File(filePath))) {
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    public static boolean writeStringToFile(String filePath, String str) {
        boolean success = true; // 使用基本类型 `boolean` 更简洁
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        File file = new File(filePath);

        try {
            // 如果父路径不存在，则创建路径
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // 创建文件（如果文件不存在）
            if (!file.exists()) {
                file.createNewFile();
            }

            // 写入文件内容
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(bytes);
            }
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }

        return success;
    }

}
