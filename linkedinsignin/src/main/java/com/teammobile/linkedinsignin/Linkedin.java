package com.teammobile.linkedinsignin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.teammobile.linkedinsignin.helper.Constants;
import com.teammobile.linkedinsignin.model.LinkedinToken;
import com.teammobile.linkedinsignin.ui.LinkedinSignInActivity;

import java.util.HashSet;
import java.util.List;

public class Linkedin {
    public static LinkedinLoginViewResponseListener linkedinLoginViewResponseListener;

    public void initialize(Context context, String clientId, String clientSecret, String redirectUri, String state, List<String>scopes) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE).edit();
        editor.putString(Constants.CLIENT_ID, clientId);
        editor.putString(Constants.CLIENT_SECRET, clientSecret);
        editor.putString(Constants.REDIRECT_URI, redirectUri);
        editor.putString(Constants.STATE, state);
        editor.putStringSet(Constants.SCOPE, new HashSet<>(scopes));
        editor.apply();
    }

    public void login(Context context, LinkedinLoginViewResponseListener listener) {
        this.linkedinLoginViewResponseListener = listener;
        Intent loginActivity = new Intent(context, LinkedinSignInActivity.class);
        context.startActivity(loginActivity);
    }

}


