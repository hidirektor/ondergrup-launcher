package me.t3sl4.hydraulic.launcher.utils;

public class SystemVariables {
    public static final String CURRENT_VERSION = "v1.2.5";

    public static String BASE_URL = "https://ondergrup.hidirektor.com.tr/api/v2";
    public static String RELEASE_URL = "https://github.com/hidirektor/ondergrup-hydraulic-tool/releases";
    public static String NEW_VERSION_URL = "https://github.com/hidirektor/ondergrup-hydraulic-tool/releases/latest";
    public static String ASSET_URL = "https://api.github.com/repos/hidirektor/ondergrup-hydraulic-tool/releases/latest";

    public static String WEB_URL = "https://ondergrup.com";
    public static String BASE_LOGIN_URL = "https://ondergrup.hidirektor.com.tr/#/login";
    public static String developedBy = "Designed and Coded by\nHalil İbrahim Direktör";
    public static String PORTFOLIO_URL = "https://hidirektor.com.tr";

    public static String loginURLPrefix = "/auth/login";
    public static String profileInfoURLPrefix = "/user/getProfile";
    public static String checkLicenseUrlPrefix = "/license/check";
    public static String activateLicenseUrlPrefix = "/license/activate";
    public static String downloadPhotoURLPrefix = "/user/downloadProfilePhoto";

    public static String mainPath;
    public static String userAccountsFolderPath;
    public static String userAccountsDataPath;
    public static String licensePath;
    public static String userDataPath;
    public static String profilePhotoLocalPath;
    public static String localHydraulicDataPath;
    public static String partListDataPath;
    public static String hydraulicPath;
    public static String downloadPath;

    public static String getVersion() {
        return CURRENT_VERSION;
    }
}
