package tudelft.beaconscanner;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import tudelft.beaconscanner.enums.SettingsConstants;


public class BeaconNotification {

    private static final String TAG = "BeaconNotification";
    private NotificationCompat.Builder builder;
    private Context context;

    private ArrayList<BeaconObject> mBeaconList = new ArrayList<>();

    public BeaconNotification(Context context) {
        final Resources res = context.getResources();

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        this.context = context;
        this.builder = new NotificationCompat.Builder(context);
        this.builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Beacon Scanner")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setTicker("Scan Results")
                .setNumber(7)
                .addAction(R.drawable.common_full_open_on_phone, "Open app", intent);
    }

    private void updateBeaconList(BeaconObject beaconObject){
        String beaconId = beaconObject.getId();
        Boolean found  = false;
        ArrayList<BeaconObject> tempList = new ArrayList<>();
        for (BeaconObject bs : mBeaconList){
            if (bs.getId().equals(beaconId)){
                tempList.add(beaconObject);
                found = true;
            } else {
                tempList.add(bs);
            }
        }
        if (!found){
            tempList.add(beaconObject);
        }
        mBeaconList = new ArrayList<>(tempList);
        sortList();
    }


    private void sortList(){
        String sort_preference = PreferenceManager
                .getDefaultSharedPreferences(this.context).getString(
                        SettingsConstants.SORT_PREFERENCE, SettingsConstants.SORT_RSSI);
        if (sort_preference.equals(SettingsConstants.SORT_ALPHABETICALLY)) {
            sortByName();
        } else if (sort_preference.equals(SettingsConstants.SORT_RSSI)){
            sortByRSSI();
        }
    }

    private void sortByRSSI(){
        Collections.sort(mBeaconList, new Comparator<BeaconObject>() {
            @Override
            public int compare(BeaconObject item1, BeaconObject item2) {

                return item2.getRSSI().compareTo(item1.getRSSI());
            }
        });
    }

    private void sortByName(){
        Collections.sort(mBeaconList, new Comparator<BeaconObject>() {
            @Override
            public int compare(BeaconObject item1, BeaconObject item2) {

                return item1.getName().compareTo(item2.getName());
            }
        });
    }

    private void removeOldBeacons(int secondsSinceLastSeen){

        SimpleDateFormat dt = new SimpleDateFormat("hh:mm:ss");
        Date date = new Date();
        long msNow = 0;

        try {
            Date dateNow = dt.parse(dt.format(date));
            msNow = dateNow.getTime();
        } catch (Exception e){
            Log.e(TAG, e.toString());
        }

        ArrayList<BeaconObject> tempList = new ArrayList<>();
        for (BeaconObject bs : mBeaconList){
            try {
                Date dateBeacon = dt.parse(bs.getTimestamp());
                long msBeacon = dateBeacon.getTime();
                long msDiff = msNow - msBeacon;
                if (msDiff < secondsSinceLastSeen * 1000){
                    tempList.add(bs);
                }
            } catch (Exception e){
                Log.e(TAG, e.toString());
            }
        }
        mBeaconList = new ArrayList<>(tempList);

    }
    private int countBeacons(int secondsSinceLastSeen){
        int n = 0;
        SimpleDateFormat dt = new SimpleDateFormat("hh:mm:ss");
        Date date = new Date();
        long msNow = 0;

        try {
            Date dateNow = dt.parse(dt.format(date));
            msNow = dateNow.getTime();
        } catch (Exception e){
            Log.e(TAG, e.toString());
        }

        for (BeaconObject bObj : mBeaconList){
            try {
                Date dateBeacon = dt.parse(bObj.getTimestamp());
                long msBeacon = dateBeacon.getTime();
                long msDiff = msNow - msBeacon;
                Log.i(TAG, "Diff: " + String.valueOf(msNow) + "-" + String.valueOf(msBeacon) + "=" + String.valueOf(msDiff));
                if (msDiff < secondsSinceLastSeen * 1000){
                    n++;
                }
            } catch (Exception e){
                Log.e(TAG, e.toString());
            }
        }
        return n;
    }

    public void update(BeaconObject beaconObject) {
        updateBeaconList(beaconObject);
        removeOldBeacons(SettingsConstants.SECONDS_SINCE_LAST_SEEN);

        NotificationCompat.InboxStyle mNotificationCompat = new NotificationCompat.InboxStyle();
        SpannableStringBuilder beaconsSeen = new SpannableStringBuilder();
        beaconsSeen.append("Beacons seen: ");
        beaconsSeen.setSpan(new ForegroundColorSpan(Color.BLACK), 0, beaconsSeen.length(), 0);
        beaconsSeen.append(String.valueOf(mBeaconList.size()));
        mNotificationCompat.addLine(beaconsSeen);
        mNotificationCompat.addLine("");

        int top = 3;
        int min = (mBeaconList.size() < top) ? mBeaconList.size() : top;

        for (int i = 0; i < min; i++){
            BeaconObject bObj = mBeaconList.get(i);
            SpannableStringBuilder exampleItem = new SpannableStringBuilder();
            exampleItem.append(String.valueOf(i + 1) + ". " + bObj.getName());
            exampleItem.setSpan(new ForegroundColorSpan(Color.BLACK), 0, exampleItem.length(), 0);
            exampleItem.append("    " + bObj.getRSSI().toString());
            mNotificationCompat.addLine(exampleItem);
        }

        builder.setStyle(mNotificationCompat);
        notify(context, builder.build());
    }

    public void cancel(){
        cancel(this.context);
    }

    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(TAG, 0, notification);
        } else {
            nm.notify(TAG.hashCode(), notification);
        }
    }

    private static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(TAG, 0);
        } else {
            nm.cancel(TAG.hashCode());
        }
    }
}