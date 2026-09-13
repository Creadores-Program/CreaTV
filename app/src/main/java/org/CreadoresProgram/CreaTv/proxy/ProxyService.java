package org.CreadoresProgram.CreaTv.proxy;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import org.CreadoresProgram.CreaTv.R;

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

        Notification notification = new Notification(icon, tickerText, when);

        Intent stopIntent = new Intent(this, ProxyService.class);
        stopIntent.setAction(ACTION_STOP);
        
        PendingIntent pendingIntent = PendingIntent.getService(
                this, 
                0, 
                stopIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        notification.setLatestEventInfo(
                this, 
                getString(R.string.servicioCreaTv), 
                getString(R.string.servicioApagar), 
                pendingIntent
        );

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
