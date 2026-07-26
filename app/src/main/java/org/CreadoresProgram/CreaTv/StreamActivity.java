package org.CreadoresProgram.CreaTv;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.VideoView;
import android.media.MediaPlayer;
import android.widget.MediaController;
import android.net.Uri;

import org.json.JSONObject;

import java.util.concurrent.Executors;

import org.CreadoresProgram.CreaTv.utils.Util;

public class StreamActivity extends Activity {
    private VideoView videoView;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_video);
        this.videoView = (VideoView) findViewById(R.id.videoView);
        this.webView = (WebView) findViewById(R.id.webview);
        Util.configWebView(this.webView, this);
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
                            MediaController mediaController = new MediaController(StreamActivity.this);
                            mediaController.setAnchorView(videoView);
                            videoView.setMediaController(mediaController);
                            videoView.setVideoURI(Uri.parse(linkVideo));
                            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    videoView.start();
                                }
                            });
                            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                @Override
                                public boolean onError(MediaPlayer mp, int what, int extra) {
                                    return true;
                                }
                            });
                            String creator = Util.getCreatorName(getIntent().getData());
                            String urlChat = "";
                            if(creator != null){
                                urlChat = "https://nigthdev.com/hosted/obschat/?channel="+creator+"fade=false";
                            }else{
                                urlChat = "file:///android_asset/chat/defaultChat.html";
                            }
                            webView.loadUrl(urlChat);
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                    //Log.e("CreaTv", "Error al reproducir Video", e);
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
            videoView.pause();
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
            videoView.start();
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
        if (this.videoView != null) {
            this.videoView.stopPlayback();
            this.videoView = null;
        }
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
