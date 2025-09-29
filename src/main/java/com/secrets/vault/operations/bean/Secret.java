package com.secrets.vault.operations.bean;


import com.google.gson.annotations.SerializedName;

public class Secret {
    @SerializedName("appName")
    private String appName;
    @SerializedName("secret")
    private String secret;

    public Secret(){ }

    public Secret(String appName,String secret){
        this.appName = appName;
        this.secret = secret;
    };

    public String getAppName(){
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getSecret(){
        return secret;
    }

    public void setSecret(String secret){
        this.secret = secret;
    }

    @Override
    public String toString() {
        return "Secret : {AppName='" + appName + "', Secret='" + secret + "'}";
    }
}
