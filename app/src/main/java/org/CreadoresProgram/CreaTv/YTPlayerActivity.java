package org.CreadoresProgram.CreaTv;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;
import android.view.View;
import android.view.Window;

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        applyFull();
        this.webView = (WebView) findViewById(R.id.webview);
        Util.configWebView(this.webView, this);
        webView.loadUrl("https://creatv.onrender.com/youtube-player?v="+videoId);
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
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isFinishing()) {
            applyFull();
        }
    }
    private void applyFull(){
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                |  View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                |  View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                |  View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }
}
