package tudelft.beaconscanner.ui_helpers;

import com.gimbal.android.Beacon;
import com.gimbal.android.BeaconSighting;

import tudelft.beaconscanner.BeaconObject;
import tudelft.beaconscanner.R;
import tudelft.beaconscanner.enums.SettingsConstants;

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

    public ArrayList<BeaconObject> tempChild;
    public ArrayList<BeaconObject> groupItem = new ArrayList<>();
    public ArrayList<Object> Childtem = new ArrayList<>();
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

    public ExpandableListAdapterBeacons(Context context, ArrayList<BeaconObject> grList,
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
        tempChild = (ArrayList<BeaconObject>) Childtem.get(groupPosition);
        TextView text = null;
        if (convertView == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_subitem_scanned_beacon,parent,false);
        }

        TextView twField1 = (TextView) convertView.findViewById(R.id.beaconId);
        TextView twField2 = (TextView) convertView.findViewById(R.id.beaconBattery);
        TextView twField3 = (TextView) convertView.findViewById(R.id.beaconTemperature);
        TextView twField4 = (TextView) convertView.findViewById(R.id.beaconDate);

        BeaconObject beacon = tempChild.get(childPosition);
        String distance = String.format("%.2f", beacon.getDistance());

        if (beacon.getBeaconType().equals(SettingsConstants.TYPE_GIMBAL)) {
            twField1.setText(beacon.getId());
            twField2.setText(String.valueOf(beacon.getBatteryLevel()));
            twField2.setText(distance + "m");
            twField3.setText(String.valueOf(beacon.getTemperature() + "°C"));
        } else if (beacon.getBeaconType().equals(SettingsConstants.TYPE_BEACON)) {
            twField1.setText("Tx: " + beacon.getTxPower() + " dBm");
            twField2.setText(distance + "m");
            twField3.setText("Type: " + beacon.getBeaconTypeCode());
        }
        twField4.setText(beacon.getTimestamp());

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


        String name = groupItem.get(groupPosition).getName();
        Integer rssi = groupItem.get(groupPosition).getRSSI();

        TextView twName = (TextView) convertView.findViewById(R.id.beaconName);
        TextView twRSSI = (TextView) convertView.findViewById(R.id.beaconRSSI);

        twName.setText(name);
        twRSSI.setText(String.valueOf(rssi) + " dBm");
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


}
