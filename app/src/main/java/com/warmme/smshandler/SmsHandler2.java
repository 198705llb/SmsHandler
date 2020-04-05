package com.warmme.smshandler;

import android.util.Log;

import com.warmme.smshandler.utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SmsHandler2 {

    private static final String TAG = "SmsHandler2";

    /**
     * 异步发送短信内容到服务器
     *
     * @return
     * @throws IOException
     */
    public static String sendSmsAsy(final SmsInfo smsInfo) {
        Log.d(TAG, "sendSmsAsy: coming!");
        if (!filter(smsInfo)){
            Log.d(TAG, "sendSmsAsy: not right sms =>"+toJson(smsInfo));
            return "faild!";
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendSms(smsInfo);
            }
        }).start();
        return "succeed";
    }

    /**
     * 过滤出目标短信
     *
     * @param smsInfo
     * @return
     */
    private static boolean filter(SmsInfo smsInfo) {
               if (smsInfo==null){
                   return false;
               }
               if (smsInfo.getMsg()==null || smsInfo.getMsg().equals("")){
                   return false;
               }
               if (!smsInfo.getMsg().contains("【招商金科】")){
                   return false;
               }
               return true;
    }

    /**
     * 发送到服务器
     *
     * @param smsInfo
     * @return
     */
    private static String sendSms(final SmsInfo smsInfo) {
        Log.d(TAG, "sendSmsAsy: right sms =>"+toJson(smsInfo));
        return HttpUtils.post(toJson(smsInfo));
    }

    /**
     * json转换
     *
     * @param smsInfo
     * @return
     */
    private static String toJson(SmsInfo smsInfo) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("msg", smsInfo.getMsg());
            jsonObject.put("receiveTime", System.currentTimeMillis());
            jsonObject.put("senderPhone", smsInfo.getSenderPhone());
            jsonObject.put("receiverPhone", smsInfo.getReceiverPhone());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "smsJson: " + e.getMessage());
        }
        return jsonObject.toString();
    }
}
