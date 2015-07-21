package com.delostik.ichange.Content;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcManager;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.delostik.ichange.R;


public class ChoosePaymentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosepayment);

        Button btn_payment1 = (Button)findViewById(R.id.btn_payment_1);
        btn_payment1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChoosePaymentActivity.this, PayActivity.class);
                intent.putExtra("cookie", getIntent().getStringExtra("cookie").toString());
                intent.putExtra("id", getIntent().getStringExtra("id").toString());
                startActivity(intent);

            }
        });

        Button btn_payment2 = (Button)findViewById(R.id.btn_payment_2);
        btn_payment2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChoosePaymentActivity.this, NFCPayActivity.class);
                intent.putExtra("cookie", getIntent().getStringExtra("cookie").toString());
                intent.putExtra("id", getIntent().getStringExtra("id").toString());
                startActivity(intent);
            }
        });

        Button btn_payment3 = (Button)findViewById(R.id.btn_payment_3);
        btn_payment3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChoosePaymentActivity.this, ATMPayActivity.class);
                intent.putExtra("cookie", getIntent().getStringExtra("cookie").toString());
                intent.putExtra("id", getIntent().getStringExtra("id").toString());
                startActivity(intent);
            }
        });
    }

}
