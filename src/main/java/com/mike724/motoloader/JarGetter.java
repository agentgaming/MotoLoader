package com.mike724.motoloader;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;

import java.util.ArrayList;
import java.util.List;

class JarGetter {
    private static String key = "nXWvOgfgRJKBbbzowle1";
    private static String username = "jxBkqvpe0seZhgfavRqB";
    private static String password = "RXaCcuuQcIUFZuVZik9K";

    protected static byte[] getJar(int id) {
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("id", Integer.toString(id)));
            String out = doPost("https://agentgaming.net/api/get_plugin.php", params);

            if (out.trim() == "0") {
                System.exit(0);
            }

            return Base64.decode(Security.decrypt(out, "s93l-j39sl3902js"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected static ArrayList<Integer> getRequiredJars() {
        try {
            ArrayList<Integer> required = new ArrayList<>();
            String out = doPost("https://agentgaming.net/api/get_required.php", new ArrayList<NameValuePair>());

            if (out.trim() == "00") {
                System.exit(0);
            }

            for (String s : out.split(",")) {
                Integer i = Integer.parseInt(s);
                required.add(i);
            }

            return required;
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
        DefaultHttpClient client = new DefaultHttpClient();

        HttpParams param = client.getParams();
        HttpConnectionParams.setConnectionTimeout(param, 120000);
        HttpConnectionParams.setSoTimeout(param, 120000);

        HttpPost post = new HttpPost(url);
        Header authHeader = new BasicScheme().authenticate(creds, post, new BasicHttpContext());
        post.addHeader(authHeader);
        post.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
        HttpResponse resp = client.execute(post);
        String respString = IOUtils.toString(resp.getEntity().getContent());
        return respString;
    }
}
