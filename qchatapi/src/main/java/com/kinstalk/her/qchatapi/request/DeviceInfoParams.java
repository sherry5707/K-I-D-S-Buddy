package com.kinstalk.her.qchatapi.request;

import java.io.Serializable;

/**
 * Created by wangzhipeng on 2017/5/17.
 */

public class DeviceInfoParams implements Serializable {
    public String device;
    public String os_version;
    public String app_version;
    public String serial_number;
    public String build_number;
    public String din;
    public String tx_sdk_version;
    public String product_sn;
    public String mac_address;
//    public String location;
    public String audiohwtype;
    public String hwid;

    public DeviceInfoParams(String device, String os_version, String app_version, String serial_number, String build_number, String din, String tx_sdk_version, String product_sn, String mac_address, /*String location,*/ String audiohwtype, String hwid) {
        this.device = device;
        this.os_version = os_version;
        this.app_version = app_version;
        this.serial_number = serial_number;
        this.build_number = build_number;
        this.din = din;
        this.tx_sdk_version = tx_sdk_version;
        this.product_sn = product_sn;
        this.mac_address = mac_address;
//        this.location = location;
        this.audiohwtype = audiohwtype;
        this.hwid = hwid;
    }
}
