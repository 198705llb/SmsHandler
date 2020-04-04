package com.warmme.smshandler;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UploadWorker extends Worker {
    private static String TAG = "UploadWorker";

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        new Thread(new Runnable() {
            @Override
            public void run() {
               while (true){
                   try {
                       Log.d(TAG, "doWork: ===>succeed!"+new Date());
                       Thread.sleep(1000*10);
                   } catch (InterruptedException e) {
                       Log.d(TAG, "doWork: ===>succeed!"+new Date()+"  "+e.getMessage().toString());
                   }
               }
            }
        }).start();
        return Result.success();
    }
}
