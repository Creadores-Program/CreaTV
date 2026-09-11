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
                                    if (isActivityDestroyed()) return;
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
                        showErrorDialog(getString(R.string.noLink), getString(R.string.nohayLink));
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
                        showErrorDialog(getString(R.string.noLink), getString(R.string.noCalidad));
                        return;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isActivityDestroyed()) return;
                            new AlertDialog.Builder(StreamActivity.this, android.R.style.Theme_Holo_Light_Dialog)
                                .setTitle(R.string.calidad)
                                .setItems(displayList.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String selectedKey = keysList.get(which).toString();
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
                    showErrorDialog(getString(R.string.errorRed), e.getMessage());
                } catch(Exception e){
                    e.printStackTrace();
                    Log.e(getString(R.string.app_name), getString(R.string.errorVideoRep), e);
                    showErrorDialog("Error!", getString(R.string.errorDesc));
                }
            }
        });
        networkThread.start();
    }

    private void launchPlayerAndChat(final Uri uriUrlTarget, final String linkVideo) {
        if (linkVideo == null || linkVideo.isEmpty()) {
            showErrorDialog(getString(R.string.noLink), getString(R.string.linkInvalid));
            return;
        }

        Runnable launchAction = new Runnable() {
            @Override
            public void run() {
                if (isActivityDestroyed()) return;
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
                if (isActivityDestroyed()) return;
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
    private boolean isActivityDestroyed() {
        if (isFinishing()) return true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return isDestroyed();
        }
        return false;
    }
}
