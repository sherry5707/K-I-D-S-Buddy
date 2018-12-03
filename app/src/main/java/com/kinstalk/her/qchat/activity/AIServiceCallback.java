package com.kinstalk.her.qchat.activity;

import com.google.gson.JsonObject;

import org.json.JSONObject;

public interface AIServiceCallback {
    void onJsonResult(JSONObject object);
}