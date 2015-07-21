package com.delostik.ichange.Content;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.delostik.ichange.HttpUtils;
import com.delostik.ichange.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RecordActivity extends ListActivity {

    JSONObject record = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        new Thread(getRecord).start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String json = data.getString("getRecordRes");
            try {
                record = new JSONObject(json);
            } catch (JSONException e) {
                Log.i("JSONException: ", e.getMessage().toString());
            }
            returnRecordRes();
        }
    };

    private void returnRecordRes() {
        JSONArray piece = new JSONArray();
        try {
            piece = record.getJSONArray("res");
        } catch (JSONException e) {
            Log.i("returnRecordRes", e.getMessage().toString());
            Toast.makeText(RecordActivity.this, "获取交易记录失败", Toast.LENGTH_SHORT);
            return;
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < piece.length(); ++i) {
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONObject item = (JSONObject) piece.get(i);

                String othername = item.get("otherName").toString();
                if (!othername.equals("银行卡") && !othername.equals("自动取款机")) {
                    byte[] des = othername.getBytes("UTF-8");
                    for (int j = 1; j < des.length; j++) {
                        des[j] = '*';
                    }
                    othername = new String(des, "UTF-8");
                }

                Long timestamp = Long.parseLong(item.get("date").toString()) * 1000;
                String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));
                map.put("date", date);
                list.add(map);

                String type = item.get("type").toString();
                Log.i("type", type);
                if (type.equals("2")) {
                    othername = "从 " + othername + " 转入";
                    map.put("money", item.get("money").toString());
                    map.put("type", "收入");
                }
                else {
                    othername = "付款到 " + othername;
                    map.put("money", "-" + item.get("money").toString());
                    map.put("type", "支出");
                }

                map.put("otherName", othername);

            } catch (Exception e) {}
        }



        SimpleAdapter adapter = new SimpleAdapter(this,list,R.layout.vrecord,
                new String[]{"type", "otherName", "money", "date"},
                new int[]{R.id.rec_type,R.id.rec_otherName,R.id.rec_money, R.id.rec_date});
        setListAdapter(adapter);

    }

    Runnable getRecord = new Runnable() {
        @Override
        public void run() {
            String cookie = getIntent().getStringExtra("cookie");
            String id = getIntent().getStringExtra("id");
            Map<String,String> params = new HashMap<String, String>();
            params.put("cookie", cookie);
            params.put("userId", id);
            String strUrlPath = "http://121.40.194.163/microPay/index.php?s=/Home/Account/get_logs.html";
            String strResult= HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("getRecordRes", strResult);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

}
