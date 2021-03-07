package com.rfsaidel.wifitethering

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

private val TAG = "TetheringOreo"

@RequiresApi(api = Build.VERSION_CODES.M)
var mMyOreoWifiManager: MyOreoWifiManager? = null

@RequiresApi(api = Build.VERSION_CODES.M)
public fun hotspotOreo(turnOn: Boolean, mContext: Context) {
    if (mMyOreoWifiManager == null) {
        mMyOreoWifiManager = MyOreoWifiManager(mContext)
    }
    if (turnOn) {

        //this dont work
        val callback: StartTetheringCallback = object : StartTetheringCallback() {
            override fun onTetheringStarted() {
                Log.i(TAG,"Tethering Started")
            }

            override fun onTetheringFailed() {
                Log.i(TAG,"Tethering Started FAILURE")
            }
        }
        mMyOreoWifiManager!!.startTethering(callback)
    } else {
        mMyOreoWifiManager!!.stopTethering()
        Log.i(TAG,"Tethering Stopped")
    }
}