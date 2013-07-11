package com.mike724.networkapi;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPUtils {
    /**
     * @param url  the url of the HTTP server
     * @param params the post paramaters in url format
     * @param username the HTTP authorization username
     * @param password the HTTP authorization password
     * @return the HTTP response
     * @see String
     */
    public static String basicAuthPost(URL url, String params, String username, String password) throws Exception {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setInstanceFollowRedirects(false);
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept-Encoding", "gzip, deflate");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("Content-Length", "" + Integer.toString(params.getBytes().length));
        con.setUseCaches(false);
        con.setRequestProperty("Authorization", "Basic " + Base64.encodeBase64String((username + ":" + password).getBytes()).replaceAll("\n", ""));
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(params);
        wr.flush();
        wr.close();
        String out = IOUtils.toString(con.getInputStream());
        con.disconnect();
        return out;
    }
}
