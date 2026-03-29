package com.curro.gestormvs.domain.useCases;

import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.models.VirtualMachine;

import java.util.List;

public class ListVMsUseCase {

    private final SshRepository sshRepository;

    public ListVMsUseCase(SshRepository sshRepository) {
        this.sshRepository = sshRepository;
    }


    // Use case -------------------------
    public List<VirtualMachine> execute() {
        return sshRepository.getAllVMs();
    }

}
