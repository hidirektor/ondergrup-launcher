package me.t3sl4.hydraulic.launcher.utils.HTTP;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import me.t3sl4.hydraulic.launcher.Launcher;
import me.t3sl4.hydraulic.launcher.utils.GeneralUtil;
import me.t3sl4.hydraulic.launcher.utils.SystemVariables;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class HttpUtil {
    public static void activateLicense(String licenseUrl, String licenseKey, String activatedBy, String accessToken, Runnable onSuccess, Runnable onFailure) throws IOException {
        JSONObject licenseJson = new JSONObject();
        licenseJson.put("licenseKey", licenseKey);
        licenseJson.put("activatedBy", activatedBy);
        licenseJson.put("deviceInfo", GeneralUtil.getDeviceInfoAsJson().toString());

        HTTPMethod.sendAuthorizedJsonRequest(licenseUrl, "POST", licenseJson.toString(), accessToken, new HTTPMethod.RequestCallback() {
            @Override
            public void onSuccess(String licenseResponse) {
                if(onSuccess != null) {
                    onSuccess.run();
                }
            }

            @Override
            public void onFailure() {
                if(onFailure != null) {
                    onFailure.run();
                }
            }
        });
    }

    public static void checkLicense(String licenseUrl, String licenseKey, Runnable onSuccess, Runnable onFailure) throws IOException {
        JSONObject licenseJson = new JSONObject();
        licenseJson.put("licenseKey", licenseKey);

        HTTPMethod.sendJsonRequest(licenseUrl, "POST", licenseJson.toString(), new HTTPMethod.RequestCallback() {
            @Override
            public void onSuccess(String licenseResponse) {
                if(onSuccess != null) {
                    onSuccess.run();
                }
            }

            @Override
            public void onFailure() {
                if(onFailure != null) {
                    onFailure.run();
                }
            }
        });
    }

    public static void loginReq(String loginUrl, String jsonLoginBody, String userName, String password, String licenseKey, Label requestResponseLabel, Pane mainPane, ImageView profilePhoto, Label nameSurnameLabel, Label eMailLabel) throws IOException {
        HTTPMethod.sendJsonRequest(loginUrl, "POST", jsonLoginBody, new HTTPMethod.RequestCallback() {
            @Override
            public void onSuccess(String loginResponse) throws IOException {
                JSONObject mainObject = new JSONObject(loginResponse);
                JSONObject loginObject = mainObject.getJSONObject("payload");
                String userID = loginObject.getString("userID");
                String accessToken = loginObject.getString("accessToken");

                String userNameData = userName;
                String passwordData = password;
                String licenseKeyData = licenseKey;
                String accessTokenData = accessToken;
                final String[] nameSurnameData = new String[1];

                requestResponseLabel.setText("Successfully logged in :)");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String profileInfoUrl = SystemVariables.BASE_URL + SystemVariables.profileInfoURLPrefix;
                String jsonProfileInfoBody = "{\"userID\": \"" + userID + "\"}";
                HTTPMethod.sendAuthorizedJsonRequest(profileInfoUrl, "POST", jsonProfileInfoBody, accessToken, new HTTPMethod.RequestCallback() {
                    @Override
                    public void onSuccess(String profileInfoResponse) {
                        JSONObject defaultObject = new JSONObject(profileInfoResponse);
                        JSONObject mainObject = defaultObject.getJSONObject("payload");

                        JSONObject userObject = mainObject.getJSONObject("user");
                        nameSurnameData[0] = userObject.getString("nameSurname");
                        nameSurnameLabel.setText(nameSurnameData[0]);
                        eMailLabel.setText(userObject.getString("eMail"));

                        requestResponseLabel.setText("Profile fetch successfully completed :)");

                        HttpUtil.downloadAndSetProfilePhoto(userName, profilePhoto);
                        try {
                            Thread.sleep(6000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        String roleValue = userObject.getString("userType");
                        if (roleValue.equals("TECHNICIAN") || roleValue.equals("ENGINEER") || roleValue.equals("SYSOP")) {
                            String activateLicense = SystemVariables.BASE_URL + SystemVariables.activateLicenseUrlPrefix;
                            String activateLicenseJsonBody = "{\"activatedBy\": \"" + userID + "\", \"licenseKey\": \"" + licenseKey + "\", \"deviceInfo\": \"" + GeneralUtil.getDeviceInfoAsJson() + "\"}";

                            HTTPMethod.sendAuthorizedJsonRequest(activateLicense, "POST", activateLicenseJsonBody, accessToken, new HTTPMethod.RequestCallback() {
                                @Override
                                public void onSuccess(String profileInfoResponse) {
                                    JSONObject defaultObject = new JSONObject(profileInfoResponse);
                                    String message = defaultObject.getString("message");

                                    if (message.contains("success")) {
                                        requestResponseLabel.setText("License fetched successfully :)");
                                        try {
                                            Thread.sleep(3000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        GeneralUtil.createUserAccountData(userNameData, passwordData, nameSurnameData[0], licenseKeyData, accessTokenData, false);

                                        mainPane.setVisible(false);
                                        mainPane.toBack();
                                    }
                                }

                                @Override
                                public void onFailure() {
                                    requestResponseLabel.setText("License fetch failed. Saving only user data not license data.");
                                    try {
                                        Thread.sleep(3000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    GeneralUtil.createUserAccountData(userNameData, passwordData, nameSurnameData[0], null, accessTokenData, false);

                                    mainPane.setVisible(false);
                                    mainPane.toBack();
                                }
                            });
                        } else {
                            requestResponseLabel.setText("That user is not authorized. Please try different account.");
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure() {
                        requestResponseLabel.setText("Profile fetch failed. Please check your credentials.");
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onFailure() {
                requestResponseLabel.setText("Login failed. Please check your credentials.");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void downloadAndSetProfilePhoto(String username, ImageView profilePhotoImageView) {
        String localFileFinalPath = SystemVariables.userAccountsFolderPath + username + ".jpg";

        File localFile = new File(localFileFinalPath);
        if (localFile.exists()) {
            setProfilePhoto(username, profilePhotoImageView);
        } else {
            String photoUrl = SystemVariables.BASE_URL + SystemVariables.downloadPhotoURLPrefix;

            HTTPMethod.downloadFile(photoUrl, "POST", localFileFinalPath, username, new HTTPMethod.RequestCallback() {
                @Override
                public void onSuccess(String response) {
                    setProfilePhoto(username, profilePhotoImageView);
                }

                @Override
                public void onFailure() {
                    System.out.println("Profil fotoğrafı indirilemedi.");
                }
            });
        }
    }

    public static void setProfilePhoto(String username, ImageView profilePhotoImageView) {
        String photoPath = SystemVariables.userAccountsFolderPath + username + ".jpg";
        File photoFile = new File(photoPath);

        if (photoFile.exists()) {
            Image image = new Image(photoFile.toURI().toString());
            profilePhotoImageView.setImage(image);
            profilePhotoImageView.setVisible(true);
        } else {
            Image image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/images/logo-sade.png")));
            profilePhotoImageView.setImage(image);
            profilePhotoImageView.setVisible(true);
        }
    }
}
