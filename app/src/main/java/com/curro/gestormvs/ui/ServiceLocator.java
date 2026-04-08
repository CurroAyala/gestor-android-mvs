package com.curro.gestormvs.ui;

import android.content.Context;

import com.curro.gestormvs.data.daos.HostDao;
import com.curro.gestormvs.data.db.AppDatabase;
import com.curro.gestormvs.data.repositories.HostRepository;
import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.useCases.CheckVMStateUseCase;

public class ServiceLocator {

    private static HostRepository hostRepository;
    private static SshRepository sshRepository;

    private static CheckVMStateUseCase checkVMStateUseCase;


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

    public static CheckVMStateUseCase provideCheckVMStateUseCase() {
        if (checkVMStateUseCase == null) {
            checkVMStateUseCase = new CheckVMStateUseCase(provideSshRepository());
        }
        return checkVMStateUseCase;
    }

}
