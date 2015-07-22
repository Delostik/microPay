package com.delostik.ichange.Content;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.delostik.ichange.HttpUtils;
import com.delostik.ichange.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class changePayPasswordActivity extends Activity {
    private String pold = new String();
    private String pnew = new String();
    private JSONObject changeRes = new JSONObject();

    @Override
    protected void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepaypassword);

        Button button = (Button)findViewById(R.id.btn_changeLoginPassword);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText old = (EditText)findViewById(R.id.edit_oldLoginPassword);
                EditText new1 = (EditText)findViewById(R.id.edit_newLoginPassword);
                EditText new2 = (EditText)findViewById(R.id.edit_newLoginPassword2);
                pold = old.getText().toString();
                pnew = new1.getText().toString();
                if (pold.equals("")) {
                    Toast.makeText(changePayPasswordActivity.this, "请输入旧密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!pnew.equals(new2.getText().toString())) {
                    Toast.makeText(changePayPasswordActivity.this, "两次新密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pnew.length() != 6) {
                    Toast.makeText(changePayPasswordActivity.this, "密码长度必须为6位", Toast.LENGTH_SHORT).show();
                    return;
                }
                new Thread(changePassword).start();
            }
        });
    }

    Runnable changePassword = new Runnable() {
        @Override
        public void run() {
            Map<String,String> params = new HashMap<String, String>();
            params.put("cookie", getIntent().getStringExtra("cookie"));
            params.put("userId", getIntent().getStringExtra("id"));
            params.put("old_pwd", pold);
            params.put("new_pwd", pnew);
            String strUrlPath = "http://121.40.194.163/microPay/index.php?s=/Home/User/changePayPwd.html";
            String strResult= HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("changeRes", strResult);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String json = data.getString("changeRes");
            try {
                changeRes = new JSONObject(json);
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
        if (changeRes == JSONObject.NULL) {
            msg = "连接服务器失败";
            Log.i("LOGIN: ", "Null Json Object");
        } else {
            int code = 0;
            try {
                code = changeRes.getInt("code");
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
                        msg = changeRes.getString("info");
                    } catch (Exception e) {
                        msg = "连接服务器失败";
                        Log.i("SAVE: ", e.getMessage().toString());
                    }
                    break;
                case 200:
                    msg = "修改成功";
                    break;
            }
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        if (msg.equals("修改成功")) {
            finish();
        }
    }
}
