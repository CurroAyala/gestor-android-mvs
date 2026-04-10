package com.curro.gestormvs.ui.snapshots.viewModels;


import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.useCases.DeleteSnapshotUseCase;
import com.curro.gestormvs.domain.useCases.ListSnapshotsUseCase;
import com.curro.gestormvs.domain.useCases.RevertSnapshotUseCase;

/**
 * Global Factory for creating ViewModels across the application.

 * This class acts as a manual Dependency Injection container.
 * Instead of creating a specific factory for every single ViewModel, we provide
 * the core data layer dependencies to this shared factory.

 * When the UI layer requests a specific ViewModel, this factory instantiates
 * the required UseCases "on the fly" and injects them into the ViewModel.
 */
public class SnapshotViewModelFactory implements ViewModelProvider.Factory {

    private final SshRepository repository;

    public SnapshotViewModelFactory(SshRepository repository) {
        this.repository = repository;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(SnapshotListViewModel.class)) {
            ListSnapshotsUseCase listUseCase = new ListSnapshotsUseCase(repository);
            DeleteSnapshotUseCase deleteUseCase = new DeleteSnapshotUseCase(repository);
            RevertSnapshotUseCase revertUseCase = new RevertSnapshotUseCase(repository);

            return (T) new SnapshotListViewModel(listUseCase, deleteUseCase, revertUseCase);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
