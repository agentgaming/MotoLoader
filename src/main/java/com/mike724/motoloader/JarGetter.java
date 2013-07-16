package com.mike724.motoloader;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.message.BasicNameValuePair;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.mike724.networkapi.HTTPUtils.basicAuthPost;

class JarGetter {
    public static byte[] getJar(int id) {
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("id", Integer.toString(id)));
            String out = MotoLoader.getInstance().getDataStorage().doPost("https://agentgaming.net/api/get_plugin.php", params);

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
}
