package me.t3sl4.hydraulic.launcher.utils;

import me.t3sl4.hydraulic.launcher.utils.Model.User;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtil {
    public static void criticalFileSystem() throws IOException {
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
        SystemVariables.userAccountsFolderPath = SystemVariables.mainPath + "userAccounts/";
        SystemVariables.userAccountsDataPath = SystemVariables.mainPath + "userAccounts/userAccounts.yml";
        SystemVariables.profilePhotoLocalPath = SystemVariables.mainPath + "userAccounts/";
        SystemVariables.userDataPath = SystemVariables.mainPath + "userData/";
        SystemVariables.localHydraulicDataPath = SystemVariables.mainPath + "userData/HydraulicUnits/";
        SystemVariables.partListDataPath = SystemVariables.mainPath + "data/";
        SystemVariables.licensePath = SystemVariables.userDataPath + "license.txt";
        SystemVariables.hydraulicPath = SystemVariables.mainPath + programName;
        SystemVariables.downloadPath = SystemVariables.mainPath;

        createDirectory(SystemVariables.userAccountsFolderPath);
        createFile(SystemVariables.userAccountsDataPath);
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

    @SuppressWarnings("unchecked")
    public static void createUserAccountData(String userName, String password, String nameSurname, String licenseKey, String accessToken, boolean isFavourite) {
        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(loaderOptions);
        Map<String, Object> data;

        File yamlFile = new File(SystemVariables.userAccountsDataPath);
        if (!yamlFile.exists()) {
            data = new HashMap<>();
            data.put("user_accounts", new HashMap<>());
        } else {
            try (FileReader reader = new FileReader(yamlFile)) {
                data = yaml.load(reader);
                if (data == null) {
                    data = new HashMap<>();
                    data.put("user_accounts", new HashMap<>());
                }
            } catch (IOException e) {
                throw new RuntimeException("YAML dosyası okunurken bir hata oluştu", e);
            }
        }

        Map<String, Map<String, Object>> userAccountsMap = (Map<String, Map<String, Object>>) data.get("user_accounts");

        boolean found = false;
        for (Map.Entry<String, Map<String, Object>> entry : userAccountsMap.entrySet()) {
            if (entry.getValue().get("userName").equals(userName)) {
                found = true;
                break;
            }
        }

        if (!found) {
            int nextIndex = userAccountsMap.size();
            Map<String, Object> newEntry = new HashMap<>();
            newEntry.put("userName", userName);
            newEntry.put("password", password);
            newEntry.put("nameSurname", nameSurname);
            newEntry.put("isFavourite", String.valueOf(isFavourite));
            newEntry.put("isDeleted", String.valueOf(false));
            newEntry.put("deletionDate", "");
            newEntry.put("licenseKey", licenseKey == null ? "" : licenseKey);
            newEntry.put("accessToken", accessToken);

            userAccountsMap.put(String.valueOf(nextIndex), newEntry);
        }

        try (FileWriter writer = new FileWriter(yamlFile)) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
            yaml = new Yaml(options);
            yaml.dump(data, writer);
        } catch (IOException e) {
            throw new RuntimeException("YAML dosyasına yazılırken bir hata oluştu", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void readUserAccountData(List<User> savedUserAccounts) {
        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(loaderOptions);
        Map<String, Object> data;

        File yamlFile = new File(SystemVariables.userAccountsDataPath);
        if (!yamlFile.exists()) {
            return; // Dosya yoksa işlem yapmadan geri dön
        } else {
            try (FileReader reader = new FileReader(yamlFile)) {
                data = yaml.load(reader);
                if (data == null || !data.containsKey("user_accounts")) {
                    return; // Geçerli bir veri yoksa işlem yapmadan geri dön
                }
            } catch (IOException e) {
                System.out.println("Hata: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("YAML dosyası okunurken bir hata oluştu", e);
            }
        }

        Map<String, Map<String, Object>> userAccountsMap = (Map<String, Map<String, Object>>) data.get("user_accounts");
        for (Map<String, Object> accountData : userAccountsMap.values()) {
            String userName = (String) accountData.get("userName");
            String password = (String) accountData.get("password");
            String nameSurname = (String) accountData.get("nameSurname");
            boolean isFavourite = Boolean.parseBoolean((String) accountData.get("isFavourite"));
            boolean isDeleted = Boolean.parseBoolean((String) accountData.get("isDeleted"));
            String deletionDate = (String) accountData.get("deletionDate");
            String licenseKey = (String) accountData.get("licenseKey");
            String accessToken = (String) accountData.get("accessToken");

            User user = new User(userName, password, nameSurname, isFavourite, isDeleted, deletionDate, licenseKey, accessToken);
            savedUserAccounts.add(user);
        }
    }

    public static void updateUserFavouriteStatusInFile(User userData) {
        File yamlFile = new File(SystemVariables.userAccountsDataPath);
        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(loaderOptions);
        Map<String, Object> data;

        try (FileReader reader = new FileReader(yamlFile)) {
            data = yaml.load(reader);
            if (data != null && data.containsKey("user_accounts")) {
                Map<String, Map<String, Object>> userAccountsMap = (Map<String, Map<String, Object>>) data.get("user_accounts");

                for (Map.Entry<String, Map<String, Object>> entry : userAccountsMap.entrySet()) {
                    Map<String, Object> accountData = entry.getValue();
                    if (accountData.get("userName").equals(userData.getUserName())) {
                        accountData.put("isFavourite", String.valueOf(userData.isFavourite()));
                        break;
                    }
                }

                DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);

                try (FileWriter writer = new FileWriter(yamlFile)) {
                    yaml = new Yaml(options);
                    yaml.dump(data, writer);
                }
            }
        } catch (IOException e) {
            System.out.println("Error updating user favourite status: " + e.getMessage());
        }
    }

    public static void deleteUserInFile(User userData) {
        File yamlFile = new File(SystemVariables.userAccountsDataPath);
        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(loaderOptions);
        Map<String, Object> data;

        try (FileReader reader = new FileReader(yamlFile)) {
            data = yaml.load(reader);
            if (data != null && data.containsKey("user_accounts")) {
                Map<String, Map<String, Object>> userAccountsMap = (Map<String, Map<String, Object>>) data.get("user_accounts");

                for (Map.Entry<String, Map<String, Object>> entry : userAccountsMap.entrySet()) {
                    Map<String, Object> accountData = entry.getValue();
                    if (accountData.get("userName").equals(userData.getUserName())) {
                        accountData.put("isDeleted", String.valueOf(userData.isFavourite()));
                        accountData.put("deletionDate", userData.getDeletionDate());
                        break;
                    }
                }

                DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);

                try (FileWriter writer = new FileWriter(yamlFile)) {
                    yaml = new Yaml(options);
                    yaml.dump(data, writer);
                }
            }
        } catch (IOException e) {
            System.out.println("Error updating user favourite status: " + e.getMessage());
        }
    }
}