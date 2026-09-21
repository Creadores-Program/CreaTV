package org.CreadoresProgram.CreaTv;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.net.Uri;
import android.content.Intent;
import android.content.DialogInterface;
import android.util.Log;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CheckBox;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;

import org.CreadoresProgram.CreaTv.utils.Util;
import org.CreadoresProgram.CreaTv.proxy.*;

public class StreamActivity extends Activity {

    private ContextThemeWrapper themeWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_loading);
        
        String urlTarget = (getIntent().getData() != null) ? getIntent().getData().toString() : getIntent().getStringExtra(Util.STREAMURL);
        if(urlTarget == null){
            finish();
            return;
        }

        this.themeWrapper = new ContextThemeWrapper(this, R.style.AppDialogTheme);
        
        final Uri uriUrlTarget = Uri.parse(urlTarget);

        final boolean isTwitch = (uriUrlTarget.getHost() != null) 
        && uriUrlTarget.getHost().toLowerCase().contains("twitch");
        
        Thread networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    if(uriUrlTarget.getHost() != null){
                        String hostYt = uriUrlTarget.getHost().toLowerCase();
                        if(hostYt.contains("youtube") || hostYt.contains("you.be")){
                            final String videoId = Util.getYtId(urlTarget);
                            if (videoId == null || TextUtils.isEmpty(videoId.trim())) {
                                showErrorDialog(getString(R.string.noLink), getString(R.string.noCalidad));
                                return;
                            }
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
                            LinearLayout headerLayout = new LinearLayout(themeWrapper);
                            headerLayout.setOrientation(LinearLayout.VERTICAL);
                            int padding = (int) (14 * getResources().getDisplayMetrics().density);
                            headerLayout.setPadding(padding, padding, padding, 0);
                            TextView titleView = new TextView(themeWrapper, null, android.R.attr.windowTitleStyle);
                            titleView.setText(R.string.calidad);
                            titleView.setTextSize(20);
                            titleView.setTypeface(null, android.graphics.Typeface.BOLD);
                            headerLayout.addView(titleView);
                            final CheckBox cbChat;
                            if (isTwitch) {
                                cbChat = new CheckBox(themeWrapper);
                                cbChat.setText(R.string.abrirChatTwitch);
                                cbChat.setChecked(true);
                                headerLayout.addView(cbChat);
                            } else {
                                cbChat = null;
                            }
                            new AlertDialog.Builder(themeWrapper)
                                .setCustomTitle(headerLayout)
                                .setItems(displayList.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String selectedKey = keysList.get(which);
                                        String linkVideo = data.optString(selectedKey);
                                        boolean openChat = (cbChat != null && cbChat.isChecked());
                                        launchPlayerAndChat(uriUrlTarget, linkVideo, openChat);
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int whitch){
                                        finish();
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

    private void launchPlayerAndChat(final Uri uriUrlTarget, final String linkVideo, final boolean openChat) {
        if (linkVideo == null || TextUtils.isEmpty(linkVideo)) {
            showErrorDialog(getString(R.string.noLink), getString(R.string.linkInvalid));
            return;
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2 && !ProxyService.isRunning(this)) {
            Intent intent = new Intent(this, ProxyService.class);
            startService(intent);
        }

        Runnable launchAction = new Runnable() {
            @Override
            public void run() {
                if (isActivityDestroyed()) return;
                if (openChat) {
                    String creator = Util.getCreatorName(uriUrlTarget);
                    if (creator != null) {
                        Intent cintent = new Intent(StreamActivity.this, MainActivity.class);
                        cintent.putExtra(Util.CREATORNAME, creator);
                        cintent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(cintent);
                    }
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse((Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2) ? ProxyServer.buildStreamUrl(linkVideo) : linkVideo), "video/*");
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
                new AlertDialog.Builder(themeWrapper)
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
            case "ao":
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
