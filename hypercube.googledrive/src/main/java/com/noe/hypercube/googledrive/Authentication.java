package com.noe.hypercube.googledrive;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import java.awt.*;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.Logger;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Properties;

public class Authentication {
    private static final Logger LOG = Logger.getLogger(Authentication.class);

    private static String CLIENT_ID = "752406924353.apps.googleusercontent.com";
    private static String CLIENT_SECRET = "QbGMzP0XtBKq8XRJ0VQuPLOD";

    //    private static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    private static String REDIRECT_URI = "http://localhost";
    private static String APPLICATION_NAME = "HyperCube";

    private static final String CLIENTSECRETS_LOCATION = "client_secrets.json";

    /**
     * Email of the Service Account
     */
    private static final String SERVICE_ACCOUNT_EMAIL = "752406924353@project.googleusercontent.com";

    /**
     * Path to the Service Account's Private Key file
     */
    private static final String SERVICE_ACCOUNT_PKCS12_FILE_PATH = "/HyperCube-8a2cd699b4d7.p12";

    public static void main(String[] args) {
        try {
            createDriveService();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

        String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
//        System.out.println("Please open the following URL in your browser then type the authorization code:");
//        System.out.println("  " + url);
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//        String code = br.readLine();
        String code = "";

        try {
            Desktop.getDesktop().browse(java.net.URI.create(url));
            code = getCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
        final String refreshToken = response.getRefreshToken();
        final String accessToken = response.getAccessToken();
        System.out.println("refreshToken: " + refreshToken);
        System.out.println("accessToken: " + accessToken);
        GoogleCredential credential = new GoogleCredential.Builder().setJsonFactory(jsonFactory).setTransport(httpTransport).setClientSecrets(CLIENT_ID, CLIENT_SECRET).build();
        credential.setFromTokenResponse(response);
        final Drive drive = new Drive.Builder(httpTransport, jsonFactory, credential).setApplicationName(APPLICATION_NAME).build();
        final FileList fileList = drive.files().list().execute();
        System.out.println(fileList);
        return drive;
    }

    private static String getCode() throws IOException {

        ServerSocket serverSocket = new ServerSocket();
        Socket socket = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        while (true) {
            String code = "";
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                out.write("HTTP/1.1 200 OK\r\n");
                out.write("Content-Type: text/html\r\n");
                out.write("\r\n");

                code = in.readLine();
                System.out.println("code = " + code);
                String match = "code";
                int loc = 0;
                if(code != null) {
                    loc = code.indexOf(match);
                }

                if (loc > 0) {
                    int httpstr = code.indexOf("HTTP") - 1;
                    code = code.substring(code.indexOf(match), httpstr);
                    String parts[] = code.split("=");
                    code = parts[1];
                    out.write("Thanks for using Hypercube!");
                    out.close();
                    socket.close();
                    return code;
                } else {
                    // It doesn't have a code
                    out.write("Code not found in the URL!");
                }

            } catch (IOException e) {
                //error ("System: " + "Connection to server lost!");
                System.exit(1);
                break;
            }
        }
        return "";
    }

    public static Drive createDrive() {
        final HttpTransport httpTransport = new NetHttpTransport();
        final JsonFactory jsonFactory = new JacksonFactory();
        Credential credential = null;
        try {
            final GoogleAuthorizationCodeFlow flow = getFlow();
            credential = exchangeCode(getProperty("google.auth.code"));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        final GoogleCredential credential = new GoogleCredential.Builder()
//                .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
//                .setTransport(httpTransport)
//                .setJsonFactory(jsonFactory)
//                .build();
//
//        credential.setAccessToken(getProperty("google.token.access"));
//        credential.setRefreshToken(getProperty("google.token.refresh"));

        StoredCredential storedCredential;
        final Drive drive = new Drive.Builder(httpTransport, jsonFactory, credential).setApplicationName(APPLICATION_NAME).build();
        return drive;
    }

    /**
     * Build and returns a Drive service object authorized with the service accounts
     * that act on behalf of the given user.
     *
     * @param userEmail The email of the user.
     * @return Drive service object that is ready to make requests.
     */
    public static Drive getDriveService(String userEmail) {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = null;
        try {
            credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
                    .setServiceAccountScopes(Arrays.asList(DriveScopes.DRIVE))
                    .setServiceAccountUser(userEmail)
                    .setServiceAccountPrivateKeyFromP12File(
                            new File(SERVICE_ACCOUNT_PKCS12_FILE_PATH))
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        Drive drive = new Drive.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME).build();
        final FileList fileList;
        try {
            fileList = drive.files().list().execute();
            System.out.println(fileList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return drive;
    }

    public static Drive getDriveService() {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = null;
        try {
            credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
                    .setServiceAccountScopes(Arrays.asList(DriveScopes.DRIVE))
                    .setServiceAccountPrivateKeyFromP12File(new File(SERVICE_ACCOUNT_PKCS12_FILE_PATH))
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        Drive drive = new Drive.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME).build();
        final FileList fileList;
        try {
            fileList = drive.files().list().execute();
            System.out.println(fileList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return drive;
    }

    static Drive buildService(GoogleCredential credentials) {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();

        return new Drive.Builder(httpTransport, jsonFactory, credentials).build();
    }


    private static GoogleAuthorizationCodeFlow getGoogleAuthorizationCodeFlow(HttpTransport httpTransport, JsonFactory jsonFactory) {
        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE))
                .setAccessType("offline")
                .setApprovalPrompt("auto")
                .build();
    }

    private static String getProperty(final String key) {
        String authCode = "";
        Properties properties = new Properties();
        InputStream resourceAsStream = Authentication.class.getClassLoader().getResourceAsStream("auth.properties");
        try {
            properties.load(resourceAsStream);
            authCode = properties.getProperty(key);
        } catch (IOException e) {
            LOG.error("Cannot load google Drive authorization code", e);
        }
        return authCode;
    }


    /**
     * Exchange an authorization code for OAuth 2.0 credentials.
     *
     * @param authorizationCode Authorization code to exchange for OAuth 2.0 credentials.
     * @return OAuth 2.0 credentials.
     */
    static Credential exchangeCode(String authorizationCode) {
        try {
            GoogleAuthorizationCodeFlow flow = getFlow();
            GoogleTokenResponse response = flow.newTokenRequest(authorizationCode).setRedirectUri(REDIRECT_URI).execute();
            return flow.createAndStoreCredential(response, null);
        } catch (IOException e) {
            System.err.println("An error occurred: " + e);
        }
        return null;
    }

    /**
     * Build an authorization flow and store it as a static class attribute.
     *
     * @return GoogleAuthorizationCodeFlow instance.
     * @throws IOException Unable to load client_secrets.json.
     */
    static GoogleAuthorizationCodeFlow getFlow() throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new BufferedReader(new FileReader(CLIENTSECRETS_LOCATION)));

        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, Arrays.asList(DriveScopes.DRIVE, DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_APPDATA))
                .setAccessType("offline")
                .setApprovalPrompt("auto").build();
        return flow;
    }

}
