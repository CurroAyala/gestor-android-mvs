package com.curro.gestormvs.domain.useCases;

import com.curro.gestormvs.data.repositories.HostRepository;
import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.models.Host;

public class ConnectHostUseCase {

    private final HostRepository hostRepository;
    private final SshRepository sshRepository;

    public ConnectHostUseCase(HostRepository hostRepository, SshRepository sshRepository) {
        this.hostRepository = hostRepository;
        this.sshRepository = sshRepository;
    }


    // Aux methods -------------------------
    public Host getHostById(long id) {
        return hostRepository.getHostById(id);
    }

    // Use case -------------------------
    public void execute(Host host) {
        sshRepository.connect(host);
    }

}
