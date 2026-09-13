package org.CreadoresProgram.CreaTv.proxy;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import org.CreadoresProgram.CreaTv.R;

import java.lang.reflect.Method;

public class ProxyService extends Service {

    public static final String ACTION_STOP = "org.CreadoresProgram.CreaTv.proxy.ACTION_STOP";
    private static final int NOTIFICATION_ID = 9999;
    private static volatile boolean isServiceRunning = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!isServiceRunning && !ProxyServer.isRunning()) {
            isServiceRunning = true;
            ProxyServer.start();
            showNotification();
        }

        return START_STICKY;
    }

    @SuppressWarnings("deprecation")
    private void showNotification() {
        int icon = android.R.drawable.ic_menu_info_details; 
        CharSequence tickerText = getString(R.string.servicioActivo);
        long when = System.currentTimeMillis();
        CharSequence title = getString(R.string.servicioCreaTv);
        CharSequence text = getString(R.string.servicioApagar);

        Intent stopIntent = new Intent(this, ProxyService.class);
        stopIntent.setAction(ACTION_STOP);
        
        PendingIntent pendingIntent = PendingIntent.getService(
                this, 
                0, 
                stopIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(icon)
                    .setTicker(tickerText)
                    .setWhen(when)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(pendingIntent);

            notification = builder.getNotification();
        } else {
            notification = new Notification(icon, tickerText, when);
            try {
                Method setLatestEventInfo = Notification.class.getMethod(
                        "setLatestEventInfo",
                        Context.class, 
                        CharSequence.class, 
                        CharSequence.class, 
                        PendingIntent.class
                );
                setLatestEventInfo.invoke(notification, this, title, text, pendingIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        ProxyServer.stop();
        stopForeground(true);
    }

    public static boolean isRunning(Context context) {
        return isServiceRunning || ProxyServer.isRunning();
    }
}
