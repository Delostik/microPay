package com.delostik.ichange;

import com.delostik.ichange.account.LoginActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent login =  new Intent(this, LoginActivity.class);
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startActivity(login);
                MainActivity.this.finish();
            }
        };
        timer.schedule(task, 1000 * 2);
    }

}
