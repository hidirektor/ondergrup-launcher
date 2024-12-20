package me.t3sl4.hydraulic.launcher.utils;

import javafx.application.Platform;
import me.t3sl4.hydraulic.launcher.utils.SystemVariables;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class FileUtil {
    public static void criticalFileSystem() {
        // İşletim sistemine göre dosya yollarını ayarla
        String userHome = System.getProperty("user.name");
        String os = System.getProperty("os.name").toLowerCase();
        String basePath;
        String programName;

        if (os.contains("win")) {
            basePath = "C:/Users/" + userHome + "/";
            programName = "windows_Hydraulic.exe";
        } else {
            basePath = "/Users/" + userHome + "/";
            programName = "unix_Hydraulic.jar";
        }

        // Dosya yollarını belirle
        SystemVariables.mainPath = basePath + "OnderGrup/";
        SystemVariables.userDataPath = SystemVariables.mainPath + "userData/";
        SystemVariables.localHydraulicDataPath = SystemVariables.mainPath + "userData/HydraulicUnits/";
        SystemVariables.partListDataPath = SystemVariables.mainPath + "data/";
        SystemVariables.licensePath = SystemVariables.userDataPath + "license.txt";
        SystemVariables.hydraulicPath = SystemVariables.mainPath + programName;
        SystemVariables.downloadPath = SystemVariables.mainPath;
    }

    private static void createDirectory(String path) throws IOException {
        Path dirPath = Paths.get(path);
        if (Files.notExists(dirPath)) {
            Files.createDirectories(dirPath);
        }
    }

    public static void createFile(String path) throws IOException {
        Path filePath = Paths.get(path);
        if (Files.notExists(filePath)) {
            Files.createFile(filePath);
        }
    }

    public static void fileCopy(String sourcePath, String destPath, boolean isRefresh) throws IOException {
        File destinationFile = new File(destPath);

        if(isRefresh) {
            InputStream resourceAsStream = FileUtil.class.getResourceAsStream(sourcePath);

            if (resourceAsStream == null) {
                throw new FileNotFoundException("Kaynak bulunamadı: " + sourcePath);
            }

            Path destination = Paths.get(destPath);
            Files.copy(resourceAsStream, destination, StandardCopyOption.REPLACE_EXISTING);
            resourceAsStream.close();
        } else {
            if (!destinationFile.exists()) {
                InputStream resourceAsStream = FileUtil.class.getResourceAsStream(sourcePath);

                if (resourceAsStream == null) {
                    throw new FileNotFoundException("Kaynak bulunamadı: " + sourcePath);
                }

                Path destination = Paths.get(destPath);
                Files.copy(resourceAsStream, destination, StandardCopyOption.REPLACE_EXISTING);
                resourceAsStream.close();
            } else {
                System.out.println("File already exists: " + destPath);
            }
        }
    }
}