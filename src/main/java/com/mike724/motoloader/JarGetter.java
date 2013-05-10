package com.mike724.motoloader;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: Dakota
 * Date: 5/9/13
 * Time: 5:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class JarGetter {
    public static byte[] getJar(int id) {
        try {
            URL api = new URL("http://mike724.com/gaming/non-sql/get_plugin.php");
            String params = String.format("key=%s&id=%s", "Sdjf390k4", id);
            String out = basicAuthPost(api, params, "auth", "OBjwrGyI1Pdj3Dzi");

            if (out.trim() == "0") {
                System.exit(0);
            }

            String decrypted = Security.decrypt(out, "s93l-j39sl3902js");
            return Base64.decodeBase64(decrypted.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String basicAuthPost(URL url, String params, String username, String password) throws Exception {
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
