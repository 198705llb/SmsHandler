package com.warmme.smshandler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    IntentFilter filter;
    SmsReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        receiver = new SmsReceiver();
        registerReceiver(receiver, filter);//注册广播接收器
//        sendSmsAsy("on create");

        OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(UploadWorker.class)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiresCharging(true)
                .build();
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(UploadWorker.class, 15, TimeUnit.MINUTES).setConstraints(constraints).build();
        WorkManager.getInstance(getApplicationContext()).enqueue(uploadWorkRequest);
//        WorkManager.getInstance(getApplicationContext()).enqueue(periodicWorkRequest);
    }


    public class SmsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            StringBuilder content = new StringBuilder();//用于存储短信内容
            String sender = null;//存储短信发送方手机号
            Bundle bundle = intent.getExtras();//通过getExtras()方法获取短信内容
            String format = intent.getStringExtra("format");
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");//根据pdus关键字获取短信字节数组，数组内的每个元素都是一条短信
                for (Object object : pdus) {
                    SmsMessage message = SmsMessage.createFromPdu((byte[]) object, format);//将字节数组转化为Message对象
                    sender = message.getOriginatingAddress();//获取短信手机号
                    content.append(message.getMessageBody());//获取短信内容
                    Log.d(TAG, "onReceive: "+message.getMessageBody());
//                    sendSmsAsy(message.getMessageBody());
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);//解绑广播接收器

    }
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
        HttpsURLConnection connection = null;
        String result = null;
        try {

            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = { new MyX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(null, tm, new java.security.SecureRandom());
            sslContext.init(new KeyManager[0], new TrustManager[] { new MyX509TrustManager() }, new SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            url = new URL("https", "120.78.93.52", 8443, "sms");



            connection = (HttpsURLConnection ) url.openConnection();

            connection.setSSLSocketFactory(ssf);
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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
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


 class MyX509TrustManager implements X509TrustManager {

    private static final String TAG = "MyX509TrustManager";

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        if (null != chain) {
            for (int k = 0; k < chain.length; k++) {
                X509Certificate cer = chain[k];
                print(cer);
            }
        }
        Log.d(TAG, "check client trusted. authType=" + authType);

    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        if (null != chain) {
            for (int k = 0; k < chain.length; k++) {
                X509Certificate cer = chain[k];
                print(cer);
            }
        }
        Log.d(TAG, "check servlet trusted. authType=" + authType);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {

        Log.d(TAG, "get acceptedissuer");

        return null;
    }

    private void print(X509Certificate cer) {

        int version = cer.getVersion();
        String sinname = cer.getSigAlgName();
        String type = cer.getType();
        String algorname = cer.getPublicKey().getAlgorithm();
        BigInteger serialnum = cer.getSerialNumber();
        Principal principal = cer.getIssuerDN();
        String principalname = principal.getName();

        Log.d(TAG, "version=" + version + ", sinname=" + sinname
                + ", type=" + type + ", algorname=" + algorname
                + ", serialnum=" + serialnum + ", principalname="
                + principalname);
    }

}


class MyHostnameVerifier implements HostnameVerifier {

    private static final String TAG = "MyHostnameVerifier";

    public boolean verify(String hostname, SSLSession session) {
        Log.d(TAG,
                "hostname=" + hostname + ",PeerHost= "
                        + session.getPeerHost());
        return true;
    }

}


