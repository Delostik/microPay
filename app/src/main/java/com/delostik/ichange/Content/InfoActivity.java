package com.delostik.ichange.Content;


import android.app.Activity;
import android.content.Intent;
import android.net.LinkAddress;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.delostik.ichange.R;
import com.delostik.ichange.account.LoginActivity;

import org.json.JSONObject;

import java.io.FileOutputStream;

public class InfoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        LinearLayout btnExit = (LinearLayout)findViewById(R.id.btn_exit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeCookie();
            }
        });

        LinearLayout btnChangeLoginPassword = (LinearLayout)findViewById(R.id.btn_changePassword);
        btnChangeLoginPassword.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InfoActivity.this, changePasswordActivity.class);
                intent.putExtra("cookie", getIntent().getStringExtra("cookie"));
                intent.putExtra("id", getIntent().getStringExtra("id"));
                startActivity(intent);
            }
        });

        LinearLayout btnChangePayPassword = (LinearLayout)findViewById(R.id.btn_changePayPassword);
        btnChangePayPassword.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InfoActivity.this, changePasswordActivity.class);
                intent.putExtra("cookie", getIntent().getStringExtra("cookie"));
                intent.putExtra("id", getIntent().getStringExtra("id"));
                startActivity(intent);
            }
        });
    }

    public void writeCookie() {
        try {
            JSONObject json = new JSONObject();
            json.put("username", "");
            json.put("password", "");
            FileOutputStream fout = openFileOutput("account", MODE_PRIVATE);
            fout.write(json.toString().getBytes());
            fout.close();
            Toast.makeText(InfoActivity.this, "退出成功", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(InfoActivity.this, LoginActivity.class);
            startActivity(intent);
            IndexActivity.context.finish();
            this.finish();
        } catch(Exception e) {
            Toast.makeText(InfoActivity.this, "删除登录信息失败，请检查权限", Toast.LENGTH_SHORT).show();
        }
    }

}
