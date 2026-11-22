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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.CreadoresProgram.CreaTv.utils.Util;
import org.CreadoresProgram.CreaTv.proxy.*;

import org.streamlink.android.Streamlink;
import org.streamlink.android.stream.MuxedStream;
import org.streamlink.android.stream.Stream;

public class StreamActivity extends Activity {

    private ContextThemeWrapper themeWrapper;

    private static class StreamSelection {
        final String videoUrl;
        final String audioUrl;
        final boolean isMuxed;

        StreamSelection(String videoUrl, String audioUrl, boolean isMuxed) {
            this.videoUrl = videoUrl;
            this.audioUrl = audioUrl;
            this.isMuxed = isMuxed;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_loading);

        String urlTarget = (getIntent().getData() != null)
                ? getIntent().getData().toString()
                : getIntent().getStringExtra(Util.STREAMURL);
        if (urlTarget == null) {
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
                try {
                    /*if (uriUrlTarget.getHost() != null) {
                        String hostYt = uriUrlTarget.getHost().toLowerCase();
                        if (hostYt.contains("youtube") || hostYt.contains("you.be")) {
                            final String videoId = Util.getYtId(urlTarget);
                            if (videoId == null || TextUtils.isEmpty(videoId.trim())) {
                                showErrorDialog(getString(R.string.noLink), getString(R.string.noCalidad));
                                return;
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
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
                    }*/

                    Map<String, Stream> streams = Streamlink.streams(urlTarget);
                    if (streams == null || streams.isEmpty()) {
                        showErrorDialog(getString(R.string.noLink), getString(R.string.nohayLink));
                        return;
                    }

                    final Map<String, StreamSelection> validSelections = new LinkedHashMap<String, StreamSelection>();
                    for (Map.Entry<String, Stream> entry : streams.entrySet()) {
                        String key = entry.getKey();
                        Stream stream = entry.getValue();
                        if (key != null && stream != null) {
                            StreamSelection selection = buildSelection(stream);
                            if (selection != null && !TextUtils.isEmpty(selection.videoUrl)) {
                                validSelections.put(key, selection);
                            }
                        }
                    }

                    if (validSelections.isEmpty()) {
                        showErrorDialog(getString(R.string.noLink), getString(R.string.noCalidad));
                        return;
                    }

                    final List<String> keysList = new ArrayList<String>(validSelections.keySet());
                    final List<String> displayList = new ArrayList<String>();
                    for (String key : keysList) {
                        displayList.add(getDisplayKey(key));
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
                                        StreamSelection selection = validSelections.get(selectedKey);
                                        boolean openChat = (cbChat != null && cbChat.isChecked());
                                        launchPlayerAndChat(uriUrlTarget, selection, openChat);
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int whitch) {
                                        finish();
                                    }
                                })
                                .setCancelable(false)
                                .create()
                                .show();
                        }
                    });

                } catch (final Exception e) {
                    showErrorDialog(getString(R.string.errorRed), e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(getString(R.string.app_name), getString(R.string.errorVideoRep), e);
                    showErrorDialog("Error!", getString(R.string.errorDesc));
                }
            }
        });
        networkThread.start();
    }

    private StreamSelection buildSelection(Stream stream) {
        if (stream instanceof MuxedStream) {
            MuxedStream muxed = (MuxedStream) stream;
            Stream videoStream = muxed.getVideoStream();
            Stream audioStream = muxed.getAudioStream();
            String videoUrl = (videoStream != null) ? videoStream.getUrl() : null;
            String audioUrl = (audioStream != null) ? audioStream.getUrl() : null;
            return new StreamSelection(videoUrl, audioUrl, true);
        }
        return new StreamSelection(stream.getUrl(), null, false);
    }

    private void launchPlayerAndChat(final Uri uriUrlTarget, final StreamSelection selection, final boolean openChat) {
        final String linkVideo = (selection != null) ? selection.videoUrl : null;
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
                String targetUrl = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2)
                        ? ProxyServer.buildStreamUrl(linkVideo)
                        : linkVideo;
                intent.setDataAndType(Uri.parse(targetUrl), "video/*");
                if (selection.isMuxed && !TextUtils.isEmpty(selection.audioUrl)) {
                    intent.putExtra("audio_url", selection.audioUrl);
                }
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
            case "worst":
            case "link_worst":
                return getString(R.string.bajaCalidad);
            case "best":
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