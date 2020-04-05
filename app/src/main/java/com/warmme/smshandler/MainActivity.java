package com.warmme.smshandler;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SmsHandler2.sendSmsAsy(new SmsInfo("18575582923","onCreate:=>【招商金科】"));
//        SmsHandler.sendSmsAsy("on create");
//        Intent intent = new Intent(this, SmsIntentService.class);
//        startService(intent);

     /*   OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(UploadWorker.class)
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

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasReadSmsPermission = checkSelfPermission(Manifest.permission.READ_SMS);
            if (hasReadSmsPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }*/

//        getContentResolver().unregisterContentObserver(smsObserver);

      /*  Uri uri = Uri.parse("content://sms/inbox");
        grantUriPermission("com.warmme.smshandler",uri, Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        SmsHandler.sendSmsAsy("duqushujuku ==> "+getSMS(getContentResolver()));
*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    public  String getSMS( ContentResolver cr) {
        final String SMS_URI_INBOX = "content://sms/inbox";

        String address = "18575582923";
        Uri uri = Uri.parse(SMS_URI_INBOX);

        String[] projection = new String[]{"_id", "address", "person", "body", "date", "type",};
//        Cursor cursor = getContentResolver().query(uri, projection, null, null, "date desc");
        Cursor cur = cr.query(uri, projection, "read = ?", new String[]{"0"}, "date desc");
        StringBuilder smsBuilder = new StringBuilder();


        if(cur.moveToFirst()){
            int index_Address = cur.getColumnIndex("address");
            int index_Person = cur.getColumnIndex("person");
            int index_Body = cur.getColumnIndex("body");
            int index_Date = cur.getColumnIndex("date");
            int index_Type = cur.getColumnIndex("type");

            do {
                String strAddress = cur.getString(index_Address);
                int intPerson = cur.getInt(index_Person);
                String strbody = cur.getString(index_Body);
                long longDate = cur.getLong(index_Date);
                int intType = cur.getInt(index_Type);

                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "yyyy-MM-dd hh:mm:ss");
                Date d = new Date(longDate);
                String strDate = dateFormat.format(d);

                String strType = "";
                if (intType == 1) {
                    strType = "接收";
                } else if (intType == 2) {
                    strType = "发送";
                } else if (intType == 3) {
                    strType = "草稿";
                } else if (intType == 4) {
                    strType = "发件箱";
                } else if (intType == 5) {
                    strType = "发送失败";
                } else if (intType == 6) {
                    strType = "待发送列表";
                } else if (intType == 0) {
                    strType = "所以短信";
                } else {
                    strType = "null";
                }

                if(strAddress.equals(address)){
                    smsBuilder.append("[ ");
                    smsBuilder.append(strAddress + ", ");
                    smsBuilder.append(intPerson + ", ");
                    smsBuilder.append(strbody + ", ");
                    smsBuilder.append(strDate + ", ");
                    smsBuilder.append(strType);
                    smsBuilder.append(" ]\n\n");
                }

            } while (cur.moveToNext());

            if (!cur.isClosed()) {
                cur.close();
                cur = null;
            }
        }else{
            smsBuilder.append("no result!");
        }


        smsBuilder.append("getSmsInPhone has executed!");

        return smsBuilder.toString();
    }
}





