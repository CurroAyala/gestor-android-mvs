package com.curro.gestormvs.domain.useCases;

import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.models.Snapshot;
import com.curro.gestormvs.domain.models.VirtualMachine;

public class RevertSnapshotUseCase {

    private final SshRepository sshRepository;

    public RevertSnapshotUseCase(SshRepository sshRepository) {
        this.sshRepository = sshRepository;
    }


    // Use case -------------------------
    public void execute(VirtualMachine vm, Snapshot snapshot) {
        if (vm.getState().equalsIgnoreCase("crashed")) {
            throw new IllegalArgumentException("Cannot create a snapshot from a crashed virtual machine");
        }

        sshRepository.reverSnapshot(vm, snapshot);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
