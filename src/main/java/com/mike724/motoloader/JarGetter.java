package com.mike724.motoloader;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

class JarGetter {
    private static String key = "nXWvOgfgRJKBbbzowle1";
    private static String username = "jxBkqvpe0seZhgfavRqB";
    private static String password = "RXaCcuuQcIUFZuVZik9K";

    public static byte[] getJar(int id) {
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("id", Integer.toString(id)));
            String out = doPost("https://agentgaming.net/api/get_plugin.php", params);

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

    private static String doPost(String url, List<NameValuePair> params) throws Exception {
        List<NameValuePair> data = new ArrayList<>();
        data.add(new BasicNameValuePair("key", key));
        data.addAll(params);

        Credentials creds = new UsernamePasswordCredentials(username, password);

        return basicAuthPost(url, data, creds);
    }

    private static String basicAuthPost(String url, List<NameValuePair> params, Credentials creds) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        Header authHeader = new BasicScheme().authenticate(creds, post, new BasicHttpContext());
        post.addHeader(authHeader);
        post.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
        HttpResponse resp = client.execute(post);
        String respString = EntityUtils.toString(resp.getEntity()).trim();
        return respString;
    }
}
