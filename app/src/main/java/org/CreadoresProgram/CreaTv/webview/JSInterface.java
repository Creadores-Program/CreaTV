package org.CreadoresProgram.CreaTv.webview;

import org.CreadoresProgram.CreaTv.utils.Util;

import android.app.Activity;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import android.net.Uri;

import java.util.Locale;

public class JSInterface{
    private Activity context;
    public JSInterface(Activity context){
        this.context = context;
    }
    @JavascriptInterface
    public void openVideo(String url, boolean qualitylow){
        Util.openVideo(url, qualitylow, context);
    }
    @JavascriptInterface
    public void openUrl(String url){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
    @JavascriptInterface
    public String getLangJson(){
        String lang = Locale.getDefault().getLanguage().toLowerCase();
        if(!lang.equals("es") && !lang.equals("en") && !lang.equals("it") && !lang.equals("pt") && !lang.equals("fr")){
            lang = "es";
        }
        String langJson = Util.readAssetAsString(context.getAssets(), "lang/"+lang+".json");
        if(langJson == null){
            return "{}";
        }
        return langJson;
    }
}
