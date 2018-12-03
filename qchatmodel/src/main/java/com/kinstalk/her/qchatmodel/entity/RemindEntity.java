package com.kinstalk.her.qchatmodel.entity;

import java.io.Serializable;

/**
 * Created by Tracy on 2018/4/20.
 *
 * type 为0 表示服务器下发的普通提醒，1待定，type 为2 表示小微提醒，这个可删除
 * type 3 早起 type 4 早睡 type  5 作業
 * status 星星数
 */

public class RemindEntity implements Serializable {
        public String openid;
        public String remindid;
        public String content;
        public Long assign_time;
        public Long remind_time;
        public String repeat_time;
        public int type;
        public int status;

        public RemindEntity(String content) {
            this.content = content;
        }

        public RemindEntity() {
            this.openid = "";
            this.remindid = "";
            this.content = "";
            this.assign_time = 0L;
            this.remind_time = 0L;
            this.repeat_time = "";
            this.type = 1;
            this.status = 0;
        }

        public String getOpenid() {
            return openid;
        }

        public String getRemindid() {
            return remindid;
        }

        public String getContent() {
            return content;
        }

        public Long getAssign_time() {
            return assign_time;
        }
        public String getRepeat_time() {return repeat_time;}

        public Long getRemind_time() {
            return remind_time;
        }

        public int getType() { return type;}

        public int getStatus() {
            return status;
        }

        public void setOpenid(String openid) {
            this.openid = openid;
        }

        public void setRemindid(String remindid) {
            this.remindid = remindid;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setAssign_time(Long assign_time) {
            this.assign_time = assign_time;
        }

        public void setRemind_time(Long remind_time) {
            this.remind_time = remind_time;
        }

        public void setRepeat_time(String repeat_time) { this.repeat_time = repeat_time;}

        public void setType(int type) { this.type = type;}

        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "RemindEntity{" +
                    "openid='" + openid + '\'' +
                    ", remindid='" + remindid + '\'' +
                    ", content='" + content + '\'' +
                    ", add_time=" + assign_time +
                    ", remind_time=" + remind_time +
                    ", repeat_time=" + repeat_time +
                    ", type=" + type +
                    ", status=" + status +
                    '}';
        }
}
