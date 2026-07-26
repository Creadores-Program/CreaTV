package org.CreadoresProgram.CreaTv;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.util.concurrent.Executors;

import org.CreadoresProgram.CreaTv.utils.Util;

public class StreamActivity extends Activity {
    private WebView videoView;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_video);
        this.videoView = (WebView) findViewById(R.id.videoView);
        this.webView = (WebView) findViewById(R.id.webview);
        Util.configWebView(this.webView, this);
        Util.configWebView(this.videoView, this);
        String urlTarget = (getIntent().getData() != null) ? getIntent().getData().toString() : getIntent().getExtras().getString(Util.STREAMURL);
        if(urlTarget == null){
            finish();
            return;
        }
        Thread networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    JSONObject data = Util.getVideoLink(urlTarget);
                    String linkN = (getIntent().hasExtra(Util.QUALITY)) ? getIntent().getExtras().getString(Util.QUALITY) : "link_best";
                    final String linkVideo = data.getString(linkN);
                    if(linkVideo == null){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                        return;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            videoView.loadUrl("file:///android_asset/videoPlayer/videoPlayer.html?link="+linkVideo);
                            String creator = Util.getCreatorName(getIntent().getData());
                            String urlChat = "";
                            if(creator != null){
                                urlChat = "https://nightdev.com/hosted/obschat/?channel="+creator+"&fade=false";
                            }else{
                                urlChat = "file:///android_asset/chat/defaultChat.html";
                            }
                            webView.loadUrl(urlChat);
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                    Log.e("CreaTv", "Error al reproducir Video", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                   return;
                }
            }
        });
        networkThread.start();
        updateScreen(getResources().getConfiguration().orientation);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(this.webView != null){
            webView.onPause();
            webView.pauseTimers();
        }
        if (this.videoView != null && this.videoView.isPlaying()) {
            videoView.onPause();
            videoView.pauseTimers();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(this.webView != null){
            webView.onResume();
            webView.resumeTimers();
        }
        if (this.videoView != null && !this.videoView.isPlaying()) {
            videoView.onResume();
            videoView.resumeTimers();
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
        videoView.post(new Runnable(){
            @Override
            public void run(){
                videoView.destroy();
                videoView = null;
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateScreen(newConfig.orientation);
    }
    private void updateScreen(int orientation){
        View decorView = getWindow().getDecorView();
        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            int flags = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            decorView.setSystemUiVisibility(flags);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else{
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
}
