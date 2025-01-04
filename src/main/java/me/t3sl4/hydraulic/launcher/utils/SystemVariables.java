package me.t3sl4.hydraulic.launcher.utils;

public class SystemVariables {
    public static final String CURRENT_VERSION = "v1.2.7";

    public static String REPO_OWNER = "hidirektor";
    public static String HYDRAULIC_REPO_NAME = "ondergrup-hydraulic-tool";

    public static String PREF_NODE_NAME = "Canicula/releases";
    public static String PREF_UPDATER_KEY = "updater_version";
    public static String PREF_LAUNCHER_KEY = "launcher_version";
    public static String PREF_HYDRAULIC_KEY = "hydraulic_version";

    public static String BASE_URL = "https://ondergrup.hidirektor.com.tr/api/v2";

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
