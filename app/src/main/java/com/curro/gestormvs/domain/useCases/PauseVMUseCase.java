package com.curro.gestormvs.domain.useCases;

import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.models.VirtualMachine;

public class PauseVMUseCase {

    private final SshRepository sshRepository;

    public PauseVMUseCase(SshRepository sshRepository) { this.sshRepository = sshRepository; }


    // Use case -------------------------
    public void execute(VirtualMachine vm) {
        if (vm == null) throw new IllegalArgumentException("Virtual Machine cannot be null");
        if (!vm.getState().equalsIgnoreCase("running") &&
                !vm.getState().equalsIgnoreCase("paused")) {
            throw new IllegalArgumentException("Invalid virtual machine state");
        }

        switch (vm.getState().trim().toLowerCase()) {
            case "running":
                sshRepository.pauseVM(vm);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;

            case "paused":
                sshRepository.resumeVM(vm);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
        }
    }

}
