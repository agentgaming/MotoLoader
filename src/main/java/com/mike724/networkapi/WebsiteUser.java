package com.mike724.networkapi;

public class WebsiteUser {
    private String username;
    private String accessToken;

    public WebsiteUser(String username) {
        this.username = username;
        this.accessToken = "";
    }

    public String getUsername() {
        return username;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
