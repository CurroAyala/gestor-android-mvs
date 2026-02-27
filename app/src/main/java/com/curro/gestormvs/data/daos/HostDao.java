package com.curro.gestormvs.data.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import com.curro.gestormvs.data.entities.HostEntity;

@Dao
public interface HostDao {

    @Insert
    void createHost(HostEntity host);

    @Update
    void updateHost(HostEntity host);

    @Delete
    void deleteHost(HostEntity host);


}
