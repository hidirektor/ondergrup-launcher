package me.t3sl4.hydraulic.launcher.utils.Model;

public class User {

    private String userName;
    private String password;
    private String nameSurname;
    private boolean isFavourite;
    private boolean isDeleted;
    private String deletionDate;
    private String licenseKey;
    private String accessToken;

    public User(String userName, String password, String nameSurname, boolean isFavourite, boolean isDeleted, String deletionDate, String licenseKey, String accessToken) {
        this.userName = userName;
        this.password = password;
        this.nameSurname = nameSurname;
        this.isFavourite = isFavourite;
        this.isDeleted = isDeleted;
        this.deletionDate = deletionDate;
        this.licenseKey = licenseKey;
        this.accessToken = accessToken;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNameSurname() {
        return nameSurname;
    }

    public void setNameSurname(String nameSurname) {
        this.nameSurname = nameSurname;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(String deletionDate) {
        this.deletionDate = deletionDate;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
