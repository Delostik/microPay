package com.delostik.ichange.Content;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
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

public class AddCardActivity extends Activity{
    private String tel = new String();
    private String cardNo = new String();
    boolean nextEnabled = false;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcard);

        EditText text = (EditText) findViewById(R.id.edit_cardNo);
        text.addTextChangedListener(textWatcher);
        text = (EditText) findViewById(R.id.edit_tel);
        text.addTextChangedListener(textWatcher);

        Button btn_code = (Button) findViewById(R.id.btn_addcard_getCode);
        btn_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText edit_tel = (EditText) findViewById(R.id.edit_tel);
                tel = edit_tel.getText().toString();
                if (tel.length() != 11) {
                    Toast.makeText(AddCardActivity.this, "请填写有效的手机号码", Toast.LENGTH_SHORT).show();
                    return;
                }

                EditText edit_cardNo = (EditText) findViewById(R.id.edit_cardNo);
                cardNo = edit_cardNo.getText().toString();
                Button btn_next = (Button) findViewById(R.id.btn_confirm_addcard);
                nextEnabled = true;
                btn_next.setEnabled(nextEnabled);
                btn_next.setClickable(nextEnabled);

                new Thread(checkBind).start();
            }
        });

        Button btn_next = (Button) findViewById(R.id.btn_confirm_addcard);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(bind).start();
            }

        });
    }

    private Handler handler = new Handler() {
        JSONObject bindRes = new JSONObject();
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String json = data.getString("checkBindRes");
            try {
                bindRes = new JSONObject(json);
            } catch (JSONException e) {
                Log.i("JSONException: ", e.getMessage().toString());
                Toast.makeText(AddCardActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                return;
            }

            int code = 0;
            try {
                code = bindRes.getInt("code");
            } catch (JSONException e) {
                Log.i("LOGIN: ", "Null Code");
                Toast.makeText(AddCardActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                return;
            }
            if (code == 200) {
                Button btn_next = (Button)findViewById(R.id.btn_confirm_addcard);
                nextEnabled = true;
                btn_next.setEnabled(nextEnabled);
                btn_next.setClickable(nextEnabled);
                Toast.makeText(AddCardActivity.this, "短信已发送", Toast.LENGTH_SHORT).show();
            }
            else {
                try {
                    Toast.makeText(AddCardActivity.this, bindRes.getString("info"), Toast.LENGTH_SHORT).show();
                }catch (JSONException e) {
                    Toast.makeText(AddCardActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private Handler bindHandler = new Handler() {
        JSONObject bindRes = new JSONObject();
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String json = data.getString("bindRes");
            try {
                bindRes = new JSONObject(json);
            } catch (JSONException e) {
                Log.i("JSONException: ", e.getMessage().toString());
                Toast.makeText(AddCardActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                return;
            }

            int code = 0;
            try {
                code = bindRes.getInt("code");
            } catch (JSONException e) {
                Log.i("LOGIN: ", "Null Code");
                Toast.makeText(AddCardActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                return;
            }
            if (code == 200) {
                Toast.makeText(AddCardActivity.this, "绑定成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddCardActivity.this, IndexActivity.class);
                startActivity(intent);
            }
            else {
                try {
                    Toast.makeText(AddCardActivity.this, bindRes.getString("info"), Toast.LENGTH_SHORT).show();
                }catch (JSONException e) {
                    Toast.makeText(AddCardActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    Runnable checkBind = new Runnable() {
        @Override
        public void run() {
            Map<String,String> params = new HashMap<String, String>();
            params.put("cookie", getIntent().getStringExtra("cookie"));
            params.put("userId", getIntent().getStringExtra("id"));
            params.put("number", cardNo);
            params.put("phone", tel);
            String strUrlPath = "http://121.40.194.163/microPay/index.php?s=/Home/Account/verifyCard.html";
            String strResult= HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("checkBindRes", strResult);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    Runnable bind = new Runnable() {
        @Override
        public void run() {
            Map<String,String> params = new HashMap<String, String>();
            params.put("cookie", getIntent().getStringExtra("cookie"));
            params.put("userId", getIntent().getStringExtra("id"));
            params.put("number", cardNo);
            params.put("phone", tel);
            EditText code = (EditText)findViewById(R.id.edit_code);
            params.put("verifyCode", code.getText().toString());
            String strUrlPath = "http://121.40.194.163/microPay/index.php?s=/Home/Account/addCard.html";
            String strResult= HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("bindRes", strResult);
            msg.setData(data);
            bindHandler.sendMessage(msg);
        }
    };

    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            nextEnabled = false;
            final Button btn_next = (Button)findViewById(R.id.btn_confirm_addcard);
            btn_next.setEnabled(nextEnabled);
            btn_next.setClickable(nextEnabled);
        }
    };

}
