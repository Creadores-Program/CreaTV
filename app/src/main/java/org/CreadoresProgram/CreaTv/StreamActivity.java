package org.CreadoresProgram.CreaTv;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.net.Uri;
import android.content.Intent;
import android.content.DialogInterface;
import android.util.Log;

import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.io.IOException;

import org.CreadoresProgram.CreaTv.utils.Util;

public class StreamActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_loading);
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
                    String linkN = (getIntent().hasExtra(Util.QUALITY)) ? getIntent().getExtras().getString(Util.QUALITY) : "link_worst";
                    final String linkVideo = data.getString(linkN);
                    if(linkVideo == null){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(StreamActivity.this, android.R.style.Theme_Holo_Light_Dialog)
                                    .setTitle("No Link!")
                                    .setMessage("No link was provided!")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                                    .setCancelable(false)
                                    .create().show();
                            }
                        });
                        return;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(getIntent().hasExtra(Util.ONCHAT)){
                                String creator = Util.getCreatorName(Uri.parse(urlTarget));
                                if(creator != null){
                                    Intent cintent = new Intent(StreamActivity.this, ChatActivity.class);
                                    cintent.putExtra(Util.CREATORNAME, creator);
                                    cintent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    cintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    cintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(cintent);
                                }
                            }
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(linkVideo), "video/*");
                            startActivity(intent);
                            finish();
                        }
                    });
                } catch (final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(StreamActivity.this, android.R.style.Theme_Holo_Light_Dialog)
                                .setTitle("Error Network!")
                                .setMessage(e.getMessage())
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .setCancelable(false)
                                .create().show();
                        }
                    });
                    return;
                }catch(Exception e){
                    e.printStackTrace();
                    Log.e("CreaTv", "Error play Video", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(StreamActivity.this, android.R.style.Theme_Holo_Light_Dialog)
                                .setTitle("Error!")
                                .setMessage("An unknown error occurred. Sometimes just try 3 more times! Or perhaps your content creator isn't online!")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .setCancelable(false)
                                .create().show();
                        }
                    });
                   return;
                }
            }
        });
        networkThread.start();
    }
}
