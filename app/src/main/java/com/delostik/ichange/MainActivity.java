package com.delostik.ichange;

import com.delostik.ichange.account.LoginActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import java.util.Timer;
import java.util.TimerTask;
import com.delostik.ichange.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView runWebView = (WebView)findViewById(R.id.mainwebview);
        runWebView.loadDataWithBaseURL(null, "<HTML><body bgcolor='#000000'style='margin:0'><div align=center><IMG src='file:///android_asset/loading.gif'width='100%' height='100%'/></div></body></html>", "text/html", "UTF-8", null);

        final Intent login =  new Intent(this, LoginActivity.class);
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startActivity(login);
                MainActivity.this.finish();
            }
        };
        timer.schedule(task, 1000 * 4);


    }

}
