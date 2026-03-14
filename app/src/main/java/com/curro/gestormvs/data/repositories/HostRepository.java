package com.curro.gestormvs.data.repositories;

import android.content.Context;
import android.util.Log;

import com.curro.gestormvs.data.daos.HostDao;
import com.curro.gestormvs.data.entities.HostEntity;
import com.curro.gestormvs.data.mappers.HostMapper;
import com.curro.gestormvs.domain.models.Host;
import com.curro.gestormvs.data.security.KeystoreManager;

import java.util.ArrayList;
import java.util.List;


public class HostRepository {

    private final HostDao hostDao;
    private final KeystoreManager keystoreManager;

    public HostRepository(HostDao hostDao, Context context) throws Exception {
        this.hostDao = hostDao;
        this.keystoreManager = new KeystoreManager(context);
    }


    // CRUD methods -----------------------------------------
    public List<Host> getAllHosts() {
        try {
            List<Host> res = new ArrayList<>();

            List<HostEntity> entities = hostDao.getAllHosts();
            for (HostEntity entity : entities) {
                String decryptedPassword = keystoreManager.decrypt(entity.encryptedPassword, "hostId="+entity.id);
                res.add(HostMapper.toDomain(entity, decryptedPassword));
            }

            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public Host getHostById(long id) {
        try {
            HostEntity entity = hostDao.getHostById(id);
            String decryptedPassword = keystoreManager.decrypt(entity.encryptedPassword, "hostId="+entity.id);
            return HostMapper.toDomain(entity, decryptedPassword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Host getHostByIp(String ip) {
        try {
            HostEntity entity = hostDao.getHostByIp(ip);
            String decryptedPassword = keystoreManager.decrypt(entity.encryptedPassword, "hostId="+entity.id);
            return HostMapper.toDomain(entity, decryptedPassword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createHost(Host host) {
        try {
            String encryptedPassword = keystoreManager.encrypt(host.getPassword(), "hostId="+host.getId());
            HostEntity entity = HostMapper.toEntity(host, encryptedPassword);
            hostDao.createHost(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateHost(Host host) {
        try {
            String encryptedPassword = keystoreManager.encrypt(host.getPassword(), "hostId="+host.getId());
            HostEntity entity = HostMapper.toEntity(host, encryptedPassword);
            hostDao.updateHost(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteHost(Host host) {
        try {
            String encryptedPassword = keystoreManager.encrypt(host.getPassword(), "hostId="+host.getId());
            HostEntity entity = HostMapper.toEntity(host, encryptedPassword);
            hostDao.deleteHost(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
