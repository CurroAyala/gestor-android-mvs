package com.curro.gestormvs.domain.useCases;

import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.models.VirtualMachine;

public class HibernateVMUseCase {

    private final SshRepository sshRepository;

    public HibernateVMUseCase(SshRepository sshRepository) {
        this.sshRepository = sshRepository;
    }


    // Use case -------------------------
    public void execute(VirtualMachine vm) {
        if (vm == null) throw new IllegalArgumentException("Virtual Machine cannot be null");
        if (!vm.getState().equalsIgnoreCase("running") &&
                !vm.getState().equalsIgnoreCase("shut off")) {
            throw new IllegalArgumentException("Invalid virtual machine state");
        }

        switch (vm.getState().trim().toLowerCase()) {
            case "running":
                sshRepository.hibernateVM(vm);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "shut off":
                boolean existingSaveFile = sshRepository.checkSaveFile(vm.getName());
                if (!existingSaveFile) {
                    throw new RuntimeException("No save file found for virtual machine");
                }

                sshRepository.restoreVM(vm);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
        }

    }


}
