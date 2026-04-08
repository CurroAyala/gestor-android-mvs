package com.curro.gestormvs.ui.virtualMachines.viewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.curro.gestormvs.data.repositories.HostRepository;
import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.useCases.CheckVMStateUseCase;
import com.curro.gestormvs.domain.useCases.ConnectHostUseCase;
import com.curro.gestormvs.domain.useCases.DisconnectHostUseCase;
import com.curro.gestormvs.domain.useCases.ListVMsUseCase;
import com.curro.gestormvs.domain.useCases.PowerVMUseCase;
import com.curro.gestormvs.domain.useCases.RebootVMUseCase;


/**
 * Global Factory for creating ViewModels across the application.

 * This class acts as a manual Dependency Injection container.
 * Instead of creating a specific factory for every single ViewModel, we provide
 * the core data layer dependencies to this shared factory.

 * When the UI layer requests a specific ViewModel, this factory instantiates
 * the required UseCases "on the fly" and injects them into the ViewModel.
 */
public class VirtualMachineViewModelFactory implements ViewModelProvider.Factory {

    private final HostRepository hostRepository;
    private final SshRepository repository;

    private final CheckVMStateUseCase checkVMStateUseCase;

    public VirtualMachineViewModelFactory(HostRepository hostRepository, SshRepository repository,
                                          CheckVMStateUseCase checkVMStateUseCase) {
        this.hostRepository = hostRepository;
        this.repository = repository;
        this.checkVMStateUseCase = checkVMStateUseCase;
    }


    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(VirtualMachineListViewModel.class)) {
            ConnectHostUseCase connectHostUseCase = new ConnectHostUseCase(hostRepository, repository);
            DisconnectHostUseCase disconnectHostUseCase = new DisconnectHostUseCase(repository);
            ListVMsUseCase listUseCase = new ListVMsUseCase(repository);
            PowerVMUseCase startUseCase = new PowerVMUseCase(repository, checkVMStateUseCase);
            RebootVMUseCase rebootUseCase = new RebootVMUseCase(repository, checkVMStateUseCase);

            return (T) new VirtualMachineListViewModel(connectHostUseCase,
                                                        disconnectHostUseCase,
                                                        listUseCase,
                                                        startUseCase,
                                                        rebootUseCase);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
