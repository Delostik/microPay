package com.delostik.ichange.account;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.delostik.ichange.Content.IndexActivity;
import com.delostik.ichange.HttpUtils;
import com.delostik.ichange.Loading;
import com.delostik.ichange.R;

import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity {
    private String username = new String();
    private String password = new String();
    private JSONObject loginRes = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (isConnect(this)==false)
        {
            new AlertDialog.Builder(this)
                    .setTitle("网络错误")
                    .setMessage("网络连接失败，请确认网络连接")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            finish();
                        }
                    }).show();
            return;
        }

        JSONObject json = new JSONObject();

        try {
            FileInputStream fin = openFileInput("account");
            byte[] buffer = new byte[fin.available()];
            try {
                fin.read(buffer);
                String account = EncodingUtils.getString(buffer, "UTF-8");
                json = new JSONObject(account);
            } catch(Exception e) {
                Log.i("SAVE", e.getMessage().toString());
                throw new FileNotFoundException();
            }
        } catch (Exception e) {
            Log.i("SAVE: ", e.getMessage().toString());
        }

        if (json != JSONObject.NULL) {
            try {
                username = json.getString("username");
                if ("".equals(username)) throw new JSONException("123");
                password = json.getString("password");
                EditText edit_username = (EditText)findViewById(R.id.edit_login_username);
                edit_username.setText(username);
                EditText edit_password = (EditText)findViewById(R.id.edit_login_password);
                edit_password.setText(password);

                Loading.show(this);
                new Thread(login).start();
            } catch (JSONException e) {
                Log.i("SAVE: ", e.getMessage().toString());
            }
        }
        final Button btn_regist = (Button)findViewById(R.id.btn_login_regist);
        btn_regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistTelActivity.class);
                startActivityForResult(intent, 2);
            }
        });

        final Button btn_login = (Button)findViewById(R.id.btn_login_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edit_username = (EditText)findViewById(R.id.edit_login_username);
                username = edit_username.getText().toString();
                EditText edit_password = (EditText)findViewById(R.id.edit_login_password);
                password = edit_password.getText().toString();
                Loading.show(LoginActivity.this);
                new Thread(login).start();
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String json = data.getString("login_res");
            try {
                loginRes = new JSONObject(json);
            } catch (JSONException e) {
                Log.i("JSONException: ", e.getMessage().toString());
            }
            returnLoginRes();
        }
    };

    public void returnLoginRes() {
        String msg = new String("UTF-8");
        String cookie = new String();
        String id = new String();
        if (loginRes == JSONObject.NULL) {
            msg = "连接服务器失败";
            Log.i("LOGIN: ", "Null Json Object");
        } else {
            int code = 0;
            try {
                code = loginRes.getInt("code");
            } catch (JSONException e) {
                Log.i("SAVE: ", e.getMessage().toString());
            }
            switch(code) {
                case 0:
                    msg = "连接服务器失败";
                    Log.i("LOGIN: ", "[0] Failed Read Code");
                    break;
                case 100:
                    try {
                        msg = loginRes.getString("info");
                    } catch (Exception e) {
                        msg = "连接服务器失败";
                        Log.i("SAVE: ", e.getMessage().toString());
                    }
                    break;
                case 200:
                    try {
                        cookie = loginRes.getString("cookie");
                        id = loginRes.getString("id");
                        msg = "登陆成功";
                    } catch (JSONException e) {
                        msg = "连接服务器失败";
                        Log.i("SAVE: ", e.getMessage().toString());
                    }
                    break;
            }
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Loading.cancel();
        if (msg.equals("登陆成功")) {
            final Intent index =  new Intent(this, IndexActivity.class);
            index.putExtra("cookie", cookie);
            index.putExtra("username", username);
            index.putExtra("password", password);
            index.putExtra("id", id);
            startActivity(index);
            LoginActivity.this.finish();
        }
    }

    Runnable login = new Runnable() {
        @Override
        public void run() {
            Map<String,String> params = new HashMap<String, String>();
            params.put("name", username);
            params.put("password", password);
            String strUrlPath = "http://121.40.194.163/microPay/index.php?s=/Home/User/login.html";
            String strResult= HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("login_res", strResult);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        int registRes = data.getIntExtra("registRes", 0);
        if (registRes == 1) {
            LoginActivity.this.finish();
        }
    }

    public static boolean isConnect(Context context) {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                // 获取网络连接管理的对象
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null&& info.isConnected()) {
                    // 判断当前网络是否已经连接
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.v("error",e.toString());
        }
        return false;
    }

}
