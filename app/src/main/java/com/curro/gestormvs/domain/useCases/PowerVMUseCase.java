package com.curro.gestormvs.domain.useCases;

import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.models.VirtualMachine;

public class PowerVMUseCase {

    private final SshRepository sshRepository;
    private final CheckVMStateUseCase checkVMStateUseCase;

    public PowerVMUseCase(SshRepository sshRepository, CheckVMStateUseCase checkVMStateUseCase) {
        this.sshRepository = sshRepository;
        this.checkVMStateUseCase = checkVMStateUseCase;
    }


    // Use case -------------------------
    public void execute(VirtualMachine vm) {
        if (vm == null) throw new IllegalArgumentException("Virtual Machine cannot be null");

        switch (vm.getState().toLowerCase()) {
            case "shutdown":
                throw new IllegalArgumentException(
                        "The virtual machine is still shutting down"
                );

            case "running":
                sshRepository.shutdownVM(vm);

                boolean result = checkVMStateUseCase.execute(vm.getName(), "shut off");
                if (!result) {
                    throw new RuntimeException("Error shutting down virtual machine." +
                            "It may be still starting or already turned off");
                }
                break;
            case "shut off":
                sshRepository.startVM(vm);
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
        }
    }


}