package com.curro.gestormvs.domain.useCases;

import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.models.Snapshot;
import com.curro.gestormvs.domain.models.VirtualMachine;

import java.util.List;

public class ListSnapshotsUseCase {

    private final SshRepository sshRepository;

    public ListSnapshotsUseCase(SshRepository sshRepository) { this.sshRepository = sshRepository; }


    // Use case -------------------------
    public List<Snapshot> execute(VirtualMachine vm) {
        if (vm == null) throw new IllegalArgumentException("Virtual Machine cannot be null");

        return sshRepository.getAllSnapshots(vm);
    }

}
