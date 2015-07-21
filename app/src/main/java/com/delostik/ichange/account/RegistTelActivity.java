package com.delostik.ichange.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
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

public class RegistTelActivity extends Activity {

    private String tel = "";
    private JSONObject checkRes;
    boolean nextEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist_tel);

        final Button btn_getCode = (Button)findViewById(R.id.btn_regist_getCode);
        final Button btn_next = (Button)findViewById(R.id.btn_regist_next);

        EditText text = (EditText)findViewById(R.id.edit_regist_tel);
        text.addTextChangedListener(textWatcher);

        btn_next.setEnabled(nextEnabled);
        btn_next.setClickable(nextEnabled);

        btn_getCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edit_tel = (EditText)findViewById(R.id.edit_regist_tel);
                tel = edit_tel.getText().toString();
                if (tel.length() != 11) {
                    Toast.makeText(RegistTelActivity.this, "请填写有效的手机号码", Toast.LENGTH_SHORT).show();
                    return;
                }
                new Thread(checkTel).start();
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edit_code = (EditText)findViewById(R.id.edit_regist_code);
                String code = edit_code.getText().toString();
                EditText edit_tel = (EditText)findViewById(R.id.edit_regist_tel);
                tel = edit_tel.getText().toString();
                if (code.equals("444444")) {
                    Intent intent = new Intent(RegistTelActivity.this, RegistActivity.class);
                    intent.putExtra("tel", tel);
                    Log.i("PHONE============= ", tel);
                    startActivityForResult(intent, 0);
                } else {
                    Log.i("code: ", code);
                    Toast.makeText(RegistTelActivity.this, "验证码不正确", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String json = data.getString("checkTel_res");
            try {
                checkRes = new JSONObject(json);
            } catch (JSONException e) {
                Log.i("JSONException: ", e.getMessage().toString());
            }
            returnCheckRes();
        }
    };

    public void returnCheckRes() {
        String msg = "";
        if (checkRes == JSONObject.NULL) {
            msg = "连接服务器失败";
            Log.i("CHECKTEL: ", "Null Json Object");
        } else {
            int code = 0;
            try {
                code = checkRes.getInt("code");
            } catch (JSONException e) {
                Log.i("LOGIN: ", "Null Code");
            }
            switch(code) {
                case 0:
                    msg = "连接服务器失败";
                    Log.i("CHECKTEL: ", "Failed Reading Code");
                    break;
                case 100:
                    try {
                        msg = checkRes.getString("info");
                    } catch (Exception e) {
                        msg = "连接服务器失败";
                        Log.i("CHECKTEL: ", "[100] Failed Read Info");
                    }
                    break;
                case 200:
                    msg = "验证码已发送";
                    nextEnabled = true;
                    final Button btn_next = (Button)findViewById(R.id.btn_regist_next);
                    btn_next.setEnabled(nextEnabled);
                    btn_next.setClickable(nextEnabled);
                    break;
            }
            Toast.makeText(RegistTelActivity.this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    Runnable checkTel = new Runnable() {
        @Override
        public void run() {
            Map<String,String> params = new HashMap<String, String>();
            params.put("phone", tel);
            String strUrlPath = "http://121.40.194.163/microPay/index.php?s=/Home/User/verifyPhone.html";
            String strResult= HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("checkTel_res", strResult);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        int loginRes = data.getIntExtra("loginRes", 0);
        if (loginRes == 1) {
            Intent intent = new Intent(RegistTelActivity.this, LoginActivity.class);
            intent.putExtra("registRes", 1);
            RegistTelActivity.this.setResult(2, intent);
            RegistTelActivity.this.finish();
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            nextEnabled = false;
            final Button btn_next = (Button)findViewById(R.id.btn_regist_next);
            btn_next.setEnabled(nextEnabled);
            btn_next.setClickable(nextEnabled);
        }
    };
}
