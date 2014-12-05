package com.noe.hypercube;

import com.box.boxjavalibv2.BoxClient;
import com.box.boxjavalibv2.BoxConfigBuilder;
import com.box.boxjavalibv2.IBoxConfig;
import com.box.boxjavalibv2.dao.BoxOAuthToken;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.exceptions.BoxServerException;
import com.box.boxjavalibv2.jsonparsing.BoxJSONParser;
import com.box.boxjavalibv2.jsonparsing.BoxResourceHub;
import com.box.boxjavalibv2.resourcemanagers.IBoxOAuthManager;
import com.box.restclientv2.exceptions.BoxRestException;
import com.noe.hypercube.service.Authentication;
import com.noe.hypercube.service.Box;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class BoxAuthentication extends Authentication<BoxClient> {

    private static final int PORT = 8080;
    private static final String CLIENT_ID = "s0fym1o198dy9k0qaesuiyuvyurnh080";
    private static final String CLIENT_SECRET = "uTCbyzgarF2PQREyBSa59GLoG0VQ6F3R";

    @Override
    public String getAccountName() {
        return Box.getName();
    }

    @Override
    public BoxClient createClient() {
        String code = "";
        String url = "https://www.box.com/api/oauth2/authorize?response_type=code&client_id=" + CLIENT_ID + "&redirect_uri=http%3A//127.0.0.1%3A" + PORT;
        try {
            Desktop.getDesktop().browse(java.net.URI.create(url));
            code = getCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            final BoxClient boxClient = getAuthenticatedClient(code);
            boxClient.setAutoRefreshOAuth(true);
            boxClient.addOAuthRefreshListener(newAuthData -> storeTokens(newAuthData.getRefreshToken(), newAuthData.getAccessToken()));
            return boxClient;
        } catch (BoxRestException e) {
            e.printStackTrace();
        } catch (BoxServerException e) {
            e.printStackTrace();
        } catch (AuthFatalFailureException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public BoxClient getClient(final String refreshToken, final String accessToken) {
        final IBoxConfig config = new BoxConfigBuilder().build();
        final BoxResourceHub hub = new BoxResourceHub();
        final BoxJSONParser parser = new BoxJSONParser(hub);
        BoxClient client = new BoxClient(CLIENT_ID, CLIENT_SECRET, hub, parser, config);
        final IBoxOAuthManager manager = client.getOAuthManager();
        try {
            BoxOAuthToken newToken = manager.refreshOAuth(refreshToken, CLIENT_ID, CLIENT_SECRET);
            storeTokens(newToken.getRefreshToken(), newToken.getAccessToken());
            client.authenticate(newToken);
        } catch (BoxRestException e) {
            e.printStackTrace();
        } catch (BoxServerException e) {
            e.printStackTrace();
        } catch (AuthFatalFailureException e) {
            e.printStackTrace();
        }
        return client;
    }

    private BoxClient getAuthenticatedClient(final String code) throws BoxRestException, BoxServerException, AuthFatalFailureException {
        final BoxResourceHub hub = new BoxResourceHub();
        final BoxJSONParser parser = new BoxJSONParser(hub);
        final IBoxConfig config = new BoxConfigBuilder().build();
        final BoxClient client = new BoxClient(CLIENT_ID, CLIENT_SECRET, hub, parser, config);
        BoxOAuthToken boxOAuthToken = client.getOAuthManager().createOAuth(code, CLIENT_ID, CLIENT_SECRET, "http://localhost:" + PORT);
        client.authenticate(boxOAuthToken);
        storeTokens(client);
        return client;
    }

    private void storeTokens(final BoxClient client) throws AuthFatalFailureException {
        final BoxOAuthToken authData = client.getAuthData();
        storeTokens(authData.getRefreshToken(), authData.getAccessToken());
    }


    private static String getCode() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Socket socket = serverSocket.accept();
        BufferedReader in = new BufferedReader (new InputStreamReader(socket.getInputStream ()));
        while (true)
        {
            String code = "";
            try
            {
                BufferedWriter out = new BufferedWriter (new OutputStreamWriter(socket.getOutputStream ()));
                out.write("HTTP/1.1 200 OK\r\n");
                out.write("Content-Type: text/html\r\n");
                out.write("\r\n");

                code = in.readLine ();
                System.out.println (code);
                String match = "code";
                int loc = code.indexOf(match);

                if( loc >0 ) {
                    int httpstr = code.indexOf("HTTP")-1;
                    code = code.substring(code.indexOf(match), httpstr);
                    String parts[] = code.split("=");
                    code=parts[1];
                    out.write("Thanks for using Hypercube!");
                } else {
                    // It doesn't have a code
                    out.write("Code not found in the URL!");
                }

                out.close();

                return code;
            }
            catch (IOException e)
            {
                //error ("System: " + "Connection to server lost!");
                System.exit (1);
                break;
            }
        }
        return "";
    }
}
