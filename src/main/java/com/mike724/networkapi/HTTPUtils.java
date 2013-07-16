package com.mike724.networkapi;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.util.List;

public class HTTPUtils {

    /**
     * @param url  the url of the HTTP server
     * @param params the post parameters
     * @param creds credentials for basic auth
     * @return the HTTP response
     */
    public static String basicAuthPost(String url, List<NameValuePair> params, Credentials creds) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        Header authHeader = new BasicScheme().authenticate(creds, post, new BasicHttpContext());
        post.addHeader(authHeader);
        post.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
        HttpResponse resp = client.execute(post);
        String respString = EntityUtils.toString(resp.getEntity()).trim();
        System.out.println(respString);
        return respString;
    }
}
