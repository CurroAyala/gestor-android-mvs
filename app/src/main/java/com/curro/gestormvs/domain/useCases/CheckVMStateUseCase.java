package com.curro.gestormvs.domain.useCases;

import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.models.VirtualMachine;

import java.util.List;

public class CheckVMStateUseCase {

    private final SshRepository sshRepository;

    private static final int DELAY_MS = 2000;
    private static final int MAX_ATTEMPTS = 8;

    public CheckVMStateUseCase(SshRepository sshRepository) {
        this.sshRepository = sshRepository;
    }


    // Aux method -------------------------
    private VirtualMachine searchVM(List<VirtualMachine> list, String name) {
        for (VirtualMachine v : list) {
            if (v.getName().equalsIgnoreCase(name)) return v;
        }
        return null;
    }

    // Use case -------------------------
    public boolean execute(String vmName, String wishedState) {
        if (vmName == null) throw new IllegalArgumentException("Virtual Machine cannot be null");
        if (wishedState == null) throw new IllegalArgumentException("Wished state cannot be null");

        boolean res = false;
        int tries = 0;

        try {
            while (tries < MAX_ATTEMPTS) {
                Thread.sleep(DELAY_MS);
                List<VirtualMachine> vms = sshRepository.getAllVMs();
                VirtualMachine vm = searchVM(vms, vmName);

                if (vm != null && vm.getState().trim().equalsIgnoreCase(wishedState)) {
                    res = true;
                    break;
                }

                tries++;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return res;
    }


}
