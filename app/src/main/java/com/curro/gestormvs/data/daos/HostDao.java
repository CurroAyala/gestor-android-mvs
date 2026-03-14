package com.curro.gestormvs.data.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.curro.gestormvs.data.entities.HostEntity;

import java.util.List;

@Dao
public interface HostDao {

    @Insert
    void createHost(HostEntity host);

    @Update
    void updateHost(HostEntity host);

    @Delete
    void deleteHost(HostEntity host);

    @Query("SELECT * FROM hosts WHERE id = :id")
    HostEntity getHostById(long id);

    @Query("SELECT * FROM hosts WHERE ip = :ip")
    HostEntity getHostByIp(String ip);

    @Query("SELECT * FROM hosts")
    List<HostEntity> getAllHosts();

}
