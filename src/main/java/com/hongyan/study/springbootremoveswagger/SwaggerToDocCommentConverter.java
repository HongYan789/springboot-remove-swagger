package com.hongyan.study.springbootremoveswagger;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zy
 * @version 1.0
 * @date Created in 2023/8/23 3:40 PM
 * @description swagger相关代码剔除工具类
 */
@Slf4j
public class SwaggerToDocCommentConverter {

    private static List<File> javaFiles = new ArrayList<>();
    private static Map<String, byte[]> javaFileBytesMap = new HashMap<>();
    private static String directoryPath = "/Users/dearzhang/paraview/code/iam/idm/idm-client/src/main/java/com/paraview/idm/client/permission/request";

    static {
        collectJavaFiles(new File(directoryPath), javaFiles);
    }
    public static void main(String[] args) {
        for (File javaFile : javaFiles) {
            convertSwaggerToDocComments(javaFile.getAbsolutePath());
        }

        System.out.println("Batch conversion completed.");
    }

    /**
     * 收集java文件
     */
    @SneakyThrows
    public static void collectJavaFiles(File directory, List<File> fileList) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    collectJavaFiles(file, fileList);
                } else if (file.getName().endsWith(".java")) {
                    fileList.add(file);
                    //file 转 byte[]
                    javaFileBytesMap.put(file.getAbsolutePath(), fileToByteArray(file.getAbsolutePath()));
                }
            }
        }
    }

    /**
     * file 转 byte[]
     * @param filePath
     * @return
     * @throws IOException
     */
    public static byte[] fileToByteArray(String filePath) throws IOException {
        // method 1
        File file = new File(filePath);
        return FileCopyUtils.copyToByteArray(file);
        // method 2
//        File file = new File(filePath);
//        byte[] buffer = new byte[(int) file.length()];
//
//        try (FileInputStream fis = new FileInputStream(file)) {
//            int bytesRead = fis.read(buffer);
//            if (bytesRead != buffer.length) {
//                throw new IOException("Failed to read the whole file");
//            }
//        }
//        return buffer;

    }

    /**
     * byte[]转 BufferedReader
     * @param byteArray
     * @return
     */
    public static BufferedReader byteArrayToBufferedReader(byte[] byteArray) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        InputStreamReader inputStreamReader = new InputStreamReader(byteArrayInputStream);
        return new BufferedReader(inputStreamReader);
    }

    /**
     * 将swagger注解转换为文档注释
     * @param filePath
     */
    public static void convertSwaggerToDocComments(String filePath) {
        try (BufferedReader reader = byteArrayToBufferedReader(javaFileBytesMap.get(filePath));
             FileWriter writer = new FileWriter(filePath)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (removeTarget(line)) {
                    continue; // Skip Swagger import statements
                }
                if (containApiModel(line)) {
                    String description = extractApiModelDescription(line);
                    writer.write("/**\n * " + description + "\n */\n");
                } else if (containApiModelProperty(line)) {
                    String modifiedLine = convertLine(line);
                    writer.write(modifiedLine + "\n");
                } else if (containApiOperation(line)) {
                    String modifiedLine = convertApiOperationLine(line);
                    writer.write(modifiedLine + "\n");
                } else {
                    writer.write(line + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 需要删除跳过的行
     * @param line
     * @return
     */
    private static Boolean removeTarget(String line) {
        if (line.contains("import io.swagger.annotations.")) {
            return true;
        }
        if (line.contains("@ApiResponse(") && line.contains(")")) {
            return true;
        }
        if (line.contains("@ApiImplicitParam")) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否包含@ApiModel注解
     * 例：@ApiModel(value = "AppAccountBindGroupDTO对象", description = "获取已绑定/未绑定帐号列表")
     * @param line
     * @return
     */
    private static Boolean containApiModel(String line) {
        return line.contains("@ApiModel(value") && line.contains(")");
    }

    /**
     * 判断是否包含@ApiOperation注解
     * @param line
     * @return
     */
    private static Boolean containApiOperation(String line) {
        return line.contains("@ApiOperation(") && line.contains(")");
    }

    /**
     * 将@ApiModelProperty注解转换为文档注释
     * @param line
     * @return
     */
    private static String convertApiOperationLine(String line) {
        Pattern pattern = Pattern.compile("@ApiOperation\\(\"(.*)\"\\)");
        if (line.contains("@ApiOperation(value")) {
            pattern = Pattern.compile("value\\s*=\\s*\"(.*)\"");
        }
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String description = matcher.group(1);
            return "\t/**\n\t * " + description + "\n\t */";
        }

        return line;
    }

    /**
     * 提取@ApiModel注解的描述
     * 例：@ApiModel(value = "AppAccountBindGroupDTO对象", description = "获取已绑定/未绑定帐号列表")
     * @param line
     * @return
     */
    private static String extractApiModelDescription(String line) {
        Pattern pattern = Pattern.compile("description\\s*=\\s*\"(.*)\"");
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }


    /**
     * 判断是否包含@ApiModelProperty注解
     * 例：@ApiModelProperty("组id，多个以逗号分隔")
     * @param line
     * @return
     */
    private static Boolean containApiModelProperty(String line) {
        return line.contains("@ApiModelProperty(") && line.contains(")");
    }

    /**
     * 将@ApiModelProperty注解转换为文档注释
     * 例：@ApiModelProperty("组id，多个以逗号分隔")
     * @param line
     * @return
     */
    private static String convertLine(String line) {
        Pattern pattern = Pattern.compile("@ApiModelProperty\\(\"(.*)\"\\)");
        if (line.contains("@ApiModelProperty(value")) {
            pattern = Pattern.compile("value\\s*=\\s*\"(.*)\"");
        }
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String description = matcher.group(1);
            return "\t/**\n\t * " + description + "\n\t */";
        }

        return line;
    }
}
