package com.curro.gestormvs.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.curro.gestormvs.data.daos.HostDao;
import com.curro.gestormvs.data.entities.HostEntity;

@Database(entities = {HostEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract HostDao hostDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "gestormvs_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
