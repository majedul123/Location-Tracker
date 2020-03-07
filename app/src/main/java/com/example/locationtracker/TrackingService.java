package com.example.locationtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.LauncherActivity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class TrackingService extends Service {

    public static final String PREFS_NAME = "LoginPrefs";

    private static final String TAG = TrackingService.class.getSimpleName();


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("GPSLocationUpdates"));


        buildNotification();
        requestLocationUpdates();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String srv = intent.getStringExtra("switch");

        Log.i(TAG, "Received Stop Foreground Intent");
//        if (srv.equals("on")) {
//            Log.i(TAG, "Received Stop Foreground Intent");
//            //your end servce code
//            stopForeground(true);
//            stopSelf();
//        }


        return START_STICKY;
    }

//Create the persistent notification//

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void buildNotification() {
        String stop = "stop";
        //registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);
        // Create the persistent notification
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("App running and get your location")
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.smaill_icon);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(14454, getNotification());

        //  startForeground(1, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {


        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        String title = "Sticker Driver get your location";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.smaill_icon)
                .setContentTitle(title)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getNotification() {

        String idChannel = "my_channel_01";
        Intent mainIntent;

        mainIntent = new Intent(getApplicationContext(), LauncherActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, mainIntent, 0);

        NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel mChannel = null;
        // The id of the channel.

        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), null);
        builder.setContentTitle(getResources().getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setContentText("App is running in background");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(idChannel, getApplicationContext().getString(R.string.app_name), importance);
            // Configure the notification channel.
            mChannel.setDescription("App is running in background");
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            builder.setContentTitle("Sticker Driver")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                    .setVibrate(new long[]{100, 250})
                    .setLights(Color.YELLOW, 500, 5000)
                    .setAutoCancel(true);
        }
        return builder.build();
    }


    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            LocalBroadcastManager.getInstance(TrackingService.this).unregisterReceiver(mMessageReceiver);

            stopForeground(true);


            stopSelf();
        }
    };


    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();

        request.setInterval(2000);
        request.setFastestInterval(3000);

        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);


        if (permission == PackageManager.PERMISSION_GRANTED) {


            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        sendMessageToActivity(location, "found");
                    }
                }
            }, null);
        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                stopReceiver, new IntentFilter("GPSLocationUpdates"));

        super.onDestroy();
        stopForeground(true);

        Toast.makeText(getApplicationContext(), " distroy", Toast.LENGTH_SHORT).show();


        stopSelf();

    }


    public void stopTracking() {

        Toast.makeText(getApplicationContext(), "stop tracking call", Toast.LENGTH_SHORT).show();
//         stopForeground(true);
        this.onDestroy();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                stopReceiver, new IntentFilter("GPSLocationUpdates"));

    }

    private void sendMessageToActivity(Location l, String msg) {
        Intent intent = new Intent("GPSLocationUpdates");
        // You can also include some extra data.
        intent.putExtra("Status", msg);
        Bundle b = new Bundle();
        b.putParcelable("Location", l);
        intent.putExtra("Location", b);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    public final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("Status");
            Bundle b = intent.getBundleExtra("Location");
            Location lastKnownLoc = (Location) b.getParcelable("Location");
            Toast.makeText(getApplicationContext(), "service class", Toast.LENGTH_SHORT).show();
            if (lastKnownLoc != null) {

                Log.i("Location : ", lastKnownLoc.getLatitude() + " latitute " + lastKnownLoc.getLongitude() + " Longitute " + lastKnownLoc.getTime());

                location location1 = new location(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());

                /*

                 /// work for send letitude and longitude in server

                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
                String access_token = "Bearer " + prefs.getString("access_token", null);
                int driver_id = prefs.getInt("Id", 0);

                BaseApiService mApiService;
                mApiService = UtilsApi.getAPIService();
                ping ping = new ping(driver_id, location1);
                mApiService.update(access_token, ping).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {

                            Log.d("LocationUpdateService", "=============> successful");
                        } else {

                            Toast.makeText(getApplicationContext(), " failure " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.d("debug", "onFailure: ERROR > " + t.toString());
                    }
                });

                // work for send latitude and longitude in server
                */

                Toast.makeText(getApplicationContext(), "latitute : " + lastKnownLoc.getLatitude() + " longitute :" + lastKnownLoc.getLongitude(), Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), lastKnownLoc.getLatitude() + " latitute " + lastKnownLoc.getLongitude() + " Longitute " + lastKnownLoc.getTime(), Toast.LENGTH_SHORT).show();

            }

        }
    };

}
