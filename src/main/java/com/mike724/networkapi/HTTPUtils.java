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
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        SSLSocketFactory socketFactory = new SSLSocketFactory(ks);
        Scheme sch = new Scheme("https", 443, socketFactory);

        HttpClient client = new DefaultHttpClient();
        client.getConnectionManager().getSchemeRegistry().register(sch);
        HttpPost post = new HttpPost(url);
        Header authHeader = new BasicScheme().authenticate(creds, post, new BasicHttpContext());
        post.addHeader(authHeader);
        post.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
        HttpResponse resp = client.execute(new HttpHost("172.245.28.35"), post);
        String respString = EntityUtils.toString(resp.getEntity()).trim();
        System.out.println(respString);
        return respString;
    }

    /**
     * @param url  the url of the HTTP server
     * @param params the post parameters in url format
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
