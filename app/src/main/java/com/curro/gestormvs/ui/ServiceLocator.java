package com.curro.gestormvs.ui;

import android.content.Context;

import com.curro.gestormvs.data.daos.HostDao;
import com.curro.gestormvs.data.db.AppDatabase;
import com.curro.gestormvs.data.repositories.HostRepository;
import com.curro.gestormvs.data.repositories.SshRepository;

public class ServiceLocator {

    private static HostRepository hostRepository;
    private static SshRepository sshRepository;

    public static HostRepository provideHostRepository(Context context) {
        if (hostRepository == null) {
            HostDao dao = AppDatabase.getDatabase(context).hostDao();
            hostRepository = new HostRepository(dao, context);
        }
        return hostRepository;
    }

    public static SshRepository provideSshRepository() {
        if (sshRepository == null) {
            sshRepository = new SshRepository();
        }
        return sshRepository;
    }

}
