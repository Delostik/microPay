package com.delostik.ichange.Content;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.*;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.delostik.ichange.HttpUtils;
import com.delostik.ichange.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class IndexActivity extends ActionBarActivity {

    public static Activity context;

    private JSONObject getBalanceRes = new JSONObject();
    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        context = this;
        getSupportActionBar().hide();
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        new Thread(getBalance).start();
                    }
                })
        .setup(mPullToRefreshLayout);

        new Thread(getBalance).start();

        writeCookie();

        LinearLayout btnExchange = (LinearLayout)findViewById(R.id.btn_exchange);
        btnExchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IndexActivity.this, ExchangeToBankActivity.class);
                intent.putExtra("cookie", getIntent().getStringExtra("cookie"));
                intent.putExtra("id", getIntent().getStringExtra("id"));
                startActivity(intent);
            }
        });

        LinearLayout btnPay = (LinearLayout)findViewById(R.id.btn_pay);
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IndexActivity.this, ChoosePaymentActivity.class);
                intent.putExtra("cookie", getIntent().getStringExtra("cookie"));
                intent.putExtra("id", getIntent().getStringExtra("id"));
                startActivity(intent);
            }
        });

        LinearLayout btnRecieve = (LinearLayout)findViewById(R.id.btn_receive);
        btnRecieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IndexActivity.this, ReceiveActivity.class);
                intent.putExtra("username",getIntent().getStringExtra("username"));
                intent.putExtra("cookie", getIntent().getStringExtra("cookie"));
                intent.putExtra("id", getIntent().getStringExtra("id"));
                startActivity(intent);
            }
        });

        LinearLayout btnRecharge = (LinearLayout)findViewById(R.id.btn_recharge);
        btnRecharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IndexActivity.this, RechargeActivity.class);
                intent.putExtra("cookie", getIntent().getStringExtra("cookie"));
                intent.putExtra("id", getIntent().getStringExtra("id"));
                startActivityForResult(intent, 0);
            }
        });

        LinearLayout btnCard = (LinearLayout)findViewById(R.id.btn_card);
        btnCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IndexActivity.this, CardActivity.class);
                intent.putExtra("cookie", getIntent().getStringExtra("cookie"));
                intent.putExtra("id", getIntent().getStringExtra("id"));
                startActivity(intent);
            }
        });

        LinearLayout btnRecord = (LinearLayout)findViewById(R.id.btn_record);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IndexActivity.this, RecordActivity.class);
                intent.putExtra("cookie", getIntent().getStringExtra("cookie"));
                intent.putExtra("id", getIntent().getStringExtra("id"));
                startActivity(intent);
            }
        });

        LinearLayout btnInfo = (LinearLayout)findViewById(R.id.btn_info);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IndexActivity.this, InfoActivity.class);
                intent.putExtra("cookie", getIntent().getStringExtra("cookie"));
                intent.putExtra("id", getIntent().getStringExtra("id"));
                startActivity(intent);
            }
        });
    }



    public void writeCookie() {
        try {
            JSONObject json = new JSONObject();
            json.put("username", getIntent().getStringExtra("username"));
            json.put("password", getIntent().getStringExtra("password"));
            FileOutputStream fout = openFileOutput("account", MODE_PRIVATE);
            fout.write(json.toString().getBytes());
            fout.close();
        } catch(Exception e) {
            Log.i("SAVE: ", e.getMessage().toString());
            Toast.makeText(IndexActivity.this, "保存用户信息失败，请检查权限", Toast.LENGTH_SHORT).show();
        }
    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String json = data.getString("getBalanceRes");
            try {
                getBalanceRes = new JSONObject(json);
            } catch (JSONException e) {
                Toast.makeText(IndexActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                return;
            }
            if (getBalanceRes == JSONObject.NULL) {
                Toast.makeText(IndexActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                Log.i("LOGIN: ", "Null Json Object");
            } else {
                int code = 0;
                try {
                    code = getBalanceRes.getInt("code");
                } catch (JSONException e) {
                    Log.i("SAVE: ", e.getMessage().toString());
                }

                if (code == 200) {
                    TextView text = (TextView)findViewById(R.id.text_index_balance);
                    try {
                        double n = Double.parseDouble(getBalanceRes.getString("money"));
                        BigDecimal b = new BigDecimal(n);
                        double f = b.setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
                        text.setText(String.valueOf(f));
                    } catch (JSONException e) {
                        Toast.makeText(IndexActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                        return;
                    }

                } else {
                    try {
                        Toast.makeText(IndexActivity.this, getBalanceRes.getString("info"), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(IndexActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                        Log.i("SAVE: ", e.getMessage().toString());
                    }
                }
            }
            mPullToRefreshLayout.setRefreshComplete();
        }
    };

    Runnable getBalance = new Runnable() {
        @Override
        public void run() {
            String cookie = getIntent().getStringExtra("cookie");
            String id = getIntent().getStringExtra("id");
            Map<String,String> params = new HashMap<String, String>();
            params.put("cookie", cookie);
            params.put("id", id);
            String strUrlPath = "http://121.40.194.163/microPay/index.php?s=/Home/User/getMoney.html";
            String strResult= HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("getBalanceRes", strResult);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        new Thread(getBalance).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(getBalance).start();
    }
}
