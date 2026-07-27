package org.CreadoresProgram.CreaTv;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.net.Uri;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import java.util.concurrent.Executors;

import org.CreadoresProgram.CreaTv.utils.Util;

public class StreamActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
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
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(linkVideo), "video/*");
                            startActivity(intent);
                            finish();
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
    }
}
