package tudelft.beaconscanner.enums;

public class SettingsConstants {
    public static String SORT_PREFERENCE = "sort_preference";
    public static String SORT_RSSI = "RSSI";
    public static String SORT_ALPHABETICALLY = "Alphabetically";

    public static String SCAN_MODE = "scan_mode";
    public static String MODE_ALL = "All";
    public static String MODE_GIMBAL = "Gimbal";
    public static String MODE_BEACONS = "AltBeacon";

    public static String GIMBAL_API_KEY = "0ef2df58-9665-49c9-b914-015e057751aa";
    public static String BEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    public static String TYPE_GIMBAL = "Gimbal";
    public static String TYPE_BEACON = "AltBeacon";

    public static int SECONDS_SINCE_LAST_SEEN = 15;
    public static int TXPOWER = -62;

    public static long SCAN_DURATION_FOREGROUND = 3000;
    public static long SCAN_DURATION_BACKGROUND = 3000;
    public static long TIME_BETWEEN_SCANS_FOREGROUND = 5000;
    public static long TIME_BETWEEN_SCANS_BACKGROUND = 5000;
}
