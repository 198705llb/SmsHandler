package com.warmme.smshandler;

/**
 * 短信对象
 */
public class SmsInfo {
    /**
     * 短信发送者
     **/
    private String senderPhone;

    /**
     * 短信接收者
     **/
    private String receiverPhone;

    /**
     * 短信接收时间
     */
    private Long receiveTime;

    /**
     * 短信内容
     **/
    private String msg;

    public SmsInfo(String senderPhone, String receiverPhone, String msg) {
        this.senderPhone = senderPhone;
        this.receiverPhone = receiverPhone;
        this.msg = msg;
        this.receiveTime = System.currentTimeMillis();
    }

    public SmsInfo(String receiverPhone, String msg) {
        this.receiverPhone = receiverPhone;
        this.msg = msg;
        this.receiveTime = System.currentTimeMillis();
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public Long getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Long receiveTime) {
        this.receiveTime = receiveTime;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
