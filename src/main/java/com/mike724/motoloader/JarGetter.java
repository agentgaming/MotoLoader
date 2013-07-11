package com.mike724.motoloader;

import org.apache.commons.codec.binary.Base64;

import java.net.URL;

import static com.mike724.networkapi.HTTPUtils.basicAuthPost;

class JarGetter {
    public static byte[] getJar(int id) {
        try {
            URL api = new URL("http://meyernet.co/api/get_plugin.php");
            String params = String.format("key=%s&id=%s", "nXWvOgfgRJKBbbzowle1", id);
            String out = basicAuthPost(api, params, "jxBkqvpe0seZhgfavRqB", "RXaCcuuQcIUFZuVZik9K");

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
