package com.delostik.ichange.Content;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import com.delostik.ichange.R;

public class NFCPayActivity extends Activity {

    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcpay);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(NFCPayActivity.this, "找不到NFC设备", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(NFCPayActivity.this, "请在设置中启用NFC设备", Toast.LENGTH_SHORT).show();
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
        String username = TextRecord.parse(msg.getRecords()[0]).getText();
        Intent confirm = new Intent(NFCPayActivity.this, ConfirmPayActivity.class);
        confirm.putExtra("username", username);
        confirm.putExtra("id", getIntent().getStringExtra("id"));
        confirm.putExtra("cookie", getIntent().getStringExtra("cookie"));
        startActivity(confirm);
    }

}