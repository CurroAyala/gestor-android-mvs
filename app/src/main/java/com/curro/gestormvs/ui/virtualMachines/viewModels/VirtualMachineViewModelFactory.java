package com.curro.gestormvs.ui.virtualMachines.viewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.curro.gestormvs.data.repositories.HostRepository;
import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.useCases.ConnectHostUseCase;
import com.curro.gestormvs.domain.useCases.DisconnectHostUseCase;
import com.curro.gestormvs.domain.useCases.ListVMsUseCase;


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

    public VirtualMachineViewModelFactory(HostRepository hostRepository, SshRepository repository) {
        this.hostRepository = hostRepository;
        this.repository = repository;
    }


    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(VirtualMachineListViewModel.class)) {
            ConnectHostUseCase connectHostUseCase = new ConnectHostUseCase(hostRepository, repository);
            DisconnectHostUseCase disconnectHostUseCase = new DisconnectHostUseCase(repository);
            ListVMsUseCase listUseCase = new ListVMsUseCase(repository);
            return (T) new VirtualMachineListViewModel(connectHostUseCase,
                                                        disconnectHostUseCase,
                                                        listUseCase);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
