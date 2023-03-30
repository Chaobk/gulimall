package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MybatisMapperScanner {

    private static final Pattern namespacePattern = Pattern.compile( "<mapper\\s+namespace=\"([^\"]+)\">");

    private static final Pattern filenamePattern = Pattern.compile(".*\\.xml");

    public static void main(String[] args) throws IOException {
        String rootPath = "F:\\workFiles\\ideaPros\\mall-master\\mall-portal\\src\\main\\resources\\dao";

        List<String> list = new ArrayList<>();
        list = scan();

        for (String s : list) {
            System.out.println(s);
        }
    }

    private static List<String> scan() throws IOException {
        String directoryPath = "F:\\workFiles\\ideaPros\\mvnPro\\src\\main\\java\\files";
        List<File> mapperFiles = getMapperFiles(directoryPath);
        List<String> list = new ArrayList<>();
        for (File mapperFile : mapperFiles) {
            list.add("=======================================");
            list.add("Mapper file: " + mapperFile.getAbsolutePath());
            list.add("=======================================");
            List<String> mapperLines = Files.readAllLines(Paths.get(mapperFile.getAbsolutePath()));
            boolean inSqlStatement = false;
            StringBuilder sbu = new StringBuilder();
            for (String mapperLine : mapperLines) {
                if (mapperLine.contains("namespace")) {
                    Matcher matcher = namespacePattern.matcher(mapperLine);
                    if (matcher.find()) sbu.append("namespace=" + matcher.group(1));
                    list.add(sbu.toString());
                    sbu = new StringBuilder();
                    continue;
                }
                if (mapperLine.contains("<select") || mapperLine.contains("<update") || mapperLine.contains("<insert") || mapperLine.contains("<delete")) {
                    inSqlStatement = true;
                    sbu.append(mapperLine.trim());
                    sbu.append(" ");
                } else if (inSqlStatement) {
                    sbu.append(mapperLine.trim() + " ");
                    if (mapperLine.contains("</select>") || mapperLine.contains("</update>") || mapperLine.contains("</insert>") || mapperLine.contains("</delete>")) {
                        inSqlStatement = false;
                        list.add(sbu.toString());
                        sbu = new StringBuilder();
                    }
                }
            }
            System.out.println();
        }
        return list;
    }

    private static List<File> getMapperFiles(String directoryPath) throws IOException {
        try (Stream<Path> walk = Files.walk(Paths.get(directoryPath))) {
            return walk.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(file -> file.getName().endsWith(".xml"))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 扫描路径下的所有xml
     * @param path
     * @return
     */
    public static List<File> scanFiles(String path) {
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        List<File> matchedFiles = new ArrayList<>();
        for (File file : files) {
            if (file.isFile()) {
                Matcher matcher = filenamePattern.matcher(file.getName());
                if (matcher.matches()) {
                    matchedFiles.add(file);
                }
            } else if (file.isDirectory()) {
                matchedFiles.addAll(scanFiles(file.getAbsolutePath()));
            }
        }
        return matchedFiles;
    }
}