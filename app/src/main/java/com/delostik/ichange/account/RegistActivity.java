package com.delostik.ichange.account;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
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
    private AlertDialog longinDialog;
    private View longinDialogView;

    private String cookie = new String();
    private String id = new String();
    private String paypsw = new String();

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
                    Toast.makeText(RegistActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(edit_password2.getText().toString())) {
                    Toast.makeText(RegistActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }
                pswDialog();
            }
        });
    }

    public void pswDialog () {
        Context mContext = RegistActivity.this;

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        longinDialogView = layoutInflater.inflate(R.layout.setpsw, null);

        longinDialog = new AlertDialog.Builder(mContext).setTitle("设置支付密码").setView(longinDialogView).create();
        longinDialog.show();

        Button confirm = (Button)longinDialogView.findViewById(R.id.btn_confirm_set);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText psw1 = (EditText)longinDialogView.findViewById(R.id.setpsw1);
                String p1 = psw1.getText().toString();
                Log.i("okok", p1);
                EditText psw2 = (EditText)longinDialogView.findViewById(R.id.setpsw2);
                String p2 = psw2.getText().toString();
                Log.i("okok", p2);
                if (p1.length() != 6) {
                    Toast.makeText(RegistActivity.this, "密码长度必须是6位！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!p1.equals(p2)) {
                    Toast.makeText(RegistActivity.this, "两次输入的密码不一致！", Toast.LENGTH_SHORT).show();
                    return;
                }
                paypsw = p1;
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

    private Handler changeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
                Intent lastIntent = new Intent(RegistActivity.this, RegistTelActivity.class);
                lastIntent.putExtra("loginRes", 1);
                RegistActivity.this.setResult(0, lastIntent);
                Intent index = new Intent(RegistActivity.this, IndexActivity.class);
                index.putExtra("cookie", cookie);
                index.putExtra("username", username);
                index.putExtra("password", password);
                index.putExtra("id", id);
                startActivity(index);
                RegistActivity.this.finish();
        }
    };

    private Handler loginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String ret = data.getString("login_res");
            if (ret.equals("登陆成功")) {
                cookie = data.getString("cookie");
                new Thread(changePsw).start();
            }
            else {
                Log.e("what", "wtf");
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
                            id = loginRes.getString("id");
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
            data.putString("id", id);
            message.setData(data);
            loginHandler.sendMessage(message);
        }
    };

    Runnable changePsw = new Runnable() {
        @Override
        public void run() {
            Map<String,String> params = new HashMap<String, String>();
            params.put("cookie", cookie);
            params.put("id", id);
            params.put("pwd", paypsw);
            String strUrlPath = "http://121.40.194.163/microPay/index.php?s=/Home/User/setPayPwd.html";
            String strResult= HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("changeRes", strResult);
            msg.setData(data);
            changeHandler.sendMessage(msg);
        }
    };

}
