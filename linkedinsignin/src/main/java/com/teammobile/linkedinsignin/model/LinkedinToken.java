package com.teammobile.linkedinsignin.model;

public class LinkedinToken {

    public String accessToken;
    public Long expiredTime;

    public LinkedinToken(String accessToken, Long expiredTime){
        this.accessToken = accessToken;
        this.expiredTime = expiredTime;
    }
}
