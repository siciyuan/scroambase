package com.scroam.db.data;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * 用户数据类
 */
public class UserData {
    public int id;
    public UUID uuid;
    public String username;
    public double balance;
    public int rtpFreeCount;
    public int rtpMaxFreeCount;
    public double rtpPrice;
    public int homesLimit;
    public Timestamp lastLogin;
    public Timestamp firstJoin;
    public long playTime;
    public String stats;
    public String settings;
    public String extraData;

    public UserData() {
        this.balance = 0;
        this.rtpFreeCount = 3;
        this.rtpMaxFreeCount = 3;
        this.rtpPrice = 100;
        this.homesLimit = 3;
        this.playTime = 0;
        this.stats = "{}";
        this.settings = "{}";
        this.extraData = "{}";
    }

    public UserData(UUID uuid, String username) {
        this();
        this.uuid = uuid;
        this.username = username;
    }
}
