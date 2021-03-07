package com.rfsaidel.wifitethering

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.renderscript.ScriptGroup
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rfsaidel.wifitethering.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var mContext : Context
    lateinit var binding : ActivityMainBinding
    private var settingsPermission : Boolean = false
    private var locationPermission : Boolean = false
    private var bluetoothPermission : Boolean = false
    private val MY_PERMISSIONS_MANAGE_WRITE_SETTINGS = 100
    private val MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 200
    private val MY_PERMISSIONS_BLUETOOTH = 300
    var btAdapter: BluetoothAdapter? = null

    @RequiresApi(api = Build.VERSION_CODES.M)
    var mMyOreoWifiManager: MyOreoWifiManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this

        binding.enablePermissions.setOnClickListener{
            Log.i(TAG,"Permission >> Settings: "+settingsPermission+" Location: "+locationPermission)
            settingsPermission()
            locationPermission()
        }

        binding.enableTethering.setOnClickListener{
            Log.i(TAG,"Enable Tethering")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hotspotOreo(true)
            }
        }

        binding.disableTethering.setOnClickListener{
            Log.i(TAG,"Disable Tethering")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hotspotOreo(false)
            }
        }

        binding.selectBtDevice.setOnClickListener{
            Log.i(TAG,"Select BT Device")
            CheckBluetoothState()
        }

        btAdapter = BluetoothAdapter.getDefaultAdapter()
        setupBluetoothReceiver()
    }

    private fun settingsPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:" + this.getPackageName())
                )
                this.startActivityForResult(
                    intent, MY_PERMISSIONS_MANAGE_WRITE_SETTINGS
                )
            }else{
                settingsPermission = true
            }
        }
    }

    private fun locationPermission(){
        locationPermission = true
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
            locationPermission = false
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION)
                // MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private fun bluetoothPermission(){

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check which request we're responding to
        Log.i(TAG, "onActivityResult: "+requestCode)
        if (requestCode == MY_PERMISSIONS_MANAGE_WRITE_SETTINGS) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED) {
                settingsPermission = true
            } else {
                settingsPermission()
            }
        }
        if (!locationPermission) locationPermission()

        if (locationPermission && settingsPermission){
            Log.i(TAG,"All Permissions Granted")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                                    Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                    }
                    locationPermission = true
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                if(!settingsPermission) settingsPermission()

                if (locationPermission && settingsPermission){
                    Log.i(TAG,"All Permissions Granted")
                }
                return
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun hotspotOreo(turnOn: Boolean) {
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

    private fun CheckBluetoothState() {
        // Checks for the Bluetooth support and then makes sure it is turned on
        // If it isn't turned on, request to turn it on
        // List paired devices
        Log.i(TAG,"CheckBluetoothState")
        if (btAdapter == null) {
            Log.i(TAG,"Bluetooth NOT supported. Aborting.")
            return
        } else {
            if (btAdapter!!.isEnabled()) {
                Log.i(TAG,"Bluetooth is enabled...")

                // Listing paired devices
                Log.i(TAG,"Paired Devices are:")
                val devices: Set<BluetoothDevice> = btAdapter!!.getBondedDevices()
                for (device in devices) {
                    Log.i(TAG,"""Device: ${device.name}, $device""")
                }
            } else {
                //Prompt user to turn on Bluetooth
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, MY_PERMISSIONS_BLUETOOTH)
            }
        }
    }

    private fun setupBluetoothReceiver() {
        val btReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handleBtEvent(intent)
            }
        }
        val eventFilter = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
        eventFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        mContext.registerReceiver(btReceiver, eventFilter)
    }

    private fun handleBtEvent(intent: Intent) {
        val action = intent.action
        val deviceName = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)?.name
        Log.d(TAG, "!!!! action received: $action")

        if (BluetoothDevice.ACTION_ACL_CONNECTED == action) {
            Log.d(TAG, "!!!! BT CONNECTED: $deviceName")
            binding.enableTethering.callOnClick()
        }

        if (BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
            Log.d(TAG, "!!!! BT DISCONNECTED: $deviceName")
            binding.disableTethering.callOnClick()
        }
    }
}


