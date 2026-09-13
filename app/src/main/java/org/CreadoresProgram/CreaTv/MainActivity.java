package org.CreadoresProgram.CreaTv;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.ComponentCallbacks2;
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
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if(level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE){
            uniLowMem();
        }
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        uniLowMem();
    }

    private void uniLowMem(){
        if (webView != null) {
            webView.clearCache(false);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                webView.freeMemory();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.webView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                webView.onPause();
            } else {
                try {
                    WebView.class.getMethod("onPause").invoke(webView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            webView.pauseTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.webView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { // API 11+
                webView.onResume();
            } else {
                try {
                    WebView.class.getMethod("onResume").invoke(webView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
