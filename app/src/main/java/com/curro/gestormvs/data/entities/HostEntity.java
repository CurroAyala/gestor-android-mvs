package com.curro.gestormvs.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "hosts")
public class HostEntity {

    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    @NonNull
    @ColumnInfo(name = "user")
    public String user;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "ip")
    public String ip;

    @NonNull
    @ColumnInfo(name = "port")
    public Integer port;

    public HostEntity(@NonNull String name, @NonNull String user, @NonNull String ip, @NonNull Integer port) {
        this.name = name;
        this.user = user;
        this.ip = ip;
        this.port = port;
    }

}
