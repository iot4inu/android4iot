package com.sdtlab.kkim.irterm3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService {
    // Debugging
    private static final String TAG = "BluetoothService";
    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITEN = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothService Handler
    public static final String ARG_DEVICE_NAME = "device_name";
    public static final String ARG_TOAST = "toast";
    // Service States
    private int mState;
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    // RFCOMM Protocol
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Constructor Parameters
    private BluetoothAdapter mBtAdapter;
    private Handler mHandler;
    // Threads
    private ConnectThread mConnectThread;
    private WatchThread mWatchThread;

    public BluetoothService(Handler handler) {
        mHandler = handler;
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    private synchronized void setState(int state) {
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }
    public synchronized int getState() {
        return mState;
    }
    public synchronized void start() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mWatchThread != null) {
            mWatchThread.cancel();
            mWatchThread = null;
        }
    }
    public synchronized void connect(BluetoothDevice device) {
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        if (mWatchThread != null) {
            mWatchThread.cancel();
            mWatchThread = null;
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
    public synchronized void watch(
            BluetoothSocket socket, BluetoothDevice device) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mWatchThread != null) {
            mWatchThread.cancel();
            mWatchThread = null;
        }
        mWatchThread = new WatchThread(socket);
        mWatchThread.start();
        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(ARG_DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);
    }
    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mWatchThread != null) {
            mWatchThread.cancel();
            mWatchThread = null;
        }
        setState(STATE_NONE);
    }
    public void write(byte[] out) {
        WatchThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mWatchThread;
        }
        r.write(out);
    }
    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_LISTEN);
    }
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_LISTEN);
    }
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket create() failed", e);
            }
            mmSocket = tmp;
        }
        public void run() {
            setName("ConnectThread");
            mBtAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                BluetoothService.this.start();
                return;
            }
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            watch(mmSocket, mmDevice);
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of socket failed", e);
            }
        }
    }
    private class WatchThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public WatchThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            int i, first, offset = 0;
            while (true) {
                try {
                    Log.d(TAG, "offset = " + offset);
                    bytes = mmInStream.read(buffer, offset, 1024 - offset);
                    bytes += offset;

                    for(i = 0, first = 0; i < bytes; i++) {
                        if ((buffer[i] == '\n') && (i + 1 < bytes)){
                            // Send the obtained bytes to the UI Activity
                            mHandler.obtainMessage(
                                     MESSAGE_READ, first, i - first, buffer).
                                    sendToTarget();
                            first = i + 2; // skip '\n' and '\r'.
                        }
                    }
                    if((first == 0) && (bytes == 1024)) {
                        mHandler.obtainMessage(
                                 MESSAGE_READ, 0, 1024, buffer).sendToTarget();
                        offset = 0;
                    } else {
                        for(i = first; i < bytes; i++)
                            buffer[i - first] = buffer[i];
                        offset = bytes - first;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(
                        MESSAGE_WRITEN, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
