package com.robgthai.xprinter.demo.xprintertest

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class DeviceReceiver(val deviceList: MutableList<String>) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: ""
        when(action) {
            BluetoothDevice.ACTION_FOUND -> deviceFound(intent!!)
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> discoveryFinished(intent!!)
        }
    }

    private fun deviceFound(intent: Intent) {
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

        if(device.bondState != BluetoothDevice.BOND_BONDED && device.fullName() !in deviceList) {
            deviceList.add(device.fullName())
            //TODO Update UI
        }
    }

    private fun discoveryFinished(intent: Intent) {
        Log.d("DeviceReceiver", "Finish Discovery")
    }

}

fun BluetoothDevice.fullName(): String {
    return "$name\n$address"
}