package com.delostik.ichange.Content;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.delostik.ichange.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExchangeChangeCardActivity extends ListActivity {

    private ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changecard);

        JSONObject json = new JSONObject();
        JSONArray card = new JSONArray();

        listview = getListView();

        try {
            json = new JSONObject(getIntent().getStringExtra("card"));
            card = json.getJSONArray("res");
        } catch (JSONException e) {
            Log.i("returnCardRes", e.getMessage().toString());
            Toast.makeText(ExchangeChangeCardActivity.this, "获取银行卡信息失败", Toast.LENGTH_SHORT).show();
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
                map.put("itemid", String.valueOf(i));
                list.add(map);
            } catch (JSONException e) {}
        }

        SimpleAdapter adapter = new SimpleAdapter(this,list,R.layout.vlist,
                new String[]{"title","info","img", "itemid"},
                new int[]{R.id.title,R.id.info,R.id.img,R.id.itemid});
        setListAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ExchangeChangeCardActivity.this, RechargeActivity.class);
                setResult(RESULT_OK, intent);
                TextView itemid = (TextView)view.findViewById(R.id.itemid);
                intent.putExtra("choose", itemid.getText().toString());
                finish();
            }
        });
    }


}
