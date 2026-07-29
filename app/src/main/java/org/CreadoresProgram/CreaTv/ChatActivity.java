package org.CreadoresProgram.CreaTv;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;
import android.net.Uri;
import android.graphics.Color;
import androidx.browser.customtabs.CustomTabsIntent;

import org.CreadoresProgram.CreaTv.utils.Util;

public class ChatActivity extends Activity {
    private WebView webView;
    private boolean isTabBrowser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(!getIntent().hasExtra(Util.CREATORNAME)){
            finish();
            return;
        }
        String url = "https://nightdev.com/hosted/obschat/?channel="+getIntent().getExtras().getString(Util.CREATORNAME)+"&fade=false";
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT){
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setShowTitle(false);
            builder.setToolbarColor(Color.BLACK);
            CustomTabsIntent customTabsIntent = builder.build();
            this.isTabBrowser = true;
            customTabsIntent.launchUrl(this, Uri.parse(url+"&theme=dark"));
            return;
        }
        setContentView(R.layout.layout_main);
        this.webView = (WebView) findViewById(R.id.webview);
        Util.configWebView(this.webView, this);
        webView.loadUrl(url);
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
        if(this.isTabBrowser){
            finish();
        }
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
