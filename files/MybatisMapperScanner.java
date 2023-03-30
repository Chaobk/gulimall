package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MybatisMapperScanner {

    private static final Pattern namespaceRegex = Pattern.compile("^<mapper\\s+namespace=\"(.+?)\".*");

    public static void main(String[] args) throws IOException {
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
            list.add("Mapper file: " + mapperFile.getName());
            list.add("=======================================");
            List<String> mapperLines = Files.readAllLines(Paths.get(mapperFile.getAbsolutePath()));
            boolean inSqlStatement = false;
            StringBuilder sbu = new StringBuilder();
            for (String mapperLine : mapperLines) {
                if (mapperLine.contains("namespace")) {
                    boolean b = namespaceRegex.matcher(mapperLine).find();
                    sbu.append("namespace=" + namespaceRegex.matcher(mapperLine).group(0));
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
}