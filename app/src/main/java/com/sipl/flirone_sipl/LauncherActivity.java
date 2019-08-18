package com.sipl.flirone_sipl;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LauncherActivity extends AppCompatActivity {

    //time var
    private static int SPLASH_TIME_OUT = 5000; // 5000 ms = 5 secs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        // note that most of the code below, is actually inside "runnable".
        // the line is:   new Handler().postDelayed(new Runnable(){}, SPLASH_TIME_OUT);
        // this is not good code probably. fix this later.
        //add ...
        new Handler().postDelayed(new Runnable() {
            //
            @Override
            //...
            public void run() {
                //
                Intent homeIntent = new Intent(LauncherActivity.this, InitActivity.class);
                //
                startActivity(homeIntent);
                //
                finish();
            }
        } , SPLASH_TIME_OUT);
    }
}
