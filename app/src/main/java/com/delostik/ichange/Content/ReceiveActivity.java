package com.delostik.ichange.Content;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.delostik.ichange.R;

import java.nio.charset.Charset;
import java.util.Locale;


public class ReceiveActivity extends Activity implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(ReceiveActivity.this, "找不到NFC设备", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(ReceiveActivity.this, "请在设置中启用NFC设备", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);

}

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        Toast.makeText(ReceiveActivity.this, "已发送收款请求", Toast.LENGTH_SHORT);
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = getIntent().getStringExtra("username");
        NdefMessage msg = new NdefMessage(new NdefRecord[]{ createTextRecord(text) });
            return msg;
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

        public NdefRecord createTextRecord(String text) {
        byte[] langBytes = Locale.ENGLISH.getLanguage().getBytes(Charset.forName("UTF-8"));
            Charset utfEncoding = Charset.forName("UTF-8");
            byte[] textBytes = text.getBytes(utfEncoding);
            int utfBit = 0;
            char status = (char) (utfBit + langBytes.length);
            byte[] data = new byte[1 + langBytes.length + textBytes.length];
            data[0] = (byte) status;
            System.arraycopy(langBytes, 0, data, 1, langBytes.length);
            System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
            NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
            return record;
        }
}

