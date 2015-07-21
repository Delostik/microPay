package com.delostik.ichange.Content;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;

import com.delostik.ichange.HttpUtils;
import com.delostik.ichange.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CardActivity extends ListActivity {
    private JSONObject cardInfo = new JSONObject();
    private ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        
        new Thread(getAllCards).start();

        listview = getListView();
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CardActivity.this, BalanceActivity.class);
                TextView number = (TextView)view.findViewById(R.id.title);
                intent.putExtra("cookie", getIntent().getStringExtra("cookie"));
                intent.putExtra("id", getIntent().getStringExtra("id"));
                intent.putExtra("number", number.getText().toString());
                startActivity(intent);
            }
        });

        Button btn_addCard = (Button)findViewById(R.id.btn_addCard);
        btn_addCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CardActivity.this, AddCardActivity.class);
                intent.putExtra("cookie", getIntent().getStringExtra("cookie"));
                intent.putExtra("id", getIntent().getStringExtra("id"));
                startActivity(intent);
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
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("getCardRes", strResult);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    private void returnCardRes() {
        JSONArray card = new JSONArray();
        try {
            card = cardInfo.getJSONArray("res");
        } catch (JSONException e) {
            Log.i("returnCardRes", e.getMessage().toString());
            Toast.makeText(CardActivity.this, "获取银行卡信息失败", Toast.LENGTH_SHORT);
            return;
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < card.length(); ++i) {
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONObject item = (JSONObject) card.get(i);
                map.put("title", item.get("number"));
                map.put("info", "浙大银行");
                map.put("img", R.drawable.card);
                list.add(map);
            } catch (JSONException e) {}
        }

        SimpleAdapter adapter = new SimpleAdapter(this,list,R.layout.vlist,
                new String[]{"title","info","img"},
                new int[]{R.id.title,R.id.info,R.id.img});
        setListAdapter(adapter);

    }

}