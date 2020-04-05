package com.warmme.smshandler;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * 短信发送者
 */
public class SmsHandler {

    private static final String TAG = "SmsHandler";

    /**
     * 异步发送短信内容到服务器
     *
     * @return
     * @throws IOException
     */
    public static String sendSmsAsy(final String body){
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendSms(body);
            }
        }).start();
        return "succeed";
    }

    /**
     * 发送短信内容到服务器
     *
     * @return
     * @throws IOException
     */
    public static String sendSms(String body)  {
        Log.d(TAG, "sendSms: 进来了");
        URL url = null;
        InputStream stream = null;
        HttpURLConnection connection = null;
        String result = null;
        try {
            url = new URL("http", "120.78.93.52",80, "sms/sms");
            connection = (HttpURLConnection ) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            // 设置请求方式（GET/POST）
            connection.setRequestMethod("POST");
            //请求的内容为表单类型数据
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(3000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
            connection.setConnectTimeout(3000);
            // Open communications link (network traffic occurs here).
            connection.connect();
            OutputStream outStrm = connection.getOutputStream();
            ObjectOutputStream objOutputStrm = new ObjectOutputStream(outStrm);
            objOutputStrm.writeObject(new String(smsJson(body)));
            objOutputStrm.flush();
            objOutputStrm.close();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();

            if (stream != null) {
                // Converts Stream to String with max length of 500.
//                result = readStream(stream, 500);
            }
        }catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    public static String smsJson(String body){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("msg",body);
            jsonObject.put("receiveTime",System.currentTimeMillis());
            jsonObject.put("senderPhone","10068855");
            jsonObject.put("receiverPhone","18575582923");
            jsonObject.put("storTime",System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "smsJson: "+e.getMessage());
        }
        String b = jsonObject.toString();
        if (b.contains("{")){
            b = b.substring(b.indexOf("{"),b.length());
        }
        return jsonObject.toString();
    }
}
