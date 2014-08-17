package com.noe.hypercube.googledrive;


import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

public class Authentication {
    private static final Logger LOG = Logger.getLogger(Authentication.class);

    private static String CLIENT_ID = "752406924353.apps.googleusercontent.com";
    private static String CLIENT_SECRET = "QbGMzP0XtBKq8XRJ0VQuPLOD";

    private static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    private static String APPLICATION_NAME = "HyperCube";

    public static String getUrl() {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleAuthorizationCodeFlow flow = getGoogleAuthorizationCodeFlow(httpTransport, jsonFactory);
        return flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
    }

    public static Drive createDriveService() throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleAuthorizationCodeFlow flow = getGoogleAuthorizationCodeFlow(httpTransport, jsonFactory);

        String authCode = getAuthCode();
        GoogleTokenResponse response = flow.newTokenRequest(authCode).setRedirectUri(REDIRECT_URI).execute();
        GoogleCredential credential = new GoogleCredential();
        credential.setFromTokenResponse(response);
        return new Drive.Builder(httpTransport, jsonFactory, credential).setApplicationName(APPLICATION_NAME).build();
    }

    private static GoogleAuthorizationCodeFlow getGoogleAuthorizationCodeFlow(HttpTransport httpTransport, JsonFactory jsonFactory) {
        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE))
                .setAccessType("online")
                .setApprovalPrompt("auto").build();
    }

    private static String getAuthCode() {
        String authCode = "";
        Properties properties = new Properties();
        InputStream resourceAsStream = Authentication.class.getClassLoader().getResourceAsStream("auth.properties");
        try {
            properties.load(resourceAsStream);
            authCode = properties.getProperty("google.auth.code");
        } catch (IOException e) {
            LOG.error("Cannot load google Drive authorization code", e);
        }
        return authCode;
    }

}
