package com.warmme.smshandler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SmsHandler.sendSmsAsy("on create");
        OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(UploadWorker.class)
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(uploadWorkRequest);
        Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                SmsHandler.sendSmsAsy("msgHandler:"+msg.toString());
            }
        };
        SmsObserver smsObserver = new SmsObserver(handler);
        getContentResolver().unregisterContentObserver(smsObserver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}





