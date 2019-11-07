package com.teammobile.linkedinsignin.ui;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;

import com.teammobile.linkedinsignin.Linkedin;
import com.teammobile.linkedinsignin.R;
import com.teammobile.linkedinsignin.helper.Constants;
import com.teammobile.linkedinsignin.model.LinkedinToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class LinkedinSignInActivity extends Activity {


    /***********************************************************
     * UTILS
     ***********************************************************/
    protected final String AUTHORIZATION_URL = "https://www.linkedin.com/oauth/v2/authorization";
    protected final String ACCESS_TOKEN_URL = "https://www.linkedin.com/oauth/v2/accessToken";
    protected final String RESPONSE_TYPE = "response_type";
    protected final String CLIENT_ID = "client_id";
    protected final String REDIRECT_URI = "redirect_uri";
    protected final String STATE = "state";
    protected final String SCOPE = "scope";
    protected final String CODE = "code";
    protected final String ERROR = "error";
    protected final String ERROR_DESCRIPTION = "error_description";
    protected final String GRANT_TYPE = "grant_type";
    protected final String AUTHORIZATION_CODE = "authorization_code";
    protected final String CLIENT_SECRET = "client_secret";

    private WebView webView;
    private ProgressBar progressBar;

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String state;
    private List<String> scopes;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkedin_sign_in);

        webView = findViewById(R.id.linkedin_web_view);
        webView.clearHistory();
        webView.clearCache(true);
        webView.clearFormData();
        progressBar = findViewById(R.id.progress_bar);
        webView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        clientId = getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE).getString(Constants.CLIENT_ID, null);
        clientSecret = getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE).getString(Constants.CLIENT_SECRET, null);
        redirectUri = getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE).getString(Constants.REDIRECT_URI, null);
        state = getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE).getString(Constants.STATE, null);
        HashSet<String> defaultScope = new HashSet<>();
        defaultScope.add("r_liteprofile");
        scopes = new ArrayList<String>(getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE).getStringSet(Constants.SCOPE, defaultScope));

        this.initWebView();
    }

    private void initWebView() {
        String url = this.generateUrl();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
                    Uri uri = Uri.parse(url);

                    List<String> error = uri.getQueryParameters(ERROR);
                    for(String err : error){
                        Log.d("ERRORURL",err);
                    }
                    if (error != null && error.size() > 0) {
                        finish();
                        Linkedin.linkedinLoginViewResponseListener.linkedinLoginDidFail(uri.getQueryParameters(ERROR_DESCRIPTION));
                        return false;
                    }
                    List<String> paramsAuthCode = uri.getQueryParameters(CODE);
                    if (paramsAuthCode != null && paramsAuthCode.size() > 0) {
                        getAccessToken(paramsAuthCode.get(0));
                    }
                }


                return super.shouldOverrideUrlLoading(view, url);
            }

            @RequiresApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                List<String> error = request.getUrl().getQueryParameters(ERROR);
                if (error != null && error.size() > 0) {
                    finish();
                    Linkedin.linkedinLoginViewResponseListener.linkedinLoginDidFail(request.getUrl().getQueryParameters(ERROR_DESCRIPTION));
                    return false;
                }

                List<String> paramsAuthCode = request.getUrl().getQueryParameters(CODE);
                if (paramsAuthCode != null && paramsAuthCode.size() > 0) {
                    getAccessToken(paramsAuthCode.get(0));
                }

                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        webView.loadUrl(url);
    }

    private String generateUrl() {
        return Uri.parse(AUTHORIZATION_URL)
                .buildUpon()
                .appendQueryParameter(RESPONSE_TYPE, CODE)
                .appendQueryParameter(CLIENT_ID, this.clientId)
                .appendQueryParameter(REDIRECT_URI, this.redirectUri)
                .appendQueryParameter(STATE, this.state)
                .appendQueryParameter(SCOPE, getScopes()).build().toString();
    }

    private String getScopes() {
        String scopeString = "";
        for(String scope : this.scopes){
            scopeString += scope+" ";
        }
        return scopeString.substring(0, scopeString.length() - 1);
    }

    void getAccessToken(final String authCode) {
        progressBar.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
        Thread thread = new Thread(new Runnable(){
            public void run(){
                try {
                    HashMap<String, String> inputs = new HashMap<>();
                    inputs.put(GRANT_TYPE, AUTHORIZATION_CODE);
                    inputs.put(CODE, authCode);
                    inputs.put(REDIRECT_URI, LinkedinSignInActivity.this.redirectUri);
                    inputs.put(CLIENT_ID, LinkedinSignInActivity.this.clientId);
                    inputs.put(CLIENT_SECRET, LinkedinSignInActivity.this.clientSecret);
                    String inputsString = LinkedinSignInActivity.this.getDataString(inputs);
                    byte[] postData = inputsString.getBytes(Charset.forName("UTF-8"));
                    int postDataLength = postData.length;
                    String urlString = ACCESS_TOKEN_URL;
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                    conn.setInstanceFollowRedirects(false);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(getDataString(inputs));
                    os.flush();
                    os.close();

                    StringBuilder sb = new StringBuilder();
                    BufferedReader rd;
                    try {
                        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    } catch (FileNotFoundException e) {
                        rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    }

                    String strCurrentLine;
                    while ((strCurrentLine = rd.readLine()) != null) {
                        sb.append(strCurrentLine);
                    }
                    final JSONObject response = new JSONObject(sb.toString());
                    conn.disconnect();

                        if (response.has("access_token")) {
                            finish();
                            runOnUiThread( new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        LinkedinSignInActivity.this.clearCache();

                                        Linkedin.linkedinLoginViewResponseListener.linkedinDidLoggedIn(new LinkedinToken(response.getString("access_token"), response.getLong("expires_in")));
                                    } catch (JSONException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            finish();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        ArrayList<String> listError = new ArrayList<>();
                                        listError.add(response.getString("error_description"));
                                        Log.d("DEBUG", "LOCATION 3 ");
                                        Linkedin.linkedinLoginViewResponseListener.linkedinLoginDidFail(listError);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void clearCache(){

        CookieManager cookieManager = CookieManager.getInstance();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean value) {
                    //Removed?
                }
            });
            cookieManager.flush();
        } else {
            CookieSyncManager.createInstance(this);
            cookieManager.removeAllCookie();
        }

        new WebView(getApplicationContext()).clearCache(true);

    }


    private String getDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }



}
