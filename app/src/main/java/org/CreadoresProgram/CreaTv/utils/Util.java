package org.CreadoresProgram.CreaTv.utils;

import android.app.Activity;
import android.os.Build;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebChromeClient;


import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;

public class Util{
    public static final Charset dataCodeStr;
    static{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            dataCodeStr = KitkatCharset.getUTF8();
        }else{
            dataCodeStr = Charset.forName("UTF-8");
        }
    }
    private static class KitkatCharset{
        static Charset getUTF8(){
            return StandardCharsets.UTF_8;
        }
    }
    private static final OkHttpClient clientHt = new OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .connectionSpecs(Arrays.asList(
            new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
                .supportsTlsExtensions(true)
                .build(),
            new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .supportsTlsExtensions(true)
                .build()
        ))
        .build();
    private static final HttpUrl proxy = HttpUrl.parse("https://creatv.onrender.com/").newBuilder().addPathSegment("stream-link").build();
    private static final MediaType JSONHt = MediaType.parse("application/json; charset=utf-8");
    public static final String QUALITY = "org.CreadoresProgram.CreaTv.QUALITY";
    public static final String STREAMURL = "org.CreadoresProgram.CreaTv.STREAMURL";
    public static final String CREATORNAME = "org.CreadoresProgram.CreaTv.CREATORNAME";
    public static final String ONCHAT = "org.CreadoresProgram.CreaTv.ONCHAT";
    public static String readAssetAsString(AssetManager assetManager, String filePath) {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            inputStream = assetManager.open(filePath);
            outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return new String(outputStream.toByteArray(), dataCodeStr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (outputStream != null) {
                try { outputStream.close(); } catch (IOException ignored) {}
            }
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException ignored) {}
            }
        }
    }
    public static void openVideo(final String url, final boolean qualitylow, final boolean openChat, final Activity context){
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent(context, org.CreadoresProgram.CreaTv.StreamActivity.class);
                    //intent.setData(Uri.parse(url));
                    intent.putExtra(STREAMURL, url);
                    intent.putExtra(QUALITY, qualitylow ? "link_worst" : "link_best");
                    if(openChat){
                        intent.putExtra(ONCHAT, openChat);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    @SuppressWarnings("deprecation")
    public static void configWebView(WebView webView, Activity context){
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2){
            webSettings.setDatabasePath(context.getApplicationContext().getDir("LocalStorageOld", Context.MODE_PRIVATE).getPath());
            webView.setDrawingCacheEnabled(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webView.setBackgroundColor(Color.BLACK);
    }
    public static JSONObject getVideoLink(String url) throws Exception{
        JSONObject reqD = new JSONObject();
        reqD.put("direct_url", url);
        RequestBody body = RequestBody.create(JSONHt, reqD.toString());
        Request request = new Request.Builder()
                .url(proxy)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Mobile Safari/537.36")
                .build(); 
        Response response = null;
        try{
            response = clientHt.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return new JSONObject(response.body().string());
        }finally {
            if (response != null) response.close();
        }
    }
    public static String getCreatorName(Uri uri){
        if (uri == null) return null;

        String host = uri.getHost();
        if (host == null) return null;

        host = host.toLowerCase();
        List<String> pathSegments = uri.getPathSegments();

        if (pathSegments.isEmpty()) return null;

        if (host.contains("twitch")) {
            String channel = pathSegments.get(0);
            return (!channel.equalsIgnoreCase("directory") && !channel.equalsIgnoreCase("videos")) ? channel : null;
        }

        return null;
    }
}
