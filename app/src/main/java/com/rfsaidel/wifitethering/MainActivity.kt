package com.rfsaidel.wifitethering

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rfsaidel.wifitethering.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private var settingsPermission : Boolean = false
    private var locationPermission : Boolean = false
    private val MY_PERMISSIONS_MANAGE_WRITE_SETTINGS = 100
    private val MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.enablePermissions.setOnClickListener{
            Log.i(TAG,"Permission:\n   Settings: "+settingsPermission+"\n   Location: "+locationPermission)
            settingsPermission()
            locationPermission()
        }

        binding.enableTethering.setOnClickListener{
            Log.i(TAG,"Enable Tethering")
        }

        binding.disableTethering.setOnClickListener{
            Log.i(TAG,"Disable Tethering")
        }

        binding.selectBtDevice.setOnClickListener{
            Log.i(TAG,"Select BT Device")
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check which request we're responding to
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
}


