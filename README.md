# BeaconScanner
Scanner for Gimbal Beacons

[Mobile]
* Has two different scanning modes:
1. Gimbal mode - uses Gimbal Android SDK v2.12.1.
2. iBeacon mode - uses Android Beacon Library (https://github.com/AltBeacon/android-beacon-library).

[Wear]
* Only iBeacon mode scanning.
* Deletes beacons not seen in the last n seconds.
* Configures custom duration of BT scanning and time between consecutive scans.
* Beacon scanning implemented as a service.
* Persistent notification when scanning is running. Shows top 3 strongest (RSSI) beacons.
* Uncaught RejectedExecutionException from AltBeacon library may crash app. Should be fixed in next release.

