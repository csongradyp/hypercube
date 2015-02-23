package com.noe.hypercube;

import com.box.sdk.BoxAPIConnection;
import com.noe.hypercube.service.Authentication;
import com.noe.hypercube.service.Box;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoxAuthentication extends Authentication<BoxAPIConnection> {

    private static final Logger LOG = LoggerFactory.getLogger(BoxAuthentication.class);

    private static final int PORT = 8080;
    private static final String CLIENT_ID = "s0fym1o198dy9k0qaesuiyuvyurnh080";
    private static final String CLIENT_SECRET = "uTCbyzgarF2PQREyBSa59GLoG0VQ6F3R";

    @Override
    public String getAccountName() {
        return Box.getName();
    }

    @Override
    public BoxAPIConnection createClient() {
        String code = "";
        String url = "https://www.box.com/api/oauth2/authorize?response_type=code&client_id=" + CLIENT_ID + "&redirect_uri=http%3A//127.0.0.1%3A" + PORT;
        try {
            Desktop.getDesktop().browse(java.net.URI.create(url));
            code = getCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final BoxAPIConnection boxClient = new BoxAPIConnection(CLIENT_ID, CLIENT_SECRET, code);
        boxClient.refresh();
        storeTokens(boxClient.getAccessToken(), boxClient.getRefreshToken());
        return boxClient;
    }

    @Override
    public BoxAPIConnection getClient(final String refreshToken, final String accessToken) {
        return new BoxAPIConnection(CLIENT_ID, CLIENT_SECRET, accessToken, refreshToken);
    }

    private static String getCode() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Socket socket = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        while (true) {
            String code = "";
            try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                out.write("HTTP/1.1 200 OK\r\n");
                out.write("Content-Type: text/html\r\n");
                out.write("\r\n");

                code = in.readLine();
                String match = "code";
                int loc = code.indexOf(match);

                if (loc > 0) {
                    int httpstr = code.indexOf("HTTP") - 1;
                    code = code.substring(code.indexOf(match), httpstr);
                    String parts[] = code.split("=");
                    code = parts[1];
                    out.write("Thanks for using Hypercube!");
                } else {
                    LOG.error("Code not found in the URL!");
                }
                return code;
            } catch (IOException e) {
                LOG.error( "Connection to server lost!");
                break;
            }
        }
        return "";
    }
}
