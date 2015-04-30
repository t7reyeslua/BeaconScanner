package tudelft.beaconscanner.ui_helpers;

import com.gimbal.android.Beacon;
import com.gimbal.android.BeaconSighting;

import tudelft.beaconscanner.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ExpandableListAdapterBeacons extends BaseExpandableListAdapter {

    public ArrayList<BeaconSighting> tempChild;
    public ArrayList<BeaconSighting> groupItem = new ArrayList<BeaconSighting>();
    public ArrayList<Object> Childtem = new ArrayList<Object>();
    public LayoutInflater minflater;
    public Activity activity;
    private final Context context;

    private static final int[] EMPTY_STATE_SET = {};
    private static final int[] GROUP_EXPANDED_STATE_SET =
            {android.R.attr.state_expanded};
    private static final int[][] GROUP_STATE_SETS = {
            EMPTY_STATE_SET, // 0
            GROUP_EXPANDED_STATE_SET // 1
    };

    public ExpandableListAdapterBeacons(Context context, ArrayList<BeaconSighting> grList,
            ArrayList<Object> childItem) {
        super();
        this.context = context;
        this.groupItem = grList;
        this.Childtem = childItem;
    }

    public void setInflater(LayoutInflater mInflater, Activity act) {
        this.minflater = mInflater;
        activity = act;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        tempChild = (ArrayList<BeaconSighting>) Childtem.get(groupPosition);
        TextView text = null;
        if (convertView == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_subitem_scanned_beacon,parent,false);
        }

        TextView twID = (TextView) convertView.findViewById(R.id.beaconId);
        TextView twBatt = (TextView) convertView.findViewById(R.id.beaconBattery);
        TextView twTemp = (TextView) convertView.findViewById(R.id.beaconTemperature);
        TextView twDate = (TextView) convertView.findViewById(R.id.beaconDate);

        Beacon beacon = tempChild.get(childPosition).getBeacon();
        Date date = new Date(tempChild.get(childPosition).getTimeInMillis());
        Double rssi = Double.valueOf(tempChild.get(childPosition).getRSSI());
        Double accuracy = calculateAccuracy(-62, rssi);
        String distance = String.format("%.2f", accuracy);


        twID.setText(beacon.getIdentifier());
        twBatt.setText(String.valueOf(beacon.getBatteryLevel()));
        twBatt.setText(distance + "m");
        twTemp.setText(String.valueOf((beacon.getTemperature() - 32)*5/9) + "°C");
        SimpleDateFormat dt = new SimpleDateFormat("hh:mm:ss");
        twDate.setText(dt.format(date));

        convertView.setTag(tempChild.get(childPosition));
        return convertView;
    }



    @Override
    public int getChildrenCount(int groupPosition) {
        return ((ArrayList<BeaconSighting>) Childtem.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public int getGroupCount() {
        return groupItem.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item_scanned_beacon,parent,false);
        }


        String name = groupItem.get(groupPosition).getBeacon().getName();
        Integer rssi = groupItem.get(groupPosition).getRSSI();

        TextView twName = (TextView) convertView.findViewById(R.id.beaconName);
        TextView twRSSI = (TextView) convertView.findViewById(R.id.beaconRSSI);

        twName.setText(name);
        twRSSI.setText(String.valueOf(rssi) + " dB");
        convertView.setTag(groupItem.get(groupPosition));

        View ind = convertView.findViewById(R.id.explist_indicator);
        if (ind != null) {
            ImageView indicator = (ImageView) ind;
            if (getChildrenCount(groupPosition) == 0) {

                indicator.setVisibility(View.INVISIBLE);
            } else {
                indicator.setVisibility(View.INVISIBLE);
                indicator.setImageResource(
                        isExpanded ? R.drawable.ic_action_collapse : R.drawable.ic_action_expand);
            }
        }

        TextView tw = (TextView) convertView.findViewById(R.id.explist_bar);


        if (rssi < -80) {
            tw.setBackgroundColor(context.getResources().getColor(R.color.Crimson));
        } else if (rssi < -60) {
            tw.setBackgroundColor(context.getResources().getColor(R.color.OrangeRed));
        } else if (rssi < -40) {
            tw.setBackgroundColor(context.getResources().getColor(R.color.YellowGreen));
        }else {
            tw.setBackgroundColor(context.getResources().getColor(R.color.Green));
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private double calculateAccuracy(int txPower, Double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }
}
