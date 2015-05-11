package tudelft.beaconscanner;

import com.github.clans.fab.FloatingActionButton;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import tudelft.beaconscanner.enums.MessageConstants;
import tudelft.beaconscanner.enums.SettingsConstants;
import tudelft.beaconscanner.ui_helpers.Adapter;


public class MainActivity extends Activity  implements ServiceConnection {

    private Switch mSwitch;
    private TextView mTextView;
    private WearableListView mListView;
    private Adapter listAdapter = null;
    private ArrayList<String> mBeaconListString = new ArrayList<>();
    private ArrayList<BeaconObject> mBeaconList = new ArrayList<>();

    private FloatingActionButton fab;
    private WatchViewStub stub;
    protected static final String TAG = "BeaconScannerWear";

    private Messenger mServiceMessenger = null;
    private boolean mIsBound;
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    private ServiceConnection mConnection = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Activity state: onCreate");
        setContentView(R.layout.activity_main);
        stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.txtBeaconsSeen);
                mSwitch = (Switch) stub.findViewById(R.id.switch1);
                mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            sendMessageToService(MessageConstants.MSG_START_SCANNING);
                        } else {
                            sendMessageToService(MessageConstants.MSG_STOP_SCANNING);
                        }
                    }
                });

                try{
                    mSwitch.setChecked(BeaconScanning.isScanning());
                    stub.invalidate();
                } catch (Exception e){
                    Log.e(TAG, e.toString());
                }
                mSwitch.setVisibility(View.GONE);
                mListView = (WearableListView) stub.findViewById(R.id.wearable_list);
                redrawList();


                fab = (FloatingActionButton) findViewById(R.id.fab);
                boolean scanning = BeaconScanning.isScanning();
                fab.setImageResource(
                        !scanning ? R.drawable.ic_action_play : R.drawable.ic_action_stop);
                configureFab();
            }
        });

        automaticBind();
    }

    private void configureFab(){
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean scanning = BeaconScanning.isScanning();
                fab.setImageResource(
                        !scanning ? R.drawable.ic_action_stop: R.drawable.ic_action_play);
                if (!scanning) {
                    sendMessageToService(MessageConstants.MSG_START_SCANNING);
                } else {
                    sendMessageToService(MessageConstants.MSG_STOP_SCANNING);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Activity state: onDestroy");
        super.onDestroy();
        automaticUnbind();
    }


    @Override
    protected void onPause() {
        Log.d(TAG, "Activity state: onPause");
        super.onPause();

    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Activity state: onResume");
        super.onResume();

    }


    @Override
    protected void onStart() {
        Log.d(TAG, "Activity state: onStart");
        super.onStart();
        Log.d(TAG, "Scanning: " + BeaconScanning.isScanning());
    }


    @Override
    protected void onStop() {
        Log.d(TAG, "Activity state: onStop");
        super.onStop();

    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "Activity state: onRestart");
        super.onRestart();
    }


    private void automaticBind() {
        if (BeaconScanning.isRunning()) {
            doBindService();
        } else{
            startScanningService();
            doBindService();
        }
    }

    private void automaticUnbind() {
        stopScanningService();
    }

    public void startScanningService(){
        Log.i(TAG, "Scanning Service: START");
        Intent intent = new Intent(this, BeaconScanning.class);
        this.startService(intent);
    }

    public void stopScanningService(){
        Log.i(TAG, "Scanning Service: STOP");
        try {
            //sendMessageToService(MessageConstants.MSG_STOP_SCANNING);
            doUnbindService();
        } catch (Throwable t) {
            Log.e(TAG, "Failed to unbind from the service", t);
        }
        //this.stopService(new Intent(this, SensorReaderService.class));
    }

    private void doBindService() {
        this.bindService(new Intent(this, BeaconScanning.class),
                mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService() {
        if (mIsBound) {
            Log.d(TAG, "Unbinding from service");
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, MessageConstants.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    Log.e(TAG, e.toString());
                }
            }
            // Detach our existing connection.
            this.unbindService(mConnection);
            mIsBound = false;
        }
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mServiceMessenger = new Messenger(service);
        try {
            Message msg = Message.obtain(null, MessageConstants.MSG_REGISTER_CLIENT);
            msg.replyTo = mMessenger;
            mServiceMessenger.send(msg);

        }
        catch (RemoteException e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServiceMessenger = null;
    }

    private void sendMessageToService(int intvaluetosend) {
        if (mIsBound) {
            if (mServiceMessenger != null) {
                try {
                    Message msg = Message
                            .obtain(null, intvaluetosend, 0);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
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
                .getDefaultSharedPreferences(this).getString(
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

    private void redrawList(){
        try{
            mTextView.setText("Beacons seen: " + mBeaconList.size());
            mBeaconListString.clear();
            int i = 1;
            for (BeaconObject bs : mBeaconList){
                mBeaconListString.add(String.valueOf(i) + ". " +  bs.getName() + "       " + bs.getRSSI());
                i++;
            }

            if (mBeaconListString.size() == 0){
                mBeaconListString.add("1. No beacons detected");
            }

            String[] strArr = mBeaconListString.toArray(new String[mBeaconListString.size()]);

            if (mListView.getAdapter() == null){
                listAdapter = new Adapter(this, strArr);
                mListView.setAdapter(listAdapter);
            } else {
                listAdapter.setDataset(strArr);
                listAdapter.notifyDataSetChanged();
            }

        } catch (Exception e){
            Log.e(TAG, e.toString());
        }

    }

    private class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"IncomingHandler:handleMessage");
            switch (msg.what) {
                case MessageConstants.MSG_SET_NEW_BEACON_READING:
                    BeaconObject beaconObject = msg.getData().getParcelable(
                            MessageConstants.MSG_NEW_BEACON_READING);
                    Log.i(TAG, "New beacon reading: " + beaconObject.getName() + "/" + beaconObject.getRSSI());
                    updateBeaconList(beaconObject);
                    removeOldBeacons(SettingsConstants.SECONDS_SINCE_LAST_SEEN);
                    redrawList();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}
