package com.delostik.ichange.account;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Handler;

import com.delostik.ichange.Content.IndexActivity;
import com.delostik.ichange.HttpUtils;
import com.delostik.ichange.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class RegistActivity extends Activity {
    private String username;
    private String password;
    private String tel;
    private JSONObject registRes;
    private JSONObject loginRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        Intent intent = getIntent();
        tel = intent.getStringExtra("tel");

        Button btn_regist = (Button)findViewById(R.id.btn_regist_regist);
        btn_regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = "";
                EditText edit_username = (EditText)findViewById(R.id.edit_regist_username);
                EditText edit_password1 = (EditText)findViewById(R.id.edit_regist_password1);
                EditText edit_password2 = (EditText)findViewById(R.id.edit_regist_password2);
                username = edit_username.getText().toString();
                if (username.length() == 0) {
                    Toast.makeText(RegistActivity.this, "请输入用户名", Toast.LENGTH_SHORT);
                    return;
                }
                password = edit_password1.getText().toString();
                if (password.length() == 0) {
                    Toast.makeText(RegistActivity.this, "请输入密码", Toast.LENGTH_SHORT);
                    return;
                }
                if (!password.equals(edit_password2.getText().toString())) {
                    Toast.makeText(RegistActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT);
                    return;
                }
                new Thread(regist).start();
            }
        });
    }

    private Handler regHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String json = data.getString("regist_res");
            try {
                registRes = new JSONObject(json);
            } catch (JSONException e) {
                Log.i("JSONException: ", e.getMessage().toString());
            }
            returnRegistRes();
        }
    };

    private Handler loginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String ret = data.getString("login_res");
            if (ret.equals("登陆成功")) {
                Intent lastIntent = new Intent(RegistActivity.this, RegistTelActivity.class);
                lastIntent.putExtra("loginRes", 1);
                RegistActivity.this.setResult(0, lastIntent);
                String cookie = data.getString("cookie");
                String id = data.getString("id");
                Intent index = new Intent(RegistActivity.this, IndexActivity.class);
                index.putExtra("cookie", cookie);
                index.putExtra("username", username);
                index.putExtra("password", password);
                index.putExtra("id", id);
                startActivity(index);
                RegistActivity.this.finish();
            }
        }
    };

    public void returnRegistRes() {
        String msg = "";
        if (registRes == JSONObject.NULL) {
            msg = "连接服务器失败";
            Log.i("REGIST: ", "Null Json Object");
        } else {
            int code = 0;
            try {
                code = registRes.getInt("code");
            } catch (JSONException e) {
                Log.i("REGIST: ", "Null Code");
            }
            switch(code) {
                case 0:
                    msg = "连接服务器失败";
                    Log.i("REGIST: ", "Failed Reading Code");
                    break;
                case 100:
                    try {
                        msg = registRes.getString("info");
                    } catch (Exception e) {
                        msg = "连接服务器失败";
                        Log.i("REGIST: ", "[100] Failed Read Info");
                    }
                    break;
                case 200:
                    msg = "注册成功，正在登陆";
                    break;
            }
            Toast.makeText(RegistActivity.this, msg, Toast.LENGTH_LONG).show();
        }
        if (msg.equals("注册成功，正在登陆")) {
            new Thread(login).start();
        }
    }

    Runnable regist = new Runnable() {
        @Override
        public void run() {
            Map<String,String> params = new HashMap<String, String>();
            params.put("phone", tel);
            Log.i("phone-----------------", tel);
            params.put("name", username);
            params.put("password", password);
            String strUrlPath = "http://121.40.194.163/microPay/index.php?s=/Home/User/reg.html";
            String strResult= HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("regist_res", strResult);
            msg.setData(data);
            regHandler.sendMessage(msg);
        }
    };

    Runnable login = new Runnable() {
        @Override
        public void run() {
            Map<String, String> params = new HashMap<String, String>();
            params.put("name", username);
            params.put("password", password);
            String strUrlPath = "http://121.40.194.163/microPay/index.php?s=/Home/User/login.html";
            String strResult = HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            try {
                loginRes = new JSONObject(strResult);
            } catch (JSONException e) {
                Log.i("JSONException: ", e.getMessage().toString());
            }

            String msg = new String("UTF-8");
            String cookie = "";
            if (loginRes == JSONObject.NULL) {
                msg = "连接服务器失败";
                Log.i("LOGIN: ", "Null Json Object");
            } else {
                int code = 0;
                try {
                    code = loginRes.getInt("code");
                } catch (JSONException e) {
                    Log.i("LOGIN: ", "Null Code");
                }
                switch (code) {
                    case 0:
                        msg = "连接服务器失败";
                        Log.i("LOGIN: ", "[0] Failed Read Code");
                        break;
                    case 100:
                        try {
                            msg = loginRes.getString("info");
                        } catch (Exception e) {
                            msg = "连接服务器失败";
                            Log.i("LOGIN: ", "[100] Failed Read Info");
                        }
                        break;
                    case 200:
                        try {
                            cookie = loginRes.getString("cookie");
                            msg = "登陆成功";
                        } catch (JSONException e) {
                            msg = "连接服务器失败";
                            Log.i("LOGIN: ", "[200] Failed Read cookie");
                        }
                        break;
                }
            }
            Message message = new Message();
            Bundle data = new Bundle();
            data.putString("login_res", msg);
            data.putString("cookie", cookie);
            message.setData(data);
            loginHandler.sendMessage(message);
        }
    };

}
