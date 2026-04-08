package com.curro.gestormvs.domain.useCases;

import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.models.VirtualMachine;

public class RebootVMUseCase {

    private final SshRepository sshRepository;
    private final CheckVMStateUseCase checkVMStateUseCase;


    public RebootVMUseCase(SshRepository sshRepository, CheckVMStateUseCase checkVMStateUseCase) {
        this.sshRepository = sshRepository;
        this.checkVMStateUseCase = checkVMStateUseCase;
    }


    // Use case -------------------------
    public void execute(VirtualMachine vm) {
        if (vm == null) throw new IllegalArgumentException("Virtual Machine cannot be null");
        if (!vm.getState().equalsIgnoreCase("running")) {
            throw new IllegalArgumentException("Only running virtual machines can be restarted");
        }

        // Using virsh command "reboot" is avoided because it is a soft reboot, what means that
        // VMs keep their state as "running", preventing state checking.

        sshRepository.shutdownVM(vm);

        boolean isDown = checkVMStateUseCase.execute(vm.getName(), "shut off");
        if (!isDown) {
            sshRepository.forceShutdownVM(vm);
            boolean idNowDown = checkVMStateUseCase.execute(vm.getName(), "shut off");

            if(!idNowDown) {
                throw new RuntimeException("Error shutting down. " +
                        "It took too long.");
            }
        }

        sshRepository.startVM(vm);

        boolean isUp = checkVMStateUseCase.execute(vm.getName(), "running");
        if (!isUp) {
            throw new RuntimeException("Error starting. " +
                    "It took too long.");
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
