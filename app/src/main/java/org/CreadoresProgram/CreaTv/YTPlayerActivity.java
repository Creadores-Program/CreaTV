package org.CreadoresProgram.CreaTv;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import org.CreadoresProgram.CreaTv.utils.Util;

public class YTPlayerActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        String videoId = getIntent().getStringExtra(Util.YTID);
        if(videoId == null || videoId.isEmpty()){
            finish();
            return;
        }
        setContentView(R.layout.layout_main);
        this.webView = (WebView) findViewById(R.id.webview);
        Util.configWebView(this.webView, this);
        //String htmlCode = Util.readAssetAsString(getAssets(), "youtube/StreamYT.html");
        webView.loadUrl("https://botstroll.blogspot.com/2026/08/pois.html?v="+videoId);
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
