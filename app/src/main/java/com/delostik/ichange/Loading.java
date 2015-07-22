package com.delostik.ichange;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

public class Loading {
    static private  AlertDialog longinDialog;
    static private View longinDialogView;
    static public void show(Context context) {
        Context mContext = context;

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        longinDialogView = layoutInflater.inflate(R.layout.loading, null);

        WebView runWebView = (WebView)longinDialogView.findViewById(R.id.runWebView);
        runWebView.loadDataWithBaseURL(null,"<HTML><body bgcolor='#000000'style='margin:0'><div align=center><IMG src='file:///android_asset/loading.gif'width='100%' height='100%'/></div></body></html>", "text/html", "UTF-8",null);

        longinDialog = new AlertDialog.Builder(mContext).setView(longinDialogView).create();
        longinDialog.show();
    }

    static public void cancel(){
        longinDialog.cancel();
    }

    @Override
    protected void finalize() {
        longinDialog.cancel();
    }
}
