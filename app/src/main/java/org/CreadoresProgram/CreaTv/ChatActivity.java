package org.CreadoresProgram.CreaTv;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;
import android.net.Uri;
import android.graphics.Color;

import org.CreadoresProgram.CreaTv.utils.Util;

public class ChatActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(!getIntent().hasExtra(Util.CREATORNAME)){
            finish();
            return;
        }
        setContentView(R.layout.layout_main);
        this.webView = (WebView) findViewById(R.id.webview);
        Util.configWebView(this.webView, this);
        webView.loadUrl("file:///android_asset/chat/chat.html?channel="+getIntent().getStringExtra(Util.CREATORNAME));
    }
    @Override
    protected void onPause(){
        super.onPause();
        if(this.webView != null){
            webView.onPause();
            webView.pauseTimers();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(this.webView != null){
            webView.onResume();
            webView.resumeTimers();
        }
    }
    @Override
    protected void onDestroy() {
        if(this.webView != null){
            webView.post(new Runnable(){
                @Override
                public void run(){
                    webView.destroy();
                    webView = null;
                }
            });
        }
        super.onDestroy();
    }
}
