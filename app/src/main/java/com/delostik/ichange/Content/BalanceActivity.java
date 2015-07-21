package com.delostik.ichange.Content;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.delostik.ichange.HttpUtils;
import com.delostik.ichange.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;


public class BalanceActivity extends Activity {

    JSONObject res = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        Button btnReturn = (Button)findViewById(R.id.btn_balance_return);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        new Thread(getBalance).start();
    }

    private Handler handler  = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String json = data.getString("res");
            try {
                res = new JSONObject(json);
            } catch (JSONException e) {
                Log.i("JSONException: ", e.getMessage().toString());
            }
            returnPayRes();
        }
    };

    Runnable getBalance = new Runnable() {
        @Override
        public void run() {
            Map<String,String> params = new HashMap<String, String>();
            params.put("userId", getIntent().getStringExtra("id"));
            params.put("cookie", getIntent().getStringExtra("cookie"));
            params.put("number", getIntent().getStringExtra("number"));
            String strUrlPath = "http://121.40.194.163/microPay/index.php?s=/Home/Bankcard/getMoney.html";
            String strResult= HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("res", strResult);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    private void returnPayRes() {
        if (res == JSONObject.NULL) {
            Toast.makeText(BalanceActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
        } else {
            int code = 0;
            try {
                code = res.getInt("code");
            } catch (JSONException e) {
                Toast.makeText(BalanceActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (code) {
                case 0:
                    Toast.makeText(BalanceActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                    break;
                case 100:
                    try {
                        String msg = res.getString("info");
                        Toast.makeText(BalanceActivity.this, msg, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(BalanceActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 200:
                    try {
                        TextView balance = (TextView) findViewById(R.id.edit_balance_balance);
                        balance.setText(String.valueOf(res.getDouble("money")));
                        TextView number = (TextView) findViewById(R.id.edit_balance_number);
                        number.setText(getIntent().getStringExtra("number"));
                }catch (JSONException e) {
                    }

            }
        }
    }
}
