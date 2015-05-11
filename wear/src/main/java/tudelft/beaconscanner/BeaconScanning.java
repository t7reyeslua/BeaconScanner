package tudelft.beaconscanner;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import tudelft.beaconscanner.enums.MessageConstants;
import tudelft.beaconscanner.enums.SettingsConstants;

public class BeaconScanning extends Service implements BeaconConsumer {

    private static final String LOGTAG = "BeaconScanningService";
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    private List<Messenger> mClients = new ArrayList<>();
    private static boolean isRunning = false;


    protected static final String TAG_ALTBEACON = "AltBeaconService";
    private org.altbeacon.beacon.BeaconManager beaconManagerAlt;
    private static boolean  scanning = false;
    private Region beaconRegion;

    private BeaconNotification mBeaconNotification;

    public BeaconScanning() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOGTAG, "onBind");
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOGTAG, "Service Started.");
        isRunning = true;
        mBeaconNotification = new BeaconNotification(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAltBeacon();
        Log.i(LOGTAG, "Service Stopped.");
        isRunning = false;
        mBeaconNotification.cancel();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOGTAG, "Received start id " + startId + ": " + intent);

        return START_STICKY; // Run until explicitly stopped.
    }


    public static boolean isRunning()
    {
        return isRunning;
    }

    public static boolean isScanning()
    {
        return scanning;
    }


    private void setUpAltBeacon(){
        if (beaconManagerAlt == null) {
            Log.d(LOGTAG, "setUpAltBeacon");
            beaconManagerAlt = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
            beaconManagerAlt.getBeaconParsers().add(
                    new BeaconParser().setBeaconLayout(SettingsConstants.BEACON_LAYOUT));

            beaconManagerAlt.setForegroundScanPeriod(SettingsConstants.SCAN_DURATION_FOREGROUND);
            beaconManagerAlt.setBackgroundScanPeriod(SettingsConstants.SCAN_DURATION_BACKGROUND);
            beaconManagerAlt.setForegroundBetweenScanPeriod(SettingsConstants.TIME_BETWEEN_SCANS_FOREGROUND);
            beaconManagerAlt.setBackgroundBetweenScanPeriod(SettingsConstants.TIME_BETWEEN_SCANS_BACKGROUND);
        }
        if (!beaconManagerAlt.isBound(this)) {
            Log.d(LOGTAG, "Binding beaconManager");
            beaconManagerAlt.bind(this);
        }
    }

    private void startAltBeacon(){
        Log.d(LOGTAG, "check if setUpAltBeacon ok");
        setUpAltBeacon();
        Log.d(LOGTAG, "startAltBeacon");
        if (!scanning) {
            try {
                Log.d(LOGTAG, "Starting Ranging Beacons in Region");
                beaconManagerAlt.startRangingBeaconsInRegion(beaconRegion);
                scanning = true;
            } catch (RemoteException e) {
                Log.e(LOGTAG, e.toString());
            }
        }
    }


    private void stopAltBeacon(){
        if (beaconManagerAlt != null) {
            Log.d(LOGTAG, "stopAltBeacon");
            try {
                Log.d(LOGTAG, "Stopping Ranging Beacons in Region");
                beaconManagerAlt.stopRangingBeaconsInRegion(beaconRegion);
                scanning = false;
            } catch (RemoteException e) {
                Log.e(LOGTAG, e.toString());
            }
            if (beaconManagerAlt.isBound(this)) {
                Log.d(LOGTAG, "Unbinding beaconManager");
                beaconManagerAlt.unbind(this);
            }
            beaconManagerAlt = null;
        }

    }

    @Override
    public void onBeaconServiceConnect() {
        Log.d(LOGTAG, "onBeaconServiceConnect");
        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    for (Beacon beacon : beacons) {
                        final BeaconObject beaconObject = new BeaconObject(beacon);
                        Log.i(TAG_ALTBEACON, "[Beacons seen:" + beacons.size() + "] " +  beaconObject.toString());
                        mBeaconNotification.update(beaconObject);
                        sendMessageToUI(MessageConstants.MSG_SET_NEW_BEACON_READING, beaconObject);
                    }
                }
            }
        };
        beaconRegion = new Region("myRangingUniqueId", null, null, null);
        beaconManagerAlt.setRangeNotifier(rangeNotifier);

        startAltBeacon();
    }


    private void sendMessageToUI(int id, BeaconObject beaconObject) {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext()) {
            Messenger messenger = messengerIterator.next();
            try {

                Bundle bundle = new Bundle();
                bundle.putParcelable(MessageConstants.MSG_NEW_BEACON_READING, beaconObject);
                Message msg = Message.obtain(null, MessageConstants.MSG_SET_NEW_BEACON_READING);
                msg.setData(bundle);
                messenger.send(msg);


            } catch (RemoteException e) {
                // The client is dead. Remove it from the list.
                mClients.remove(messenger);
            }
        }
    }

    private class IncomingMessageHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            Log.d(LOGTAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case MessageConstants.MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    Log.i(LOGTAG, "Clients: " + mClients.size());
                    break;
                case MessageConstants.MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    Log.i(LOGTAG, "Clients: " + mClients.size());
                    break;
                case MessageConstants.MSG_START_SCANNING:
                    startAltBeacon();
                    break;
                case MessageConstants.MSG_STOP_SCANNING:
                    stopAltBeacon();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


}
