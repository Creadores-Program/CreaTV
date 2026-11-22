package org.CreadoresProgram.CreaTv;

import android.app.Application;
import android.os.Build;
import org.conscrypt.Conscrypt;
import org.streamlink.android.Streamlink;
import org.CreadoresProgram.CreaTv.utils.Util;
import java.security.Security;

public class CreaTvApp extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Security.insertProviderAt(Conscrypt.newProvider(), 1);
        }
        Streamlink.setHttpClient(Util.clientHtStream);
    }
}
