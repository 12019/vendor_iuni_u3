/*====*====*====*====*====*====*====*====*====*====*====*====*====*====*====*
  Copyright (c) 2014 Qualcomm Technologies, Inc.  All Rights Reserved.
  Qualcomm Technologies Proprietary and Confidential.
=============================================================================*/

/*============================================================
Copyright (c) 2012-2013 Qualcomm Atheros, Inc.
All Rights Reserved.
Qualcomm Atheros Confidential and Proprietary.
=============================================================================*/
/* Not a Contribution, Apache license notifications and license are retained

*  for attribution purposes only.
*
*  Copyright (C) 2008 The Android Open Source Project
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/

package com.qualcomm.location.ulp;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import android.os.Handler;
import android.os.Message;
import android.os.Looper;
import android.os.Looper;
import android.os.WorkSource;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.BatteryManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Map;
import java.lang.reflect.Field;
import android.util.Log;
import java.util.Iterator;

import com.android.location.provider.LocationRequestUnbundled;
import com.android.location.provider.ProviderRequestUnbundled;
import com.android.location.provider.LocationProviderBase;
import android.location.LocationListener;
import android.location.LocationRequest;
import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.os.PowerManager;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class UlpEngine {
    public interface Callback {
        public void reportLocation(Location location);
    }
    private static UlpEngine mInstance = null;
    public static final String ENH_LOCATION_SERVICES_ENABLED = "enhLocationServices_on";
    public static final String PIP_USER_SETTING_UPDATE =
        "com.qualcomm.location.PIP_USER_SETTING_UPDATE";
    // for Settings change notification
    private ContentQueryMap mSecureSettings, mGlobalSettings;
    private static final String TAG = "UlpEngine";
    private static final boolean VERBOSE_DBG = Log.isLoggable(TAG, Log.VERBOSE);
    private final Context mContext;
    private final Looper mLooper;
    // Handler for processing events
    private Handler mHandler;

    private Callback mCallback;
    private boolean mEnabled;
    private ProviderRequestUnbundled mRequest;
    private Set<UlpLocRequest> mRequestCacheSet =  new HashSet<UlpLocRequest>();
    private Set<UlpLocRequest> mRequestActiveSet =  new HashSet<UlpLocRequest>();
    public static final int ULP_ADD_CRITERIA = 1;
    public static final int ULP_REMOVE_CRITERIA = 2;
    public static final int INDEX_ZERO = 0;
    private static final int ULP_PROVIDER_SOURCE_GNSS = 0x1;
    private static final int ULP_PROVIDER_SOURCE_HYBRID = 0x2;
    // these need to match GpsLocationFlags enum in ulp_service.h
    private static final int LOCATION_INVALID = 0;
    private static final int LOCATION_HAS_LAT_LONG = 1;
    private static final int LOCATION_HAS_ALTITUDE = 2;
    private static final int LOCATION_HAS_SPEED = 4;
    private static final int LOCATION_HAS_BEARING = 8;
    private static final int LOCATION_HAS_ACCURACY = 16;
    private static final int LOCATION_HAS_SOURCE_INFO = 0x20;
    private static final int LOCATION_HAS_IS_INDOOR = 0x40;
    private static final int LOCATION_HAS_FLOOR_NUMBER = 0x80;
    private static final int LOCATION_HAS_MAP_URL = 0x100;
    private static final int LOCATION_HAS_MAP_INDEX = 0x200;

    //ULP Defines
    private static final int ULP_LOCATION_IS_FROM_HYBRID = 0x1;
    private static final int ULP_LOCATION_IS_FROM_GNSS = 0x2;
    private static final int ULP_NETWORK_POS_STATUS_REQUEST = 1;
    private static final int ULP_NETWORK_POS_START_PERIODIC_REQUEST = 2;
    private static final int ULP_NETWORK_POS_GET_LAST_KNOWN_LOCATION_REQUEST = 3;
    private static final int ULP_NETWORK_POS_STOP_REQUEST = 4;
    private static final int ULP_NETWORK_POSITION_SRC_WIFI = 1;
    private static final int ULP_NETWORK_POSITION_SRC_CELL = 2;
    private static final int ULP_NETWORK_POSITION_SRC_UNKNOWN = 255;

    private volatile boolean mGpsSetting = false;
    private volatile boolean mAgpsSetting = false;
    private volatile boolean mNetworkProvSetting = false;
    private volatile boolean mWifiSetting = false;
    private volatile boolean mEnhServicesSetting = false;
    private volatile boolean mPipUserSetting = false;

    private static final int ULP_PHONE_CONTEXT_GPS_SETTING       = 0x1;
    private static final int ULP_PHONE_CONTEXT_NETWORK_POSITION_SETTING       = 0x2;
    private static final int ULP_PHONE_CONTEXT_WIFI_SETTING= 0x4;
    private static final int ULP_PHONE_CONTEXT_BATTERY_CHARGING_STATE      = 0x8;
    private static final int ULP_PHONE_CONTEXT_AGPS_SETTING = 0x10;
    private static final int ULP_PHONE_CONTEXT_ENH_LOCATION_SERVICES_SETTING = 0x20;
    private static final int ULP_PHONE_CONTEXT_PIP_USER_SETTING = 0x40;

    /** Required Settings subscription flag*/
    private static final int ULP_PHONE_CONTEXT_GPS_ON        = 0x1;
    private static final int ULP_PHONE_CONTEXT_GPS_OFF       = 0x2;
    private static final int ULP_PHONE_CONTEXT_AGPS_ON        = 0x4;
    private static final int ULP_PHONE_CONTEXT_AGPS_OFF       = 0x8;
    private static final int ULP_PHONE_CONTEXT_CELL_BASED_POSITION_ON= 0x10;
    private static final int ULP_PHONE_CONTEXT_CELL_BASED_POSITION_OFF= 0x20;
    private static final int ULP_PHONE_CONTEXT_WIFI_ON      = 0x40;
    private static final int ULP_PHONE_CONTEXT_WIFI_OFF     = 0x80;
    private static final int ULP_PHONE_CONTEXT_ENH_LOCATION_SERVICES_ON= 0x400;
    private static final int ULP_PHONE_CONTEXT_ENH_LOCATION_SERVICES_OFF= 0x800;

    /** return phone context only once */
    private static final int ULP_PHONE_CONTEXT_REQUEST_TYPE_SINGLE= 0x1;
    /** return phone context when it changes */
    private static final int ULP_PHONE_CONTEXT_REQUEST_TYPE_ONCHANGE= 0x2;
    private static final int ULP_PHONE_CONTEXT_UPDATE_TYPE_SINGLE= 0x1;
    private static final int ULP_PHONE_CONTEXT_UPDATE_TYPE_ONCHANGE= 0x2;
    private volatile int mRequestType = 0;
    private volatile int mRequestContextType = 0;

    // Handler messages
    private static final int ENABLE = 1;
    private static final int SET_REQUEST = 2;
    private static final int REQUEST_PHONE_CONTEXT_SETTINGS = 3;
    private static final int UPDATE_NATIVE_PHONE_CONTEXT_SETTINGS = 4;
    private static final int REQUEST_NETWORK_LOCATION = 5;
    private static final int UPDATE_NETWORK_LOCATION = 6;
    private static final int INJECT_USER_LCI_SELECTION = 7;

    // System events - should match the ones defined in loc_ulp.h
    private static final int ULP_LOC_SCREEN_ON = 0;
    private static final int ULP_LOC_TIMEZONE_CHANGE = 1;
    private static final int ULP_LOC_POWER_CONNECTED = 2;
    private static final int ULP_LOC_POWER_DISCONNECTED = 3;
    private static final int ULP_LOC_PHONE_CONTEXT_UPDATE = 4;

    private LocationManager mLocMgr;
    private Cursor mSecureSettingsCursor, mGlobalSettingsCursor;

    // LCI
    private UlpLciListener mLciListener = null;
    private static final String ULP_PIP_USER_LCI_DETERMINATION_MODE = "pip_lci_determination_mode";

    // System satus
    private boolean mScreenOn;

    //To make UlpEngine a singleton
    private UlpEngine(Context context, Looper looper) {
        mContext = context;
        mLooper = looper;
        if(VERBOSE_DBG) Log.v(TAG, "Create UlpEngine");
        // construct handler, listen for events
        mHandler = new UlpEngineHandler(mLooper);
        mLocMgr = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public static UlpEngine getInstance(Context context, Looper looper) {
        if (mInstance == null) {
            synchronized (UlpEngine.class) {
                if (mInstance == null) {
                    mInstance = new UlpEngine(context, looper);
                    }
                }
            }
        return mInstance;
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (VERBOSE_DBG)
                Log.v(TAG, "mBroadcastReceiver - " + action);

            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                setScreenStatus(true);
                native_ue_system_update(ULP_LOC_SCREEN_ON);

            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                setScreenStatus(false);

            }  else if(action.equals(Intent.ACTION_TIMEZONE_CHANGED)){
                native_ue_system_update(ULP_LOC_TIMEZONE_CHANGE);

            } else if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                native_ue_system_update(ULP_LOC_POWER_CONNECTED);

            } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                native_ue_system_update(ULP_LOC_POWER_DISCONNECTED );

            }  else {
                Log.v(TAG,"Received Unexpected Intent");
            }
        }
    };

    public final void sendMessage(int what, int arg1, int arg2, Object obj) {
        mHandler.removeMessages(what);
        Message m = Message.obtain(mHandler, what, arg1, arg2, obj);
        mHandler.sendMessage(m);
    }

    public void init() {
    }

    public void init(Callback callback) {
        Log.i(TAG, "engine started (" + mContext.getPackageName() + ")");
        mCallback = callback;
        native_ue_init();

        // Get the current battery updates from sticky intent. This is why a null
        // broadcast receiver is allowed.
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryIntent = mContext.registerReceiver(null, intentFilter);

        int plugged = -1;
        plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean currentBatteryCharging = ((plugged == BatteryManager.BATTERY_PLUGGED_AC)
                             || (plugged == BatteryManager.BATTERY_PLUGGED_USB));

        if (currentBatteryCharging) native_ue_system_update(ULP_LOC_POWER_CONNECTED);

        //Register a receiver for processing System Wakeup Injection
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        mContext.registerReceiver(mBroadcastReceiver, filter);
        PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        setScreenStatus(pm.isScreenOn());

        // listen for settings changes
        ContentResolver resolver = mContext.getContentResolver();
        mSecureSettingsCursor = resolver.query(Settings.Secure.CONTENT_URI,
                new String[] {Settings.System.NAME,Settings.System.VALUE},
                "(" + Settings.System.NAME + "=?) or ("
                    + Settings.System.NAME + "=?) ",
                new String[]{Settings.Secure.LOCATION_PROVIDERS_ALLOWED,
                    ENH_LOCATION_SERVICES_ENABLED},
                    null);
        mSecureSettings = new ContentQueryMap(mSecureSettingsCursor, Settings.System.NAME, true, mHandler);
        SecureSettingsObserver secureSettingsObserver = new SecureSettingsObserver();
        mSecureSettings.addObserver(secureSettingsObserver);

        mGlobalSettingsCursor = resolver.query(Settings.Global.CONTENT_URI,
                new String[] {Settings.System.NAME,Settings.System.VALUE},
                "(" + Settings.System.NAME + "=?) or ("
                    + Settings.System.NAME + "=?) ",
                new String[]{Settings.Global.WIFI_ON, Settings.Global.ASSISTED_GPS_ENABLED},
                    null);
        mGlobalSettings = new ContentQueryMap(mGlobalSettingsCursor, Settings.System.NAME, true, mHandler);
        GlobalSettingsObserver globalSettingsObserver = new GlobalSettingsObserver();
        mGlobalSettings.addObserver(globalSettingsObserver);
    }

    public void deinit() {
        mRequest = null;
        disable();
        Log.i(TAG, "engine stopped (" + mContext.getPackageName() + ")");
    }

    /**
     * Enables the UlpEngine
     */
    public void enable() {
        sendMessage(ENABLE, 1, 0, null);
    }

    private void handleEnable() {
        if (VERBOSE_DBG) Log.v(TAG, "handleEnable");
        if (mEnabled) return;
        mEnabled = true;
        updateRequirements();
    }

    public void disable() {
        sendMessage(ENABLE, 0, 0, null);
        if(null != mSecureSettings) {
            mSecureSettings.close();
            mSecureSettings = null;
        }
        if(null != mGlobalSettings) {
            mGlobalSettings.close();
            mGlobalSettings = null;
        }
        if(null != mSecureSettingsCursor) {
            mSecureSettingsCursor.close();
            mSecureSettingsCursor = null;
        }
        if(null != mGlobalSettingsCursor) {
            mGlobalSettingsCursor.close();
            mGlobalSettingsCursor = null;
        }
    }

    public void handleDisable() {
        if(! mEnabled) {
            //Do nothing if we have not been enabled in the first place
            return;
        }
        mEnabled = false;
        updateRequirements();
    }

    private void updateRequirements() {

        if(VERBOSE_DBG) Log.v(TAG, "updateRequirements. mEnabled: "+ mEnabled + " mRequest : "+mRequest);

        int cacheSize = mRequestCacheSet.size();
        if (mEnabled == false || mRequest == null) {
            mRequest = null;
            if (cacheSize != 0) {
                Iterator iterCache = mRequestCacheSet.iterator();
                //Remove all active Criteria that we have cached
                while (iterCache.hasNext()) {
                    UlpLocRequest req = (UlpLocRequest) iterCache.next();
                    req.updateCriteria(ULP_REMOVE_CRITERIA);
                }
                mRequestCacheSet.clear();
            }
            native_ue_stop();
            return;
        }
        int mRequestSize = mRequest.getLocationRequests().size();
        if (VERBOSE_DBG) {
            Log.v(TAG, "updateRequirements. mRequestSize :"+ mRequestSize
                  +"cacheSize: "+cacheSize );}

        mRequestActiveSet.clear();
        for (int i=0 ;i < mRequestSize;i++) {
            UlpLocRequest locReq =  new UlpLocRequest(mRequest.getLocationRequests().get(i));
            //Add only those Criteria that are unique and not already present in out cache
            if (mRequestActiveSet.add(locReq) && !mRequestCacheSet.contains(locReq)) {
                locReq.updateCriteria(ULP_ADD_CRITERIA);
                Log.v(TAG, "unique Criteria :"+ locReq);
            }
        }

        if(cacheSize == 0) {
            //This is the first active Criteria so send start
            native_ue_start();
        } else {
            //At this point we have all the required active Criteria in mRequestActiveSet
            //Need to compare this with our Cache to determine which Criteria to remove
            Iterator iterCache = mRequestCacheSet.iterator();
            while (iterCache.hasNext()) {
                UlpLocRequest req = (UlpLocRequest) iterCache.next();
                if ( !mRequestActiveSet.contains(req)) {
                    req.updateCriteria(ULP_REMOVE_CRITERIA);
                    Log.v(TAG, "remove Criteria :"+ req);
                }
            }
        }
        mRequestCacheSet.clear();
        mRequestCacheSet.addAll(mRequestActiveSet);
    }

    public void setRequest(ProviderRequestUnbundled request, WorkSource source) {
        if(VERBOSE_DBG) Log.v(TAG, "setRequest received. mEnabled: " + request.getReportLocation());
        sendMessage(SET_REQUEST, 0, 0, request);
    }

    public void handleSetRequest(ProviderRequestUnbundled request) {
        mRequest = request;
        mEnabled = request.getReportLocation();
        updateRequirements();
    }


    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
    }

     /**
     * called from native code to update FLP position.
     */
    private void reportLocation(int flags, double latitude, double longitude, double altitude,
                                float speed, float bearing, float accuracy, long timestamp, int positionSource, byte[] rawData,
                                boolean isIndoor, float floorNumber, String mapUrl, String mapIndex) {

        if (VERBOSE_DBG) {
            Log.v(TAG, "reportLocation lat: " + latitude + " long: " + longitude +
                  " timestamp: " + timestamp + " positionSource: " + positionSource);
        }

        Location location = new Location(LocationManager.FUSED_PROVIDER);
        Bundle locationExtras = new Bundle();

        if ((flags & LOCATION_HAS_LAT_LONG) == LOCATION_HAS_LAT_LONG) {
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setTime(timestamp);
        }
        if ((flags & LOCATION_HAS_ALTITUDE) == LOCATION_HAS_ALTITUDE) {
            location.setAltitude(altitude);
        } else {
            location.removeAltitude();
        }
        if ((flags & LOCATION_HAS_SPEED) == LOCATION_HAS_SPEED) {
            location.setSpeed(speed);
        } else {
            location.removeSpeed();
        }
        if ((flags & LOCATION_HAS_BEARING) == LOCATION_HAS_BEARING) {
            location.setBearing(bearing);
        } else {
            location.removeBearing();
        }
        if ((flags & LOCATION_HAS_ACCURACY) == LOCATION_HAS_ACCURACY) {
            location.setAccuracy(accuracy);
        } else {
            location.removeAccuracy();
        }
        if (VERBOSE_DBG) Log.v(TAG, "reportLocation.flag:" +flags);

        if(rawData != null) {
            if (rawData.length > 0) {
                locationExtras.putByteArray("RawData", rawData);
                // XXX: will this code be reached wheneven rawData get updated?
                handleReportLciCandidates(new String(rawData));
            } else {
                locationExtras.remove("RawData");
            }
        }

        if ((flags & LOCATION_HAS_IS_INDOOR) == LOCATION_HAS_IS_INDOOR) {
            locationExtras.putBoolean("isIndoor", isIndoor);
        } else {
            locationExtras.remove("isIndoor");
        }

        if ((flags & LOCATION_HAS_FLOOR_NUMBER) == LOCATION_HAS_FLOOR_NUMBER) {
            locationExtras.putFloat("floorNumber", floorNumber);
        } else {
            locationExtras.remove("floorNumber");
        }

        if ((flags & LOCATION_HAS_MAP_URL) == LOCATION_HAS_MAP_URL) {
            locationExtras.putCharSequence("mapUrl", mapUrl);
        } else {
            locationExtras.remove("mapUrl");
        }

        if ((flags & LOCATION_HAS_MAP_INDEX) == LOCATION_HAS_MAP_INDEX) {
            locationExtras.putCharSequence("mapIndex", mapIndex);
        } else {
            locationExtras.remove("mapIndex");
        }

        location.setExtras(locationExtras);
        location.makeComplete();
        mCallback.reportLocation(location);
    }

    /**
     * Called from native code to request network location info
     */
    private void requestNetworkLocation(int type, int interval, int source)
    {
         if (VERBOSE_DBG) Log.v(TAG, "requestNetworkLocation. type: "+ type+ "interval: "+ interval+"source "+source);
         sendMessage(REQUEST_NETWORK_LOCATION, type, interval,null );
    }

    private void handleNetworkLocationUpdate(Location location) {
         Log.v(TAG, "handleNetworkLocationUpdate. lat" + location.getLatitude()+ "lon" +
            location.getLongitude() + "accurancy " + location.getAccuracy());
            if (location.hasAccuracy()) {
                //TODO: need to remove the following
            native_ue_send_network_location(location.getLatitude(), location.getLongitude(),
                    location.getAccuracy(), location.getTime());
            }
    }

    /*
     * ULP request for network location info
     *
     */
     private LocationListener mNetworkLocationListener = new LocationListener() {
         public void onLocationChanged(Location location) {
               if (VERBOSE_DBG) Log.v(TAG, "onLocationChanged for NLP lat" + location.getLatitude()+ "lon" +
               location.getLongitude() + "accurancy " + location.getAccuracy());
               sendMessage(UPDATE_NETWORK_LOCATION, 0, 0, location);
         }
         public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
               if (VERBOSE_DBG) Log.v(TAG, "Status update for NLP" + arg0);
             }
         public void onProviderEnabled(String arg0) {
             if (VERBOSE_DBG) Log.v(TAG, "onProviderEnabled for NLP.state " + arg0);
             }
         public void onProviderDisabled(String arg0) {
             if (VERBOSE_DBG) Log.v(TAG, "onProviderEnabled for NLP.state " + arg0);
             }
     };

    private void handleNativeNetworkLocationRequest(int type, int interval)
    {
         switch(type) {
            case ULP_NETWORK_POS_START_PERIODIC_REQUEST:
                if (VERBOSE_DBG) Log.v(TAG, "requestNetworkLocation NLP start from GP");
                mLocMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER ,interval, 0, mNetworkLocationListener);
                break;
            case ULP_NETWORK_POS_GET_LAST_KNOWN_LOCATION_REQUEST:
                Location location = mLocMgr.getLastKnownLocation("LocationManager.NETWORK_PROVIDER");
                if (VERBOSE_DBG) Log.v(TAG, "requestNetworkLocation NLP last known location from GP:" + location);
                sendMessage(UPDATE_NETWORK_LOCATION, 0, 0, location);
                break;
             case ULP_NETWORK_POS_STOP_REQUEST:
                if (VERBOSE_DBG) Log.v(TAG, "requestNetworkLocation NLP stop from GP");
                mLocMgr.removeUpdates(mNetworkLocationListener);
                break;
             default:
                  Log.e(TAG, "requestNetworkLocation. Inccorect request sent in: "+type);

            }

    }

    /*
     * Called from native code to request phone context info
     */
    private void requestPhoneContext (int context_type, int request_type)
    {
        if (VERBOSE_DBG) Log.v(TAG, "requestPhoneContext from native layer.context_type: "+
                  context_type+ " request_type:"+ request_type);
        sendMessage(REQUEST_PHONE_CONTEXT_SETTINGS , context_type, request_type, null );
    }

    private void handleNativePhoneContextRequest(int contextType, int requestType)
    {
        //update the Global request settings and set up a native update cycle
        mRequestContextType = contextType;
        mRequestType = requestType;
        if (VERBOSE_DBG) Log.v(TAG, "handleNativePhoneContextRequest invoked from native layer with mRequestContextType: "+
              mRequestContextType+" mRequestType:"+mRequestType);
        handleNativePhoneContextUpdate(ULP_PHONE_CONTEXT_UPDATE_TYPE_SINGLE, null);
    }

    private void handleNativePhoneContextUpdate(int updateType, Bundle settingsValues)
    {
      int currentContextType = 0;
      //Read all the current settings and update mask value
      boolean currentAgpsSetting = false, currentWifiSetting  = false,
          currentGpsSetting = false, currentNetworkProvSetting = false,
          currentBatteryCharging = false, currentEnhLocationServicesSetting = false,
          currentPipUserSetting = false;
      boolean wasAgpsSettingAvailable = false, wasWifiSettingAvailable = false,
          wasGpsSettingAvailable = false, wasNetworkProviderSettingAvailable = false,
          wasBatteryChargingAvailable = false, wasEnhLocationServicesSettingAvailable = false,
          wasPipUserSetting = false;

      ContentResolver resolver = mContext.getContentResolver();
      if(VERBOSE_DBG) {
          Log.v(TAG, "handleNativePhoneContextUpdate called. updateType: "+ updateType
                + " mRequestContextType: " + mRequestContextType + " mRequestType: " +
                mRequestType);
      }
      if(mRequestContextType == 0) {
         if (VERBOSE_DBG) Log.v(TAG, "handleNativePhoneContextUpdate. Update obtained before request. Ignoring");
         return;
      }
      try {
          //Update Gps Setting
          if( (mRequestContextType & ULP_PHONE_CONTEXT_GPS_SETTING) == ULP_PHONE_CONTEXT_GPS_SETTING )
          {
              if(updateType == ULP_PHONE_CONTEXT_UPDATE_TYPE_SINGLE) {
                  currentGpsSetting =
                      Settings.Secure.isLocationProviderEnabled(resolver, LocationManager.GPS_PROVIDER);
                  wasGpsSettingAvailable = true;
              }else
              {
                  if(settingsValues.containsKey("gpsSetting"))
                  {
                      wasGpsSettingAvailable = true;
                      currentGpsSetting = settingsValues.getBoolean("gpsSetting");
                  }
              }
          }
          //Update AGps Setting
          if( (mRequestContextType & ULP_PHONE_CONTEXT_AGPS_SETTING) == ULP_PHONE_CONTEXT_AGPS_SETTING)
          {
              if(updateType == ULP_PHONE_CONTEXT_UPDATE_TYPE_SINGLE) {
                  currentAgpsSetting =
                      (Settings.Global.getInt(resolver,Settings.Global.ASSISTED_GPS_ENABLED) == 1);
                  wasAgpsSettingAvailable = true;
              }else
              {
                  if(settingsValues.containsKey("agpsSetting"))
                  {
                      wasAgpsSettingAvailable = true;
                      currentAgpsSetting = settingsValues.getBoolean("agpsSetting");
                  }
              }
         }

          //Update GNP Setting
          if((mRequestContextType & ULP_PHONE_CONTEXT_NETWORK_POSITION_SETTING)
               == ULP_PHONE_CONTEXT_NETWORK_POSITION_SETTING)
          {
              List<String> providers = mLocMgr.getAllProviders();
              boolean networkLocProvAvailable = (providers.contains(LocationManager.NETWORK_PROVIDER)== true);
              if(updateType == ULP_PHONE_CONTEXT_UPDATE_TYPE_SINGLE) {
                  currentNetworkProvSetting =
                      (Settings.Secure.isLocationProviderEnabled(resolver, LocationManager.NETWORK_PROVIDER ) &&
                       networkLocProvAvailable);
                  wasNetworkProviderSettingAvailable = true;
              }else
              {
                  if(settingsValues.containsKey("networkProvSetting"))
                  {
                      wasNetworkProviderSettingAvailable = true;
                      currentNetworkProvSetting = (settingsValues.getBoolean("networkProvSetting") &&
                                                   networkLocProvAvailable);
                  }
              }
          }

          //Update WiFi Setting
          if( (mRequestContextType & ULP_PHONE_CONTEXT_WIFI_SETTING)
               == ULP_PHONE_CONTEXT_WIFI_SETTING )
          {
              if(updateType == ULP_PHONE_CONTEXT_UPDATE_TYPE_SINGLE) {
                  //If WiFi is enabled while Airplane mode is on then its value is set to 2
                  currentWifiSetting = ((Settings.Global.getInt(resolver,Settings.Global.WIFI_ON) == 1) ||
                                        (Settings.Global.getInt(resolver,Settings.Global.WIFI_ON) == 2));
                  wasWifiSettingAvailable = true;
              }else
              {
                  if(settingsValues.containsKey("wifiSetting"))
                  {
                      wasWifiSettingAvailable = true;
                      currentWifiSetting = settingsValues.getBoolean("wifiSetting");
                  }
              }
          }

          //Update Enhanced Location Services Setting
          if( (mRequestContextType & ULP_PHONE_CONTEXT_ENH_LOCATION_SERVICES_SETTING)
               == ULP_PHONE_CONTEXT_ENH_LOCATION_SERVICES_SETTING )
          {
              if(updateType == ULP_PHONE_CONTEXT_UPDATE_TYPE_SINGLE) {
                  String currentEnhLocationServicesSettingString =
                      Settings.Secure.getString(resolver,ENH_LOCATION_SERVICES_ENABLED);

                  if(currentEnhLocationServicesSettingString != null) {
                      currentEnhLocationServicesSetting = currentEnhLocationServicesSettingString.equals("1");
                      wasEnhLocationServicesSettingAvailable = true;
                  } else {
                      Log.e(TAG, "Got null pinter for call to "+
                                 "Settings.Secure.getString(resolver,ENH_LOCATION_SERVICES_ENABLED)");
                  }
              }else
              {
                  if(settingsValues.containsKey("enhLocationServicesSetting"))
                  {
                      wasEnhLocationServicesSettingAvailable = true;
                      currentEnhLocationServicesSetting = settingsValues.getBoolean("enhLocationServicesSetting");
                  }
              }
          }
          //Update PIP User setting Setting
          if( (mRequestContextType & ULP_PHONE_CONTEXT_PIP_USER_SETTING)
               == ULP_PHONE_CONTEXT_PIP_USER_SETTING)
          {
              if(updateType == ULP_PHONE_CONTEXT_UPDATE_TYPE_SINGLE) {

                  currentPipUserSetting = getPipUserSetting();
                  wasPipUserSetting = true;
              }else
              {
                  if(settingsValues.containsKey("pipUserSetting"))
                  {
                      wasPipUserSetting = true;
                      currentPipUserSetting = settingsValues.getBoolean("pipUserSetting");
                  }
              }
          }
      } catch (Exception e) {
        Log.e(TAG, "Exception in handleNativePhoneContextUpdate:", e);
      }

      // Start with the requested mask
      currentContextType = mRequestContextType;

      // If setting was not available then don't send it. Otherwise
      // check for changes and store accordingly
      if(!wasGpsSettingAvailable) {
          currentContextType &= (~ULP_PHONE_CONTEXT_GPS_SETTING);
      } else
      {
          if(updateType == ULP_PHONE_CONTEXT_UPDATE_TYPE_ONCHANGE &&
                    currentGpsSetting == mGpsSetting) {
              currentContextType &= (~ULP_PHONE_CONTEXT_GPS_SETTING);
          }
          mGpsSetting = currentGpsSetting;
      }

      if(!wasAgpsSettingAvailable) {
          currentContextType &= (~ULP_PHONE_CONTEXT_AGPS_SETTING);
      } else
      {
          if(updateType == ULP_PHONE_CONTEXT_UPDATE_TYPE_ONCHANGE &&
                    currentAgpsSetting == mAgpsSetting) {
              currentContextType &= (~ULP_PHONE_CONTEXT_AGPS_SETTING);
          }
          mAgpsSetting = currentAgpsSetting;
      }

      if(!wasNetworkProviderSettingAvailable) {
          currentContextType &= (~ULP_PHONE_CONTEXT_NETWORK_POSITION_SETTING);
      } else
      {
          if(updateType == ULP_PHONE_CONTEXT_UPDATE_TYPE_ONCHANGE &&
                    currentNetworkProvSetting == mNetworkProvSetting) {
              currentContextType &= (~ULP_PHONE_CONTEXT_NETWORK_POSITION_SETTING);
          }
          mNetworkProvSetting = currentNetworkProvSetting;
      }
      if(!wasWifiSettingAvailable) {
          currentContextType &= (~ULP_PHONE_CONTEXT_WIFI_SETTING);
      } else
      {
          if(updateType == ULP_PHONE_CONTEXT_UPDATE_TYPE_ONCHANGE &&
                    currentWifiSetting == mWifiSetting) {
              currentContextType &= (~ULP_PHONE_CONTEXT_WIFI_SETTING);
          }
          mWifiSetting = currentWifiSetting;
      }
      if(!wasEnhLocationServicesSettingAvailable) {
          currentContextType &= (~ULP_PHONE_CONTEXT_ENH_LOCATION_SERVICES_SETTING);
      } else
      {
          if(updateType == ULP_PHONE_CONTEXT_UPDATE_TYPE_ONCHANGE &&
                    currentEnhLocationServicesSetting == mEnhServicesSetting ) {
              currentContextType &= (~ULP_PHONE_CONTEXT_ENH_LOCATION_SERVICES_SETTING);
          }
          mEnhServicesSetting = currentEnhLocationServicesSetting;
      }

      if(!wasPipUserSetting) {
          currentContextType &= (~ULP_PHONE_CONTEXT_PIP_USER_SETTING);
      } else
      {
          if(updateType == ULP_PHONE_CONTEXT_UPDATE_TYPE_ONCHANGE &&
                    currentPipUserSetting == mPipUserSetting ) {
              currentContextType &= (~ULP_PHONE_CONTEXT_PIP_USER_SETTING);
          }
          mPipUserSetting = currentPipUserSetting;
      }

      //Since we dont send Battery updates anymore through this pipe
      currentContextType &= (~ULP_PHONE_CONTEXT_BATTERY_CHARGING_STATE);

      native_ue_update_settings(currentContextType, currentGpsSetting, currentAgpsSetting,
                             currentNetworkProvSetting, currentWifiSetting,
                             currentBatteryCharging, currentEnhLocationServicesSetting,
                             currentPipUserSetting);
      //Also update ZPP logic
      native_ue_system_update(ULP_LOC_PHONE_CONTEXT_UPDATE);

      if(VERBOSE_DBG) {
          Log.v(TAG, "After calling native_ue_update_settings. currentContextType: " +
                currentContextType+" sGpsSetting: "+currentGpsSetting + "currentAgpsSetting: "+
                currentAgpsSetting+" currentNetworkProvSetting: " + currentNetworkProvSetting +
                 "currentWifiSetting: " + currentWifiSetting + " currentBatteryCharging: " +
                currentBatteryCharging + " currentEnhLocationServicesSetting: "+currentEnhLocationServicesSetting+
                " currentPipUserSetting: "+currentPipUserSetting);
      }
    }

    private boolean updateSecureSettings(boolean gpsSetting,boolean networkProvSetting,
                                      boolean enhLocationServicesSetting){
        if(VERBOSE_DBG) {

            Log.v(TAG, "updateSettings invoked and setting values. Gps:"+
                                    gpsSetting +" GNP:"+ networkProvSetting+"enhLocationServicesSetting: "+
                                    enhLocationServicesSetting);
        }
        Bundle contextBundle = new Bundle();
        contextBundle.putBoolean("gpsSetting", gpsSetting);
        contextBundle.putBoolean("networkProvSetting", networkProvSetting);
        contextBundle.putBoolean("enhLocationServicesSetting", enhLocationServicesSetting);
        sendMessage(UPDATE_NATIVE_PHONE_CONTEXT_SETTINGS, ULP_PHONE_CONTEXT_UPDATE_TYPE_ONCHANGE,
                    0,contextBundle);
        return true;
    }

    private final class SecureSettingsObserver implements Observer {
            public void update(Observable o, Object arg) {
                if (VERBOSE_DBG) Log.v(TAG,  "SecureSettingsObserver.update invoked ");
                boolean enhLocationServicesSetting = false, gpsSetting = false, networkProvSetting = false;
                String enhLocationServicesSettingString = null;
                //Will read the Settings values & determine if anything changed there
                Map<String, ContentValues> kvs = ((ContentQueryMap)o).getRows();
                if (null != kvs && !kvs.isEmpty()) {
                    if (kvs.containsKey(Settings.Secure.LOCATION_PROVIDERS_ALLOWED) == true) {
                        if (VERBOSE_DBG) Log.v(TAG, "in Settings.Secure.LOCATION_PROVIDERS_ALLOWED - "
                        +kvs.get(Settings.Secure.LOCATION_PROVIDERS_ALLOWED).toString());
                        String providers = kvs.get(Settings.Secure.LOCATION_PROVIDERS_ALLOWED).toString();
                        gpsSetting = providers.contains("gps");
                        networkProvSetting = providers.contains("network");
                    }

                    if (kvs.containsKey(ENH_LOCATION_SERVICES_ENABLED) == true) {
                       enhLocationServicesSettingString =
                            kvs.get(ENH_LOCATION_SERVICES_ENABLED).toString();
                       if(enhLocationServicesSettingString != null) {
                         enhLocationServicesSetting =
                             enhLocationServicesSettingString.contains("1");
                       } else {
                         Log.e(TAG, "Got null pinter for call to kvs.get(ENH_LOCATION_SERVICES_ENABLED)");
                       }
                    } else {
                      Log.e(TAG, "kvs.containsKey(ENH_LOCATION_SERVICES_ENABLED) returned false");
                    }
                  if(VERBOSE_DBG) {
                        Log.v(TAG,  "SettingsObserver.update invoked and setting values. Gps:"+
                               gpsSetting +" GNP:"+ networkProvSetting+ " enhLocationServicesSettingString: "+
                              enhLocationServicesSettingString);
                  }
                    updateSecureSettings(gpsSetting,networkProvSetting,enhLocationServicesSetting);
                 }
            }
     }

    private boolean updateGlobalSettings(boolean wifiSetting,boolean agpsSetting){
        if(VERBOSE_DBG) {

            Log.v(TAG, "updateGlobalSettings invoked. values. WiFi:"+ wifiSetting+
                                    " Agps:"+ agpsSetting);
        }
        Bundle contextBundle = new Bundle();
        contextBundle.putBoolean("wifiSetting", wifiSetting);
        contextBundle.putBoolean("agpsSetting", agpsSetting);
        sendMessage(UPDATE_NATIVE_PHONE_CONTEXT_SETTINGS, ULP_PHONE_CONTEXT_UPDATE_TYPE_ONCHANGE,
                    0,contextBundle);
        return true;
    }

    private final class GlobalSettingsObserver implements Observer {
            public void update(Observable o, Object arg) {
                if (VERBOSE_DBG) Log.v(TAG,  "GlobalSettingsObserver.update invoked ");
                boolean wifiSetting = false, agpsSetting = false;
                //Will read the Settings values & determine if anything changed there
                Map<String, ContentValues> kvs = ((ContentQueryMap)o).getRows();
                if (null != kvs && !kvs.isEmpty()) {

                    try {
                        //If WiFi is enabled while Airplane mode is on then its value is set to 2
                        wifiSetting = (kvs.get(Settings.Global.WIFI_ON).toString().contains("1")||
                                       kvs.get(Settings.Global.WIFI_ON).toString().contains("2")) ;
                    } catch (NullPointerException npe) {
                        if (VERBOSE_DBG) Log.v(TAG, "no WIFI_ON in the DB");
                    }

                    try {
                        agpsSetting = kvs.get(Settings.Global.ASSISTED_GPS_ENABLED).toString().contains("1");
                    } catch (NullPointerException npe) {
                        if (VERBOSE_DBG) Log.v(TAG, "no ASSISTED_GPS_ENABLED in the DB");
                    }

                  if(VERBOSE_DBG) {
                        Log.v(TAG,  "setting values. WiFi:"+ wifiSetting+" Agps:"+ agpsSetting);
                  }
                    updateGlobalSettings(wifiSetting,agpsSetting);
                 }
            }
     }

    private boolean getPipUserSetting(){
        //Stub for OEM impelementation to get true values for Precise Indoor Positioning
        //user setting
        boolean pipUserSetting = true;
        if(VERBOSE_DBG) {

           Log.v(TAG, "getPipUserSetting invoked. values. pipUserSetting:"+ pipUserSetting);
       }
       return pipUserSetting;
   }

    private boolean updatePipUserSetting(){
        boolean pipUserSetting = getPipUserSetting();
        if(VERBOSE_DBG) {

            Log.v(TAG, "updatePipUserSetting invoked. values. pipUserSetting:"+ pipUserSetting);
        }
        Bundle contextBundle = new Bundle();
        contextBundle.putBoolean("pipUserSetting", pipUserSetting);
        sendMessage(UPDATE_NATIVE_PHONE_CONTEXT_SETTINGS, ULP_PHONE_CONTEXT_UPDATE_TYPE_ONCHANGE,
                    0,contextBundle);
        return true;
    }

    private final class UlpEngineHandler extends Handler {
        public UlpEngineHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            int message = msg.what;
            if (VERBOSE_DBG) Log.v(TAG, "handleMessage what - " + message);
            switch (message) {
                case ENABLE:
                    if (msg.arg1 == 1) {
                        handleEnable();
                    } else {
                        handleDisable();
                    }
                    break;
                case SET_REQUEST:
                    handleSetRequest((ProviderRequestUnbundled)msg.obj);
                    break;
                case REQUEST_PHONE_CONTEXT_SETTINGS:
                    handleNativePhoneContextRequest(msg.arg1, msg.arg2);
                    break;
                case UPDATE_NATIVE_PHONE_CONTEXT_SETTINGS:
                    handleNativePhoneContextUpdate(msg.arg1,(Bundle)msg.obj);
                    break;
                case REQUEST_NETWORK_LOCATION:
                    handleNativeNetworkLocationRequest(msg.arg1, msg.arg2);
                    break;
                case UPDATE_NETWORK_LOCATION:
                    handleNetworkLocationUpdate((Location)msg.obj);
                    break;
                case INJECT_USER_LCI_SELECTION:
                    handleUserLciSelection((String)msg.obj);
                    break;
            }
            //TODO: Do we need to acquire and release Wakelocks
        }
    };

    private final class UlpLocRequest{
        private final int mQuality;
        private final long mInterval;
        private final long mFastestInterval;
        private int mNumUpdates = 0;
        private final float mSmallestDisplacement;

        public UlpLocRequest(LocationRequestUnbundled locRequest){
            mQuality = locRequest.getQuality();
            mInterval = locRequest.getInterval();
            mFastestInterval = locRequest.getFastestInterval();
            mSmallestDisplacement = locRequest.getSmallestDisplacement();

            try {
                Field locationRequestField = LocationRequestUnbundled.class.getDeclaredField("delegate");
                locationRequestField.setAccessible(true);
                LocationRequest locationRequest = (LocationRequest) locationRequestField.get(locRequest);
                mNumUpdates = locationRequest.getNumUpdates();
                if (VERBOSE_DBG) Log.d(TAG, "locationRequest " + locationRequest.toString() + " numUpdates:" +
                                       mNumUpdates );
            } catch (Exception e) {
                Log.w(TAG, "Exception ", e);
            }
        }

        public void updateCriteria(int action) {

                boolean singleShot = false;

                if (VERBOSE_DBG) Log.v(TAG, "Request Received with quality: "
                                       + mQuality + " & interval: " + mInterval);
                switch (mQuality) {
                    case LocationRequestUnbundled.ACCURACY_FINE:
                    case LocationRequestUnbundled.POWER_HIGH:

                        //This is a high accuracy request
                        native_ue_update_criteria(ULP_PROVIDER_SOURCE_HYBRID, action,
                                                      mInterval, mSmallestDisplacement,
                                                      (mNumUpdates == 1) , Criteria.ACCURACY_HIGH, Criteria.POWER_HIGH );
                        break;
                    case LocationRequestUnbundled.ACCURACY_BLOCK:
                    case LocationRequestUnbundled.ACCURACY_CITY:
                    case LocationRequestUnbundled.POWER_LOW:
                        //This is a low accuracy request
                        native_ue_update_criteria(ULP_PROVIDER_SOURCE_HYBRID, action,
                                                      mInterval, mSmallestDisplacement,
                                                      (mNumUpdates == 1), Criteria.ACCURACY_LOW  , Criteria.POWER_LOW );
                        break;
                    default:
                        Log.e(TAG, "Invalid quality value received");
                }

        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }

            UlpLocRequest locReq2 = (UlpLocRequest) obj;
            return((mFastestInterval ==  locReq2.mFastestInterval) &&
                   (mInterval ==  locReq2.mInterval) &&
                   (mQuality ==  locReq2.mQuality) &&
                   (mNumUpdates ==  locReq2.mNumUpdates) &&
                   (mSmallestDisplacement ==  locReq2.mSmallestDisplacement));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + mQuality;
            result = prime * result + mNumUpdates;
            result = prime * result + (int) (mFastestInterval ^ (mFastestInterval >>> 32));
            result = prime * result + (int) (mInterval ^ (mFastestInterval >>> 32));
            result = prime * result + Float.floatToIntBits(mSmallestDisplacement);
            return result;
        }

        @Override
        public String toString() {
            return String.format("UlpLocRequest Int: %d Qual: %d SS: %d", mInterval, mQuality, mNumUpdates);
        }
    };

    /**
     * @hide
     */
    public synchronized boolean isScreenOn() {
        return mScreenOn;
    }

    private synchronized void setScreenStatus(boolean val) {
        mScreenOn = val;
    }

    /**
     *  LCI candidates listener
     *
     * @hide
     */
    public static interface UlpLciListener {
        void onLciCandidatesReceivedNonBlock(String json);
    };

    /**
     * on call the api should immediately return current candidates via listener;
     * thereafter, on candidates change return updated candidates via listener
     *
     * @return true on success
     *
     * @hide
     */
    public boolean startListenLciCandidatesNonBlock(UlpLciListener listener) {
        if (VERBOSE_DBG) Log.d(TAG, "startListenLciCandidatesNonBlock");

        synchronized (this) {
            mLciListener = listener;
        }

        return true;
    }

    /**
     *
     * @return true on success
     *
     * @hide
     */
    public boolean stopListenLciCandidatesNonBlock() {
        if (VERBOSE_DBG) Log.d(TAG, "stopListenLciCandidatesNonBlock");

        synchronized (this) {
            mLciListener = null;
        }
        return true;
    }

    private void handleReportLciCandidates(String json) {
        if (VERBOSE_DBG) Log.d(TAG, "handleReportLciCandidates mLciListener=" + mLciListener +" json=" + json);

        synchronized (this) {
            if (mLciListener != null) {
                mLciListener.onLciCandidatesReceivedNonBlock(json);
            }
        }
    }

    /**
     *
     * @return true on success
     *
     * @hide
     */
    public boolean sendUserLciSelectionNonBlock(String lci) {
        if (VERBOSE_DBG) Log.d(TAG, "sendUserLciSelectionNonBlock(lci=" + lci +")");

        sendMessage(INJECT_USER_LCI_SELECTION, 0, 0, lci);
        return true;
    }

    private boolean handleUserLciSelection(String lci) {
        String rawCmd;

        if (lci == null) {
            rawCmd =  ULP_PIP_USER_LCI_DETERMINATION_MODE + "0";       //Auto mode
        } else {
            rawCmd =  ULP_PIP_USER_LCI_DETERMINATION_MODE + "1" + lci; //Manual Mode
        }
        native_ue_inject_raw_cmd(rawCmd, rawCmd.length());

        return true;
    }

    private native void native_ue_init();
    private static native void native_ue_class_init();
    private native boolean native_ue_update_criteria(int source, int action, long minTime, float minDistance,
                                                  boolean singleShot, int horizontalAccuracy,
                                                  int powerRequirement);
    private native boolean native_ue_update_settings(int currentContextType, boolean currentGpsSetting, boolean currentAgpsSetting,
                           boolean currentNetworkProvSetting, boolean currentWifiSetting, boolean currentBatteryCharging,
                           boolean currentEnhLocationServicesSetting, boolean currentPipUserSetting);
    private native void native_ue_send_network_location(double latitude, double longitude, float accuracy, long Utctime);

    private native boolean native_ue_inject_raw_cmd(String json, int length);

    private native boolean native_ue_start();
    private native boolean native_ue_stop();
    // Special Test Command Path
    private native boolean native_ue_system_update(int systemEvent);
    static {
        System.loadLibrary("locationservice");
        native_ue_class_init();
    }
}
