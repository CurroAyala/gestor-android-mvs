package com.curro.gestormvs.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "hosts")
public class HostEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id;

    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    @NonNull
    @ColumnInfo(name = "user")
    public String user;

    @NonNull
    @ColumnInfo(name = "ip")
    public String ip;

    @NonNull
    @ColumnInfo(name = "port")
    public Integer port;

    @NonNull
    @ColumnInfo(name = "password")
    public String encryptedPassword;


    public HostEntity(long id, @NonNull String name, @NonNull String user, @NonNull String ip, @NonNull Integer port, @NonNull String encryptedPassword) {
        this.id = id;
        this.name = name;
        this.user = user;
        this.ip = ip;
        this.port = port;
        this.encryptedPassword = encryptedPassword;
    }

}
