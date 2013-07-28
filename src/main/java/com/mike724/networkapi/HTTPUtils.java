package com.mike724.networkapi;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import java.util.List;

public class HTTPUtils {

    /**
     * @param url    the url of the HTTP server
     * @param params the post parameters
     * @param creds  credentials for basic auth
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
        return respString;
    }
}
