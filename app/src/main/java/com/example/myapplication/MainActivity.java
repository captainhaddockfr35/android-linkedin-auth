package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.teammobile.linkedinsignin.Linkedin;
import com.teammobile.linkedinsignin.LinkedinLoginViewResponseListener;
import com.teammobile.linkedinsignin.model.LinkedinToken;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Linkedin linkedinConnect = new Linkedin();
        ArrayList<String> scopes = new ArrayList<>();
        scopes.add("r_emailaddress");
        scopes.add("r_liteprofile");
        scopes.add("w_member_social");
        linkedinConnect.initialize(getApplicationContext(), "", "", "", "TEST", scopes);

        Button b = (Button)findViewById(R.id.linkedinButton);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linkedinConnect.login(getApplicationContext(), new LinkedinLoginViewResponseListener() {
                    @Override
                    public void linkedinDidLoggedIn(LinkedinToken linkedinToken) {
                        Log.d("LINKEDIN_SUCCESS",linkedinToken.accessToken);
                    }

                    @Override
                    public void linkedinLoginDidFail(List<String> error) {
                        Log.d("LINKEDIN_FAILED", error.get(0));
                    }
                });
            }
        });
    }
}
