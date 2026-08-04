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
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    public static final String YTID = "org.CreadoresProgram.CreaTv.YTID";
    private static final String userAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Mobile Safari/537.36";
    private static final Pattern CANONICAL_PATTERN = Pattern.compile(
        "<link rel=\"canonical\" href=\"https://www.youtube.com/watch\\?v=([a-zA-Z0-9_-]{11})\">"
    );
    private static final Pattern LIVE_JSON_PATTERN = Pattern.compile(
        "\"videoId\":\"([a-zA-Z0-9_-]{11})\".*?\"style\":\"LIVE\""
    );
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
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){
                return handleUrlLoading(view, request.getUrl().toString());
            }
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                return handleUrlLoading(view, url);
            }
            private boolean handleUrlLoading(WebView view, String url){
                if(url == null || url.startsWith("file:") || url.startsWith("https://creatv.onrender.com")){
                    return false;
                }
                return true;
            }
        });
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
    public static JSONObject getVideoLink(String url) throws Exception {
        JSONObject reqD = new JSONObject();
        reqD.put("direct_url", url);
        RequestBody body = RequestBody.create(JSONHt, reqD.toString());
        Request request = new Request.Builder()
                .url(proxy)
                .post(body)
                .addHeader("User-Agent", userAgent)
                .build(); 
        Response response = null;
        try{
            response = clientHt.newCall(request).execute();
            String responseBodyStr = response.body() != null ? response.body().string() : "";
            if(responseBodyStr.isEmpty()){
                throw new IOException("Unexpected code " + response);
            }
            JSONObject resJson = new JSONObject(responseBodyStr);
            if (!response.isSuccessful()) throw new IOException("Error: " + response.code() + " : " + resJson.optString("detail", "Unknown"));
            return resJson;
        }finally {
            if (response != null) response.close();
        }
    }
    public static String getYtId(String url) throws Exception {
        Request request = new Request.Builder()
            .url(url)
            .addHeader("User-Agent", userAgent)
            .addHeader("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
            .build();
        Response response = null;
        try{
            response = clientHt.newCall(request).execute();
            String responseBodyStr = response.body() != null ? response.body().string() : "";
            if(responseBodyStr.isEmpty()){
                throw new IOException("Unexpected code " + response);
            }
            Matcher matcherCanonical = CANONICAL_PATTERN.matcher(responseBodyStr);
            if (matcherCanonical.find()) {
                return matcherCanonical.group(1);
            }
            Matcher matcherJson = LIVE_JSON_PATTERN.matcher(responseBodyStr);
            if (matcherJson.find()) {
                return matcherJson.group(1);
            }
        }finally{
            if(response != null) response.close();
        }
        return null;
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
