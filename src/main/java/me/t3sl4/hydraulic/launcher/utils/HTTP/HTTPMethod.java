package me.t3sl4.hydraulic.launcher.utils.HTTP;

import javafx.application.Platform;
import javafx.concurrent.Task;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HTTPMethod {

    public interface RequestCallback {
        void onSuccess(String response) throws IOException;

        void onFailure();
    }

    private static final OkHttpClient client = new OkHttpClient();

    public enum RequestType {
        JSON_BODY,
        JSON_BODYLESS,
        JSON_BODY_AUTHORIZED,
        JSON_BODYLESS_AUTHORIZED,
        FILE_DOWNLOAD,
        FILE_UPLOAD,
        MULTIPLE_FILE_UPLOAD,
        FILE_DOWNLOAD_AUTHORIZED,
        MULTIPLE_FILE_UPLOAD_AUTHORIZED
    }

    public static void sendRequest(String url, String reqMethod, RequestType reqType, String jsonBody, Map<String, String> headers, Map<String, File> files, String localFilePath, RequestCallback callback) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Request.Builder requestBuilder = new Request.Builder().url(url);

                    switch (reqType) {
                        case JSON_BODY:
                            requestBuilder.method(reqMethod, RequestBody.create(jsonBody, MediaType.parse("application/json")));
                            requestBuilder.addHeader("Content-Type", "application/json");
                            break;

                        case JSON_BODYLESS:
                            requestBuilder.method(reqMethod, null);
                            break;

                        case JSON_BODY_AUTHORIZED:
                            requestBuilder.method(reqMethod, RequestBody.create(jsonBody, MediaType.parse("application/json")));
                            requestBuilder.addHeader("authorization", headers.get("authorization"));
                            requestBuilder.addHeader("Content-Type", "application/json");
                            break;

                        case JSON_BODYLESS_AUTHORIZED:
                            requestBuilder.method(reqMethod, null);
                            requestBuilder.addHeader("authorization", headers.get("authorization"));
                            break;

                        case FILE_DOWNLOAD:
                            requestBuilder.method(reqMethod, RequestBody.create(jsonBody, MediaType.parse("application/json")));
                            break;

                        case FILE_UPLOAD:
                            MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                            files.forEach((name, file) -> multipartBuilder.addFormDataPart(name, file.getName(), RequestBody.create(file, MediaType.parse("application/octet-stream"))));
                            if (jsonBody != null) {
                                multipartBuilder.addFormDataPart("userName", jsonBody);
                            }
                            requestBuilder.method(reqMethod, multipartBuilder.build());
                            break;

                        case MULTIPLE_FILE_UPLOAD:
                            MultipartBody.Builder multipleFilesBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                            files.forEach((name, file) -> multipleFilesBuilder.addFormDataPart(name, file.getName(), RequestBody.create(file, MediaType.parse("application/octet-stream"))));
                            requestBuilder.method(reqMethod, multipleFilesBuilder.build());
                            break;

                        case FILE_DOWNLOAD_AUTHORIZED:
                            requestBuilder.method(reqMethod, null);
                            requestBuilder.addHeader("authorization", headers.get("authorization"));
                            break;

                        case MULTIPLE_FILE_UPLOAD_AUTHORIZED:
                            MultipartBody.Builder authMultipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                            files.forEach((name, file) -> authMultipartBuilder.addFormDataPart(name, file.getName(), RequestBody.create(file, MediaType.parse("application/octet-stream"))));
                            requestBuilder.method(reqMethod, authMultipartBuilder.build());
                            requestBuilder.addHeader("authorization", headers.get("authorization"));
                            break;
                    }

                    Request request = requestBuilder.build();

                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            if (reqType == RequestType.FILE_DOWNLOAD || reqType == RequestType.FILE_DOWNLOAD_AUTHORIZED) {
                                if (response.body() != null) {
                                    Files.copy(response.body().byteStream(), new File(localFilePath).toPath());
                                    Platform.runLater(() -> {
                                        try {
                                            callback.onSuccess("File downloaded to " + localFilePath);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                } else {
                                    Platform.runLater(callback::onFailure);
                                }
                            } else {
                                String responseBody = response.body() != null ? response.body().string() : "";
                                Platform.runLater(() -> {
                                    try {
                                        callback.onSuccess(responseBody);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }
                        } else {
                            Platform.runLater(callback::onFailure);
                        }
                    }
                } catch (IOException e) {
                    Platform.runLater(callback::onFailure);
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    public static void sendJsonRequest(String url, String reqMethod, String jsonBody, RequestCallback callback) {
        sendRequest(url, reqMethod, RequestType.JSON_BODY, jsonBody, new HashMap<>(), null, null, callback);
    }

    public static void sendJsonlessRequest(String url, String reqMethod, RequestCallback callback) {
        sendRequest(url, reqMethod, RequestType.JSON_BODYLESS, null, new HashMap<>(), null, null, callback);
    }

    public static void sendAuthorizedJsonRequest(String url, String reqMethod, String jsonBody, String bearerToken, RequestCallback callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + bearerToken);
        sendRequest(url, reqMethod, RequestType.JSON_BODY_AUTHORIZED, jsonBody, headers, null, null, callback);
    }

    public static void sendAuthorizedJsonlessRequest(String url, String reqMethod, String bearerToken, RequestCallback callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + bearerToken);
        sendRequest(url, reqMethod, RequestType.JSON_BODYLESS_AUTHORIZED, null, headers, null, null, callback);
    }

    public static void downloadFile(String url, String reqMethod, String localFilePath, String userName, RequestCallback callback) {
        String jsonBody = "{\"userName\":\"" + userName + "\"}";
        sendRequest(url, reqMethod, RequestType.FILE_DOWNLOAD, jsonBody, new HashMap<>(), null, localFilePath, callback);
    }

    public static void uploadFile(String url, String reqMethod, File file, String userName, RequestCallback callback) {
        Map<String, File> files = new HashMap<>();
        files.put("file", file);
        String jsonBody = userName;
        sendRequest(url, reqMethod, RequestType.FILE_UPLOAD, jsonBody, new HashMap<>(), files, null, callback);
    }

    public static void authorizedDownloadFile(String url, String reqMethod, String localFilePath, String bearerToken, RequestCallback callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + bearerToken);
        sendRequest(url, reqMethod, RequestType.FILE_DOWNLOAD_AUTHORIZED, null, headers, null, localFilePath, callback);
    }

    public static void uploadMultipleFiles(String url, String reqMethod, Map<String, File> files, RequestCallback callback) {
        sendRequest(url, reqMethod, RequestType.MULTIPLE_FILE_UPLOAD, null, new HashMap<>(), files, null, callback);
    }

    public static void authorizedUploadMultipleFiles(String url, String reqMethod, Map<String, File> files, String bearerToken, String userName, String userID, String orderID, String hydraulicType, String unitParameters, RequestCallback callback) {
        OkHttpClient client = new OkHttpClient();  // Ensure client is initialized
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + bearerToken);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                    multipartBuilder.addFormDataPart("userName", userName);
                    multipartBuilder.addFormDataPart("orderID", orderID);
                    multipartBuilder.addFormDataPart("hydraulicType", hydraulicType);
                    multipartBuilder.addFormDataPart("unitParameters", unitParameters);
                    multipartBuilder.addFormDataPart("operationPlatform", "Desktop -- JavaFX");
                    multipartBuilder.addFormDataPart("sourceUserID", userID);
                    multipartBuilder.addFormDataPart("affectedHydraulicUnitID", orderID);

                    // Add files dynamically with proper MIME type
                    for (Map.Entry<String, File> entry : files.entrySet()) {
                        String name = entry.getKey();
                        File file = entry.getValue();
                        String mimeType = (name.equals("partListFile")) ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" : "application/pdf";
                        multipartBuilder.addFormDataPart(name, file.getName(),
                                RequestBody.create(file, MediaType.parse(mimeType)));
                    }

                    RequestBody requestBody = multipartBuilder.build();
                    Request.Builder requestBuilder = new Request.Builder()
                            .url(url)
                            .method(reqMethod, requestBody)
                            .addHeader("Authorization", "Bearer " + bearerToken);

                    Request request = requestBuilder.build();
                    System.out.println("Sending request to: " + url);  // Debugging
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            String responseBody = response.body() != null ? response.body().string() : "";
                            Platform.runLater(() -> {
                                try {
                                    callback.onSuccess(responseBody);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } else {
                            Platform.runLater(callback::onFailure);
                        }
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(callback::onFailure);
                }
                return null;
            }
        };
        new Thread(task).start();
    }
}