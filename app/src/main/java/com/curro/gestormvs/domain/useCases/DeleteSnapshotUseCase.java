package com.curro.gestormvs.domain.useCases;

import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.models.Snapshot;
import com.curro.gestormvs.domain.models.VirtualMachine;

public class DeleteSnapshotUseCase {

    private final SshRepository sshRepository;

    public DeleteSnapshotUseCase(SshRepository sshRepository) {
        this.sshRepository = sshRepository;
    }


    // Use case -------------------------
    public void execute(VirtualMachine vm, Snapshot snapshot) {
        if (vm == null) throw new IllegalArgumentException("Virtual Machine cannot be null");
        if (snapshot.getName() == null || snapshot.getName().isEmpty())
            throw new IllegalArgumentException("Snapshot name cannot be null or empty");

        sshRepository.deleteSnapshot(vm, snapshot);
    }

}
