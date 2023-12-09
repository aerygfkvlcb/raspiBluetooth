package com.example.raspibluetooth;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class BluetoothService extends Service {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 블루투스 장치 연결
        String deviceAddress = "00:00:00:00:00:00"; // 라즈베리 파이의 Bluetooth 주소로 변경
        connectToDevice(deviceAddress);
        return START_STICKY;
    }

    private void connectToDevice(String deviceAddress) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();

            inputStream = bluetoothSocket.getInputStream();

            beginListenForData();
        } catch (IOException e) {
            // 연결 실패
            e.printStackTrace();
        }
    }

    private void beginListenForData() {
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = inputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == '\n') {
                                    // 여기에서 수신된 데이터 처리
                                    String data = new String(readBuffer, 0, readBufferPosition);
                                    readBufferPosition = 0;

                                    // 데이터를 브로드캐스트로 전송
                                    sendBroadcastMessage(data);
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }

                workerThread.start();
            }
        });
    }

    private void sendBroadcastMessage(String data) {
                Intent intent = new Intent("BluetoothDataReceived");
                intent.putExtra("data", data);
                sendBroadcast(intent);
            }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            stopWorker = true;
            inputStream.close();
            bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}