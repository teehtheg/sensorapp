package com.teeh.klimasensor.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import java.io.IOException

/**
 * This thread runs while attempting to make an outgoing connection
 * with a device. It runs straight through; the connection either
 * succeeds or fails.
 */
class ConnectThread(private val service: BluetoothService, private val device: BluetoothDevice) : Thread() {

    private lateinit var mmSocket: BluetoothSocket
    private val mmDevice: BluetoothDevice
    private val mSocketType: String

    init {
        mmDevice = device
        var tmp: BluetoothSocket? = null
        mSocketType = "InSecure"

        try {
            tmp = device.createInsecureRfcommSocketToServiceRecord(service.uuid)

        } catch (e: IOException) {
            Log.e(BluetoothConstants.TAG, "Socket Type: " + mSocketType + "create() failed", e)
        }
        mmSocket = tmp!!
    }

    override fun run() {
        Log.i(BluetoothConstants.TAG, "BEGIN mConnectThread SocketType:" + mSocketType)
        name = "ConnectThread" + mSocketType

        // Always cancel discovery because it will slow down a connection
        service.adapter.cancelDiscovery()

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            mmSocket.connect()
        } catch (e: IOException) {
            // Close the socket
            Log.e(BluetoothConstants.TAG, "ConnectThread could not connect. Reason:" + e.localizedMessage)
            try {
                mmSocket.close()
            } catch (e2: IOException) {
                Log.e(BluetoothConstants.TAG, "unable to close() " + mSocketType +
                        " socket during connection failure", e2)
            }

            connectionFailed()
            return
        }
        //connectionSuccessful(mmDevice)

        // Start the connected thread
        service.connected(mmSocket, mmDevice, mSocketType)
    }

    fun cancel() {
        try {
            mmSocket.close()
        } catch (e: IOException) {
            Log.e(BluetoothConstants.TAG, "close() of connect $mSocketType socket failed", e)
        }

    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private fun connectionFailed() {
        // Send a failure message back to the Activity
        val msg = service.handler.obtainMessage(BluetoothConstants.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(BluetoothConstants.TOAST, "Unable to connect device")
        msg.data = bundle
        service.handler.sendMessage(msg)

        // Start the service over to restart listening mode
        service.start()
    }

    private fun connectionSuccessful(device: BluetoothDevice) {
        val msg = service.handler.obtainMessage(BluetoothConstants.MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString(BluetoothConstants.DEVICE_NAME, device.name)
        msg.data = bundle
        service.handler.sendMessage(msg)
    }

}