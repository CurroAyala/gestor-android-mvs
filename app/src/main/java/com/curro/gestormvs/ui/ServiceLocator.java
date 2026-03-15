package com.curro.gestormvs.ui;

import android.content.Context;

import com.curro.gestormvs.data.daos.HostDao;
import com.curro.gestormvs.data.db.AppDatabase;
import com.curro.gestormvs.data.repositories.HostRepository;

public class ServiceLocator {

    private static HostRepository hostRepository;

    public static HostRepository provideHostRepository(Context context) {
        if (hostRepository == null) {
            HostDao dao = AppDatabase.getDatabase(context).hostDao();
            hostRepository = new HostRepository(dao, context);
        }
        return hostRepository;
    }

}
