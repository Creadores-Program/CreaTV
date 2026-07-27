package org.CreadoresProgram.CreaTv.webview;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VideoWebViewClient extends WebViewClient {

    private final OkHttpClient okHttpClient;

    public VideoWebViewClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    // =========================================================================
    // 1. Android 4.1 - 4.4 (API 16 - 19)
    // =========================================================================
    @SuppressWarnings("deprecation")
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (url == null || !url.startsWith("https://")) {
            return super.shouldInterceptRequest(view, url);
        }

        return handleOkHttpRequest(url, "GET", new HashMap<String, String>());
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        String url = request.getUrl() != null ? request.getUrl().toString() : "";

        if (!url.startsWith("https://")) {
            return super.shouldInterceptRequest(view, request);
        }

        String method = request.getMethod();
        Map<String, String> requestHeaders = request.getRequestHeaders();

        return handleOkHttpRequest(url, method, requestHeaders);
    }

    private WebResourceResponse handleOkHttpRequest(String url, String method, Map<String, String> headers) {
        try {
            Request.Builder builder = new Request.Builder().url(url);

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }

            if ("POST".equalsIgnoreCase(method)) {
                return null; 
            } else {
                builder.get();
            }

            Response response = okHttpClient.newCall(builder.build()).execute();

            if (response.body() == null) {
                return null;
            }

            InputStream inputStream = response.body().byteStream();
            String mimeType = getMimeType(response);
            String encoding = response.header("content-encoding", "UTF-8");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int statusCode = response.code();
                String reasonPhrase = response.message().isEmpty() ? "OK" : response.message();
                Map<String, String> responseHeaders = okHeadersToMap(response.headers());

                return new WebResourceResponse(
                        mimeType,
                        encoding,
                        statusCode,
                        reasonPhrase,
                        responseHeaders,
                        inputStream
                );
            } else {
                return new WebResourceResponse(mimeType, encoding, inputStream);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getMimeType(Response response) {
        if (response.body() != null && response.body().contentType() != null) {
            return response.body().contentType().type() + "/" + response.body().contentType().subtype();
        }
        return "text/html";
    }

    private Map<String, String> okHeadersToMap(Headers headers) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            map.put(headers.name(i), headers.value(i));
        }
        return map;
    }
}
