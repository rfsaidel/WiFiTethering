package com.rfsaidel.wifitethering

import android.bluetooth.BluetoothDevice
import android.content.*
import android.os.Build
import android.util.Log

import androidx.annotation.RequiresApi

class BootCompletedReceiver : BroadcastReceiver() {

    private val PREFS_NAME = "prefWiFiTethering"
    private val PREFS_BT_DEVICE_NAME = "btDeviceName"
    var btDeviceName: String = ""
    var TAG = "BootCompletedReceiver"
    lateinit var mContext: Context

    override fun onReceive(context: Context, intent: Intent) {
        mContext = context
        val settings: SharedPreferences = mContext.applicationContext.getSharedPreferences(PREFS_NAME, 0)
        btDeviceName = settings.getString(PREFS_BT_DEVICE_NAME,"").toString()
        Log.i(TAG, "Boot Completed")
        setupBluetoothReceiver()
    }

    private fun setupBluetoothReceiver() {
        val btReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handleBtEvent(intent)
            }
        }
        val eventFilter = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
        eventFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        mContext.applicationContext.registerReceiver(btReceiver, eventFilter)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun handleBtEvent(intent: Intent) {
        val action = intent.action
        val deviceName = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)?.name

        if (BluetoothDevice.ACTION_ACL_CONNECTED == action) {
            if(btDeviceName.equals(deviceName)) {
                Log.i(TAG, "enableTethering")
                hotspotOreo(true, mContext.applicationContext)
            }
        }

        if (BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
            if(btDeviceName.equals(deviceName)) {
                Log.i(TAG, "disableTethering")
                hotspotOreo(false, mContext.applicationContext)
            }
        }
    }
}