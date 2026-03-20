package com.curro.gestormvs.ui.hosts.viewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.curro.gestormvs.data.repositories.HostRepository;
import com.curro.gestormvs.domain.useCases.CreateHostUseCase;
import com.curro.gestormvs.domain.useCases.DeleteHostUseCase;
import com.curro.gestormvs.domain.useCases.ListHostsUseCase;
import com.curro.gestormvs.domain.useCases.UpdateHostUseCase;


/**
 * Global Factory for creating ViewModels across the application.

 * This class acts as a manual Dependency Injection container.
 * Instead of creating a specific factory for every single ViewModel, we provide
 * the core data layer dependencies to this shared factory.

 * When the UI layer requests a specific ViewModel, this factory instantiates
 * the required UseCases "on the fly" and injects them into the ViewModel.
 */
public class HostViewModelFactory implements ViewModelProvider.Factory {

    private final HostRepository repository;

    public HostViewModelFactory(HostRepository repository) {
        this.repository = repository;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(HostListViewModel.class)) {
            ListHostsUseCase listUseCase = new ListHostsUseCase(repository);
            DeleteHostUseCase deleteUseCase = new DeleteHostUseCase(repository);
            return (T) new HostListViewModel(listUseCase, deleteUseCase);
        }

        else if (modelClass.isAssignableFrom(HostCreateViewModel.class)) {
            CreateHostUseCase createUseCase = new CreateHostUseCase(repository);
            return (T) new HostCreateViewModel(createUseCase);
        }

        else if (modelClass.isAssignableFrom(HostUpdateViewModel.class)) {
            UpdateHostUseCase updateUseCase = new UpdateHostUseCase(repository);
            return (T) new HostUpdateViewModel(updateUseCase);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }

}
