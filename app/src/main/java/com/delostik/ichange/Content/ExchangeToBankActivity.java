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
import android.widget.TextView;
import android.widget.Toast;

import com.delostik.ichange.HttpUtils;
import com.delostik.ichange.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ExchangeToBankActivity extends Activity {

    JSONArray card = new JSONArray();

    public static  JSONObject cardInfo = new JSONObject();
    private JSONObject rechargeRes = new JSONObject();
    public static String jsonString = new String();
    private String curId = new String();
    private double n = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchangetobank);

        new Thread(getAllCards).start();

        TextView btn_change = (TextView) findViewById(R.id.btn_change);
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExchangeToBankActivity.this, ExchangeChangeCardActivity.class);
                intent.putExtra("card", jsonString);
                startActivityForResult(intent, 0);
            }
        });

        Button btn_recharge = (Button)findViewById(R.id.btn_confim_recharge);
        btn_recharge.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                EditText num = (EditText)findViewById(R.id.edit_recharge_num);
                n = Double.parseDouble(num.getText().toString());
                if (n <= 0) {
                    Toast.makeText(ExchangeToBankActivity.this, "金额必须大于0元", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    new Thread(recharge).start();
                }
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String json = data.getString("getCardRes");
            try {
                cardInfo = new JSONObject(json);
            } catch (JSONException e) {
                Log.i("JSONException: ", e.getMessage().toString());
            }
            returnCardRes();
        }
    };

    Runnable getAllCards = new Runnable() {
        @Override
        public void run() {
            String cookie = getIntent().getStringExtra("cookie");
            String id = getIntent().getStringExtra("id");
            Map<String,String> params = new HashMap<String, String>();
            params.put("cookie", cookie);
            params.put("userId", id);
            String strUrlPath = "http://121.40.194.163/microPay/index.php?s=/Home/Account/getAllCards.html";
            String strResult= HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            jsonString = strResult;
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("getCardRes", strResult);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    private Handler rechargeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String json = data.getString("rechargeRes");
            try {
                rechargeRes = new JSONObject(json);
            } catch (JSONException e) {
                Log.i("JSONException: ", e.getMessage().toString());
            }
            returnRechargeRes();
        }
    };

    Runnable recharge = new Runnable() {
        @Override
        public void run() {
            String cookie = getIntent().getStringExtra("cookie");
            String id = getIntent().getStringExtra("id");
            Map<String,String> params = new HashMap<String, String>();
            params.put("cookie", cookie);
            params.put("userId", id);
            params.put("id", curId);
            params.put("money", String.valueOf(n));

            String strUrlPath = "http://121.40.194.163/microPay/index.php?s=/Home/account/delMoneyAuto.html";
            String strResult= HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("rechargeRes", strResult);
            msg.setData(data);
            rechargeHandler.sendMessage(msg);
        }
    };

    private void returnRechargeRes() {
        if (rechargeRes == JSONObject.NULL) {
            Toast.makeText(this, "连接服务器失败", Toast.LENGTH_SHORT).show();
            Log.i("LOGIN: ", "Null Json Object");
        } else {
            int code = 0;
            try {
                code = rechargeRes.getInt("code");
            } catch (JSONException e) {
                Log.i("SAVE: ", e.getMessage().toString());
            }

            if (code == 200) {
                Toast.makeText(this, "充值成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ExchangeToBankActivity.this, IndexActivity.class);
                setResult(RESULT_OK, intent);
                intent.putExtra("rechargeRes", "1");
                startActivity(intent);
                finish();
            } else {
                try {
                    Toast.makeText(this, rechargeRes.getString("info"), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                    Log.i("SAVE: ", e.getMessage().toString());
                }
            }
        }
    }

    private void returnCardRes() {

        try {
            card = cardInfo.getJSONArray("res");
        } catch (JSONException e) {
            Log.i("returnCardRes", e.getMessage().toString());
            Toast.makeText(ExchangeToBankActivity.this, "获取银行卡信息失败", Toast.LENGTH_SHORT).show();
            return;
        }

        if (card.length() == 0) {
            Toast.makeText(ExchangeToBankActivity.this, "请先绑定银行卡", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ExchangeToBankActivity.this, IndexActivity.class);
            startActivity(intent);
            return;
        }

        TextView text_id = (TextView) findViewById(R.id.text_id);
        try {
            JSONObject cur = (JSONObject) card.get(0);
            String text = cur.get("number").toString();
            text = text.substring(text.length() - 4);
            text_id.setText("尾号" + text + "银行卡");
            curId = cur.get("id").toString();
        } catch (JSONException e) {
            Log.i("returnCardRes", e.getMessage().toString());
            Toast.makeText(ExchangeToBankActivity.this, "获取银行卡信息失败", Toast.LENGTH_SHORT).show();
            return;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        String result = data.getExtras().getString("choose");
        Log.i("choose", result);
        try {
            JSONObject cur = (JSONObject) card.get(Integer.parseInt(result));
            String text = cur.get("number").toString();
            curId = cur.get("id").toString();
            text = text.substring(text.length() - 4);
            TextView text_id = (TextView) findViewById(R.id.text_id);
            text_id.setText("尾号" + text + "银行卡");
        } catch (JSONException e) {
            Log.i("onActivityResult", e.getMessage().toString());
            Toast.makeText(ExchangeToBankActivity.this, "获取银行卡信息失败", Toast.LENGTH_SHORT).show();
            return;
        }
    }
}
