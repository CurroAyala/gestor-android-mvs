package com.curro.gestormvs.domain.useCases;

import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.models.VirtualMachine;

public class CreateSnapshotUseCase {

    private final SshRepository sshRepository;

    public CreateSnapshotUseCase(SshRepository sshRepository) {
        this.sshRepository = sshRepository;
    }


    // Use case -------------------------
    public void execute(VirtualMachine vm, String snapshotName) {
        if (vm == null) throw new IllegalArgumentException("Virtual Machine cannot be null");
        if (snapshotName == null || snapshotName.isEmpty()) {
            throw new IllegalArgumentException("Snapshot name cannot be null or empty");
        }
        if (snapshotName.trim().length() > 30) {
            throw new IllegalArgumentException("Snapshot name cannot be longer than 30 characters");
        }
        if (snapshotName.trim().length() < 3) {
            throw new IllegalArgumentException("Snapshot name cannot be shorter than 3 characters");
        }
        if (vm.getState().equalsIgnoreCase("crashed")) {
            throw new IllegalArgumentException("Cannot create a snapshot from a crashed virtual machine");
        }

        sshRepository.createSnapshot(vm, snapshotName);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
