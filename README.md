# Linkedin Android SDK (Unofficial)

LinkedinSDK allows you to get linkedin access token inside your Android application. Inspired by [linkedin-android-sdk](https://github.com/ovidos/linkedin-android-sdk) but support Android API 17 min.

# Installation

To install the SDK:

Add it in your root build.gradle at the end of repositories:

    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }

Add the dependency:

`implementation 'com.github.pboulch:linkedin-android-sdk:0.1.0'`

For login flow to work, LinkedinSignInActivity needs to be added to AndroidManifest.xml:

`<activity android:name="com.teammobile.linkedinsignin.ui.LinkedinSignInActivity"/>`

Also, you need to add internet permission to AndroidManifest.xml:

`<uses-permission android:name="android.permission.INTERNET" />`

# Authentication 

Linkedin SDK is allows you to authenticate with OAuth2 authentication. This means that, you are allowed to manage other people's account that are authorized with your application.

# Getting Started

First of all you need to retrieve Client ID, Client Secret and Redirect Uri from Linkedin. To add Client ID, Client Secret and Redirect Uri into your application:

1. Open your Application Class and add the following
              

	Linkedin.initialize(getApplicationContext(),
                	"LINKEDIN_CLIENT_ID",
                	"LINKEDIN_CLIENT_SECRET",
                	"LINKEDIN_REDIRECT_URI",
                	"RANDOM_STRING",
                	Arrays.asList("r_liteprofile", "r_emailaddress"));
            
"State" is a unique string of your choice designed to protect against CSRF attacks.

2. To authorize a user for your application: 
          
 
	Linkedin.login(this, new LinkedinLoginViewResponseListener() {
          	@Override
          	public void linkedinDidLoggedIn(LinkedinToken linkedinToken) {
              	// Success
          	}

          	@Override
          	public void linkedinLoginDidFail(String error) {
              	// Fail
          	}
	});
        
