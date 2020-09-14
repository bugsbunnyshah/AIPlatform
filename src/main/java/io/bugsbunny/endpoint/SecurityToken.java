package io.bugsbunny.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;

public class SecurityToken implements Serializable {

    private String region;
    private String principal;
    private String token;

    public SecurityToken()
    {

    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public static SecurityToken fromJson(String jsonString)
    {
        SecurityToken securityToken = new SecurityToken();
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        securityToken.principal = jsonObject.get("principal").getAsString();
        securityToken.token = jsonObject.get("access_token").getAsString();
        securityToken.region = jsonObject.get("region").getAsString();
        return securityToken;
    }
}
