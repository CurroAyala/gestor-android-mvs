package com.curro.gestormvs.data.mappers;

import com.curro.gestormvs.data.entities.HostEntity;
import com.curro.gestormvs.domain.models.Host;

public class HostMapper {

    public static Host toDomain(HostEntity entity, String decryptedPassword) {
        if (entity == null) { return null; }

        return new Host(
                entity.id,
                entity.name,
                entity.user,
                entity.ip,
                entity.port,
                decryptedPassword
        );
    }

    public static HostEntity toEntity(Host domain, String encryptedPassword) {
        if (domain == null) { return null; }

        return new HostEntity(
                domain.getId(),
                domain.getName(),
                domain.getUser(),
                domain.getIp(),
                domain.getPort(),
                encryptedPassword
        );
    }

}
