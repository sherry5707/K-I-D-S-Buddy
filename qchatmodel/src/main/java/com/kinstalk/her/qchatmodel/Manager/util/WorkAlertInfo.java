package com.kinstalk.her.qchatmodel.Manager.util;

/**
 * Created by lenovo on 2018/5/24.
 */

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.kinstalk.her.qchatmodel.entity.HomeWorkEntity;

public class WorkAlertInfo implements Parcelable {

    // type:0 刪除  type1：modify or add
    // id is unique
    // type 0 && workString is null  delete homework id;
    // type 1 && workString is null  add/modify PIC homework id;
    // type 1 && workString is not null add/modify TEXT homework id;

    private String workString;
    private String workUrl;
    private String id;
    private int type;

    public WorkAlertInfo() {

    }

    public WorkAlertInfo (HomeWorkEntity homeWorkEntity) {
        workString = homeWorkEntity.getContent();
        workUrl = homeWorkEntity.getUrl();
        if(!TextUtils.isEmpty(workUrl))
            workString = "";
        id = homeWorkEntity.getHomeworkid();
        type = 1;
    }

    public String getId() {return id;}

    public int getType() {return type;}

    public String getWorkString() {
        return workString;
    }

    public void setWorkString(String workString) {
        this.workString = workString;
    }

    public String getWorkUrl() {
        return workUrl;
    }

    public void setWorkUrl(String workUrl) {
        this.workUrl = workUrl;
    }

    public void setType(int type) { this.type = type;}

    public void setId(String id) { this.id = id;}

    protected WorkAlertInfo(Parcel in) {
        workString = in.readString();
        workUrl = in.readString();
        id = in.readString();
        type = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(workString);
        dest.writeString(workUrl);
        dest.writeString(id);
        dest.writeInt(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WorkAlertInfo> CREATOR = new Creator<WorkAlertInfo>() {
        @Override
        public WorkAlertInfo createFromParcel(Parcel in) {
            return new WorkAlertInfo(in);
        }

        @Override
        public WorkAlertInfo[] newArray(int size) {
            return new WorkAlertInfo[size];
        }
    };

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("WorkAlertInfo{");
        sb.append("workString='").append(workString).append('\'');
        sb.append(", workUrl='").append(workUrl).append('\'');
        sb.append(", id=").append(id);
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
