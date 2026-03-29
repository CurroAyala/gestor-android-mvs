package com.curro.gestormvs.domain.useCases;

import com.curro.gestormvs.data.repositories.SshRepository;

public class DisconnectHostUseCase {

    private final SshRepository sshRepository;

    public DisconnectHostUseCase(SshRepository sshRepository) {
        this.sshRepository = sshRepository;
    }


    // Use case -------------------------
    public void execute() {
        sshRepository.disconnect();
    }

}
