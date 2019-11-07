package com.teammobile.linkedinsignin;

import com.teammobile.linkedinsignin.model.LinkedinToken;

import java.util.List;

public interface LinkedinLoginViewResponseListener {
    public void linkedinDidLoggedIn(LinkedinToken linkedinToken);
    public void linkedinLoginDidFail(List<String> error);
}