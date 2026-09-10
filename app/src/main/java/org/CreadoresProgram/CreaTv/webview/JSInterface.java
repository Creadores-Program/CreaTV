package org.CreadoresProgram.CreaTv.webview;

import org.CreadoresProgram.CreaTv.utils.Util;

import android.app.Activity;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import android.net.Uri;

public class JSInterface{
    private Activity context;
    public JSInterface(Activity context){
        this.context = context;
    }
    @JavascriptInterface
    public void openVideo(String url, boolean openChat){
        Util.openVideo(url, openChat, context);
    }
    @JavascriptInterface
    public void openUrl(String url){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
    @JavascriptInterface
    public String getLang(String key){
        int resId = context.getResources().getIdentifier(stringKey, "string", context.getPackageName());
        if (resId != 0) {
            return context.getString(resId);
        }
        return "";
    }
}
