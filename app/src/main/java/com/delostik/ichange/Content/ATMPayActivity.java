package com.delostik.ichange.Content;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.delostik.ichange.HttpUtils;
import com.delostik.ichange.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ATMPayActivity extends Activity {

    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;

    double money = 0;
    String machindId = new String();
    private JSONObject payRes = new JSONObject();

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atm);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(ATMPayActivity.this, "找不到NFC设备", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(ATMPayActivity.this, "请在设置中启用NFC设备", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        processIntent(intent);
    }

    void processIntent(Intent intent)
    {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        String nfc = TextRecord.parse(msg.getRecords()[0]).getText();

        if (nfc.contains("#")) {
            machindId = nfc.split("#")[0];
            money = Double.parseDouble(nfc.split("#")[1]);
            Log.i("machineid", machindId);
        }
        else {
            return;
        }

        new Thread(dopay).start();
    }

    private Handler payHandler  = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String json = data.getString("payRes");
            Log.i("ss", json);

            try {
                payRes = new JSONObject(json);
            } catch (JSONException e) {
                Log.i("JSONException: ", e.getMessage().toString());
            }
            returnPayRes();
        }
    };

    Runnable dopay = new Runnable() {
        @Override
        public void run() {
            Map<String,String> params = new HashMap<String, String>();
            params.put("userId", getIntent().getStringExtra("id"));
            params.put("machineId", machindId);
            params.put("cookie", getIntent().getStringExtra("cookie"));
            params.put("money", String.valueOf(money));
            String strUrlPath = "http://121.40.194.163/microPay/index.php?s=/Home/User/drawFromMachine.html";
            String strResult= HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("payRes", strResult);
            msg.setData(data);
            payHandler.sendMessage(msg);
        }
    };

    private void returnPayRes() {
        if (payRes == JSONObject.NULL) {
            Toast.makeText(ATMPayActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
        } else {
            int code = 0;
            try {
                code = payRes.getInt("code");
            } catch (JSONException e) {
                Toast.makeText(ATMPayActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (code) {
                case 0:
                    Toast.makeText(ATMPayActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                    break;
                case 100:
                    try {
                        String msg = payRes.getString("info");
                        Toast.makeText(ATMPayActivity.this, msg, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(ATMPayActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 200:
                    Toast.makeText(ATMPayActivity.this, "取款成功，取款" + String.valueOf(money) + "元", Toast.LENGTH_SHORT).show();
            }
        }
    }

}