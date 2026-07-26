package org.CreadoresProgram.CreaTv;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import org.CreadoresProgram.CreaTv.utils.Util;
import org.CreadoresProgram.CreaTv.webview.JSInterface;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        this.webView = (WebView) findViewById(R.id.webview);
        Util.configWebView(this.webView, this);
        webView.addJavascriptInterface(new JSInterface(this), "Android");
        webView.loadUrl("file:///android_asset/index.html");
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
        webView.post(new Runnable(){
            @Override
            public void run(){
                webView.destroy();
                webView = null;
            }
        });
        super.onDestroy();
    }
}
