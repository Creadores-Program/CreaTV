package org.CreadoresProgram.CreaTv;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.net.Uri;
import android.content.Intent;
import android.content.DialogInterface;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;

import org.CreadoresProgram.CreaTv.utils.Util;

public class StreamActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_loading);
        
        String urlTarget = (getIntent().getData() != null) ? getIntent().getData().toString() : getIntent().getStringExtra(Util.STREAMURL);
        if(urlTarget == null){
            finish();
            return;
        }
        
        final Uri uriUrlTarget = Uri.parse(urlTarget);
        
        Thread networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    if(uriUrlTarget.getHost() != null){
                        String hostYt = uriUrlTarget.getHost().toLowerCase();
                        if(hostYt.contains("youtube") || hostYt.contains("you.be")){
                            final String videoId = Util.getYtId(urlTarget);
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run(){
                                    Intent ytPintent = new Intent(StreamActivity.this, YTPlayerActivity.class);
                                    ytPintent.putExtra(Util.YTID, videoId);
                                    ytPintent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(ytPintent);
                                    finish();
                                }
                            });
                            return;
                        }
                    }
                    
                    final JSONObject data = Util.getVideoLink(urlTarget);
                    if (data == null || data.length() == 0) {
                        showErrorDialog("No Link!", "No link was provided!");
                        return;
                    }

                    final List<String> keysList = new ArrayList<String>();
                    final List<String> displayList = new ArrayList<String>();
                    Iterator<String> keys = data.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        keysList.add(key);
                        displayList.add(getDisplayKey(key));
                    }

                    if (keysList.isEmpty()) {
                        showErrorDialog("No Link!", "No video qualities available!");
                        return;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final CharSequence[] options = displayList.toArray(new CharSequence[0]);
                            
                            new AlertDialog.Builder(StreamActivity.this, android.R.style.Theme_Holo_Light_Dialog)
                                .setTitle(R.string.calidad)
                                .setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String selectedKey = options[which].toString();
                                        String linkVideo = data.optString(selectedKey);
                                        
                                        launchPlayerAndChat(uriUrlTarget, linkVideo);
                                    }
                                })
                                .setCancelable(false)
                                .create()
                                .show();
                        }
                    });

                } catch (final IOException e) {
                    showErrorDialog("Error Network!", e.getMessage());
                } catch(Exception e){
                    e.printStackTrace();
                    Log.e("CreaTv", "Error play Video", e);
                    showErrorDialog("Error!", "An unknown error occurred. Sometimes just try 3 more times! Or perhaps your content creator isn't online!");
                }
            }
        });
        networkThread.start();
    }

    private void launchPlayerAndChat(final Uri uriUrlTarget, final String linkVideo) {
        if (linkVideo == null || linkVideo.isEmpty()) {
            showErrorDialog("No Link!", "Selected video link is invalid!");
            return;
        }

        Runnable launchAction = new Runnable() {
            @Override
            public void run() {
                if (getIntent().getBooleanExtra(Util.ONCHAT, false)) {
                    String creator = Util.getCreatorName(uriUrlTarget);
                    if (creator != null) {
                        Intent cintent = new Intent(StreamActivity.this, ChatActivity.class);
                        cintent.putExtra(Util.CREATORNAME, creator);
                        cintent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(cintent);
                    }
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(linkVideo), "video/*");
                startActivity(intent);
                finish();
            }
        };

        if (Thread.currentThread().equals(getMainLooper().getThread())) {
            launchAction.run();
        } else {
            runOnUiThread(launchAction);
        }
    }

    private void showErrorDialog(final String title, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(StreamActivity.this, android.R.style.Theme_Holo_Light_Dialog)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
            }
        });
    }
    private String getDisplayKey(String key) {
        switch (key) {
            case "audio_only":
                return getString(R.string.soloaudio);
            case "link_worst":
                return getString(R.string.bajaCalidad);
            case "link_best":
                return getString(R.string.mejorCalidad);
            default:
                return key;
        }
    }
}
