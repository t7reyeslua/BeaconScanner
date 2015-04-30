package tudelft.beaconscanner;

import com.gimbal.android.BeaconEventListener;
import com.gimbal.android.BeaconManager;
import com.gimbal.android.BeaconSighting;
import com.gimbal.android.Gimbal;
import com.github.clans.fab.FloatingActionButton;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import tudelft.beaconscanner.ui_helpers.ExpandableListAdapterBeacons;


public class MainActivity extends ActionBarActivity {
    private BeaconEventListener beaconSightingListener;
    private BeaconManager beaconManager;
    private ExpandableListView mExpandableListBeacons;
    private ExpandableListAdapterBeacons mExpandableListAdapterBeacons;
    private ArrayList<BeaconSighting> groupItem = new ArrayList<>();
    private ArrayList<Object> childItem = new ArrayList<>();
    private FloatingActionButton fab;
    private Boolean scanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Gimbal.setApiKey(this.getApplication(), "0ef2df58-9665-49c9-b914-015e057751aa");

        mExpandableListBeacons = (ExpandableListView) findViewById(R.id.exp_list);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        configureFab();

        beaconSightingListener = new BeaconEventListener() {
            @Override
            public void onBeaconSighting(BeaconSighting sighting) {
                Log.i("INFO", sighting.toString());
                updateScanResults(sighting);
            }
        };

        beaconManager = new BeaconManager();

    }



    private void configureFab(){

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("FAB", "Click " + scanning);
                if (!scanning){
                    beaconManager.addListener(beaconSightingListener);
                    beaconManager.startListening();
                } else {
                    beaconManager.removeListener(beaconSightingListener);
                    beaconManager.stopListening();
                }
                fab.setImageResource(
                        scanning ? R.drawable.ic_action_play : R.drawable.ic_action_stop);
                scanning = !scanning;
            }
        });

    }


    private void updateScanResults(BeaconSighting sighting){
        String beaconId = sighting.getBeacon().getIdentifier();
        childItem.clear();
        Boolean found  = false;
        ArrayList<BeaconSighting> tempList = new ArrayList<>();
        for (BeaconSighting bs : groupItem){
            ArrayList<BeaconSighting> child = new ArrayList<>();
            if (bs.getBeacon().getIdentifier().equals(beaconId)){
                tempList.add(sighting);
                child.add(sighting);
                found = true;
            } else {
                tempList.add(bs);
                child.add(bs);
            }
            childItem.add(child);
        }
        if (!found){
            tempList.add(sighting);
            ArrayList<BeaconSighting> child = new ArrayList<>();
            child.add(sighting);
            childItem.add(child);
        }
        groupItem = new ArrayList<>(tempList);
        ArrayList<Boolean>expanded = new ArrayList<>();
        for ( int i = 0; i < groupItem.size(); i++ ) {
            try {
                expanded.add(mExpandableListBeacons.isGroupExpanded(i));
            } catch (Exception e) {
                expanded.add(false);
            }
        }

        int index = mExpandableListBeacons.getFirstVisiblePosition();
        View v = mExpandableListBeacons.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();

        sortGimbalByRSSI();
        if (mExpandableListBeacons.getAdapter() == null) {
            mExpandableListAdapterBeacons = new ExpandableListAdapterBeacons(this, groupItem, childItem);
            mExpandableListBeacons.setAdapter(mExpandableListAdapterBeacons);
            mExpandableListBeacons.setOnGroupClickListener(new ExpDrawerGroupClickListener());
        } else {
            mExpandableListAdapterBeacons.groupItem = groupItem;
            mExpandableListAdapterBeacons.Childtem = childItem;
            mExpandableListAdapterBeacons.notifyDataSetChanged();
        }
        for ( int i = 0; i < groupItem.size(); i++ ) {
            if (expanded.get(i)) {
                mExpandableListBeacons.expandGroup(i);
            }
        }
        mExpandableListBeacons.setSelectionFromTop(index, top);

    }

    private void sortGimbalByRSSI(){
        Collections.sort(groupItem, new Comparator<BeaconSighting>() {
            @Override
            public int compare(BeaconSighting item1, BeaconSighting item2) {

                return item2.getRSSI().compareTo(item1.getRSSI());
            }
        });

        Collections.sort(childItem, new Comparator<Object>() {
            @Override
            public int compare(Object item1, Object item2) {
                BeaconSighting i1 = ((ArrayList<BeaconSighting>)item1).get(0);
                BeaconSighting i2 = ((ArrayList<BeaconSighting>)item2).get(0);

                return i2.getRSSI().compareTo(i1.getRSSI());
            }
        });
    }


    private void sortGimbalByName(){
        Collections.sort(groupItem, new Comparator<BeaconSighting>() {
            @Override
            public int compare(BeaconSighting item1, BeaconSighting item2) {

                return item1.getBeacon().getName().compareTo(item2.getBeacon().getName());
            }
        });

        Collections.sort(childItem, new Comparator<Object>() {
            @Override
            public int compare(Object item1, Object item2) {
                BeaconSighting i1 = ((ArrayList<BeaconSighting>)item1).get(0);
                BeaconSighting i2 = ((ArrayList<BeaconSighting>)item2).get(0);

                return i1.getBeacon().getName().compareTo(i2.getBeacon().getName());
            }
        });
    }


    private class ExpDrawerGroupClickListener implements ExpandableListView.OnGroupClickListener {
        @Override
        public boolean onGroupClick(ExpandableListView parent, View v,
                int groupPosition, long id) {

            if (parent.isGroupExpanded(groupPosition)){
                parent.collapseGroup(groupPosition);
            }else {
                parent.expandGroup(groupPosition, true);
            }
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
