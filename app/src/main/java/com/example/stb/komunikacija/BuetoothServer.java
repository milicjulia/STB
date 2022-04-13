package com.example.stb.komunikacija;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.stb.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


class BluetoothServer {
    public static final String NAME = "Server";
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    protected static final String DEVICE_NAME = "Device name";
    protected static final String TOAST = "Toast";
    protected static final String TAG = "VNesic_SERVER_DEBUG ";

    private static int mState = 0;
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    BluetoothDevice lastConnectedDevice;
    AcceptThread mAcceptThread;
    ConnectThread mConnectThread;
    ConnectedThread mConnectedThread;
    BluetoothAdapter mBluetoothAdapter;
    Handler mHandler;

    BluetoothServer(Handler m, BluetoothAdapter b) {
        mBluetoothAdapter = b;
        mState = STATE_NONE;
        mHandler = m;
    }

    public synchronized void setState(int s) {
        Log.d(TAG, "Current state :" + s);
        mState = s;
    }

    BluetoothDevice getLastConnectedDevice() {
        return lastConnectedDevice;
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        Log.d(TAG, "start");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_LISTEN);
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);
        lastConnectedDevice = device;
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) {
        Log.d(TAG, "connected, Socket Type:");
        lastConnectedDevice = device;
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);
    }

    public synchronized void stop() {
        Log.d(TAG, "stop");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    public void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != 3) return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    public void connectionLost() {
        BluetoothServer.this.start();
    }

    public void connectionFailed() {
        BluetoothServer.this.start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "Socket Type: " + "BEGIN mAcceptThread" + this);
            setName("AcceptThread");

            BluetoothSocket socket = null;
            while (mState != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + "accept() failed", e);
                    break;
                } catch (NullPointerException e) {

                    Log.e(TAG, "Socket is null", e);
                    break;
                }
                if (socket != null) {
                    synchronized (this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: ");
        }
        public void cancel() {
            Log.d(TAG, "Socket Type" + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + "close() of server failed", e);
            } catch (NullPointerException e) {
                Log.e(TAG, "Socket is null", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "ConnectThread started");
            mBluetoothAdapter.cancelDiscovery();
            try {
                if (mmSocket != null)
                    mmSocket.connect();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connectionFailed();
                return;
            }
            synchronized (BluetoothServer.this) {
                mConnectThread = null;
            }
            connected(mmSocket, mmDevice);
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private ConnectionChecker cChecker;

        private class ConnectionChecker extends Thread {
            private final OutputStream mmOutStream;
            private ConnectionChecker(OutputStream mmOutStream) {
                this.mmOutStream = mmOutStream;
            }
            public void run() {
                while (true) {
                    try {
                        // Read from the InputStream
                        mmOutStream.write("c".getBytes());
                        sleep(5000);
                    } catch (IOException e) {
                        connectionLost();
                        BluetoothServer.this.start();
                        break;
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                if (socket != null) {
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                }
            } catch (IOException e) {
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.d(TAG, "Connected Thread");
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    Log.d(TAG, "PRIMIO PORUKU " + new String(String.valueOf(bytes)));
                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    connectionLost();
                    BluetoothServer.this.start();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, bytes)
                        .sendToTarget();
            } catch (IOException e) {
                mHandler.obtainMessage(MainActivity.MESSAGE_DISCONNECTED, -1, -1, "aa".getBytes()).sendToTarget();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}