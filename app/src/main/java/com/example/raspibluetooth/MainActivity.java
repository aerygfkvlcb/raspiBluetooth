package com.example.raspibluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver bluetoothDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("BluetoothDataReceived")) {
                String receivedData = intent.getStringExtra("data");

                // 데이터를 받아서 사용하는 함수 호출
                processData(receivedData);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // BroadcastReceiver 등록
        registerReceiver(bluetoothDataReceiver, new IntentFilter("BluetoothDataReceived"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // BroadcastReceiver 해제
        unregisterReceiver(bluetoothDataReceiver);
    }

    private void processData(String data) {
        // 데이터를 처리하는 함수
        Toast.makeText(this, "Received data: " + data, Toast.LENGTH_SHORT).show();
        // 원하는 작업 수행
        //sendSMS 함수 작성해서 여기서 실행하면 될듯
    }
}