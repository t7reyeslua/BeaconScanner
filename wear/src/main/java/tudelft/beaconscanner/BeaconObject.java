package tudelft.beaconscanner;


import com.gimbal.android.BeaconSighting;

import org.altbeacon.beacon.Beacon;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;

import tudelft.beaconscanner.enums.SettingsConstants;


public class BeaconObject implements Parcelable{
    private String beaconType;

    private String name;
    private Integer RSSI;

    private String id;
    private String uuid;
    private String major;
    private String minor;

    private int manufacturer;
    private int beaconTypeCode;
    private int txPower;

    private double distance;
    private int temperature;
    private String timestamp;
    private String batteryLevel;

    public BeaconObject() {
    }

    public BeaconObject(BeaconSighting sighting){
        this.beaconType = SettingsConstants.TYPE_GIMBAL;
        this.name = sighting.getBeacon().getName();
        this.RSSI = sighting.getRSSI();
        this.id = sighting.getBeacon().getIdentifier();
        this.temperature = ((sighting.getBeacon().getTemperature()- 32)*5/9);
        this.batteryLevel = String.valueOf(sighting.getBeacon().getBatteryLevel());

        Date date = new Date(sighting.getTimeInMillis());
        SimpleDateFormat dt = new SimpleDateFormat("hh:mm:ss");
        this.timestamp = dt.format(date);

        Double rssi = this.RSSI * 1.0;
        this.distance = calculateAccuracy(SettingsConstants.TXPOWER, rssi);

    }

    public BeaconObject(Beacon beacon){
        String filler = "-";
        if (beacon.getId3().toString().length() == 1){
            filler += "0";
        }

        this.beaconType = SettingsConstants.TYPE_BEACON;
        this.name = beacon.getId2().toString() + filler + beacon.getId3().toString();
        this.RSSI = beacon.getRssi();

        this.uuid = beacon.getId1().toString();
        this.id   = this.uuid + "-" + this.name;
        this.major = beacon.getId2().toString();
        this.minor = beacon.getId3().toString();

        this.beaconTypeCode = beacon.getBeaconTypeCode();
        this.manufacturer   = beacon.getManufacturer();
        this.txPower        = beacon.getTxPower();

        this.distance       = beacon.getDistance();
        Date date = new Date();
        SimpleDateFormat dt = new SimpleDateFormat("hh:mm:ss");
        this.timestamp = dt.format(date);

    }


    public String getBeaconType() {
        return beaconType;
    }

    public void setBeaconType(String beaconType) {
        this.beaconType = beaconType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRSSI() {
        return RSSI;
    }

    public void setRSSI(Integer RSSI) {
        this.RSSI = RSSI;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getMinor() {
        return minor;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }

    public int getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(int manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getBeaconTypeCode() {
        return beaconTypeCode;
    }

    public void setBeaconTypeCode(int beaconTypeCode) {
        this.beaconTypeCode = beaconTypeCode;
    }

    public int getTxPower() {
        return txPower;
    }

    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(String batteryLevel) {
        this.batteryLevel = batteryLevel;
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

    @Override
    public String toString() {
        return "BeaconObject{" +
                "beaconType='" + beaconType + '\'' +
                ", name='" + name + '\'' +
                ", RSSI=" + RSSI +
                ", id='" + id + '\'' +
                ", uuid='" + uuid + '\'' +
                ", major='" + major + '\'' +
                ", minor='" + minor + '\'' +
                ", manufacturer=" + manufacturer +
                ", beaconTypeCode=" + beaconTypeCode +
                ", txPower=" + txPower +
                ", distance=" + distance +
                ", temperature=" + temperature +
                ", timestamp='" + timestamp + '\'' +
                ", batteryLevel='" + batteryLevel + '\'' +
                '}';
    }


    // Methods for implementing Parcelable
    public int describeContents() {
        return 0;
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<BeaconObject> CREATOR = new Parcelable.Creator<BeaconObject>() {
        public BeaconObject createFromParcel(Parcel in) {
            return new BeaconObject(in);
        }

        public BeaconObject[] newArray(int size) {
            return new BeaconObject[size];
        }
    };


    // example constructor that takes a Parcel and gives you an object populated with it's values
    private BeaconObject(Parcel in) {
        this.beaconType = in.readString();
        this.name = in.readString();
        this.RSSI = in.readInt();

        this.id = in.readString();
        this.uuid = in.readString();
        this.major = in.readString();
        this.minor = in.readString();

        this.manufacturer = in.readInt();
        this.beaconTypeCode = in.readInt();
        this.txPower = in.readInt();

        this.distance = in.readDouble();
        this.temperature = in.readInt();
        this.timestamp = in.readString();
        this.batteryLevel = in.readString();
    }

    // write your object's data to the passed-in Parcel
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.beaconType);
        out.writeString(this.name);
        out.writeInt(this.RSSI);

        out.writeString(this.id);
        out.writeString(this.uuid);
        out.writeString(this.major);
        out.writeString(this.minor);

        out.writeInt(this.manufacturer);
        out.writeInt(this.beaconTypeCode);
        out.writeInt(this.txPower);

        out.writeDouble(this.distance);
        out.writeInt(this.temperature);
        out.writeString(this.timestamp);
        out.writeString(this.batteryLevel);
    }




}
