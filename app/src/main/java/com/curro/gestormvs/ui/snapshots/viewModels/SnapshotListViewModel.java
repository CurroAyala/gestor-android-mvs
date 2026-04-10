package com.curro.gestormvs.ui.snapshots.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.curro.gestormvs.domain.models.Snapshot;
import com.curro.gestormvs.domain.models.VirtualMachine;
import com.curro.gestormvs.domain.useCases.DeleteSnapshotUseCase;
import com.curro.gestormvs.domain.useCases.ListSnapshotsUseCase;
import com.curro.gestormvs.domain.useCases.RevertSnapshotUseCase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SnapshotListViewModel extends ViewModel {

    private final ListSnapshotsUseCase listSnapshotsUseCase;
    private final DeleteSnapshotUseCase deleteSnapshotUseCase;
    private final RevertSnapshotUseCase revertSnapshotUseCase;

    private final MutableLiveData<List<Snapshot>> snapshotsLiveData;
    private final MutableLiveData<Boolean> loadingLiveData;
    private final MutableLiveData<String> messageLiveData;
    private final MutableLiveData<String> errorLiveData;


    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public SnapshotListViewModel(ListSnapshotsUseCase listSnapshotsUseCase,
                                 DeleteSnapshotUseCase deleteSnapshotUseCase,
                                 RevertSnapshotUseCase revertSnapshotUseCase) {
        this.listSnapshotsUseCase = listSnapshotsUseCase;
        this.deleteSnapshotUseCase = deleteSnapshotUseCase;
        this.revertSnapshotUseCase = revertSnapshotUseCase;

        this.snapshotsLiveData = new MutableLiveData<>();
        this.loadingLiveData = new MutableLiveData<>();
        this.messageLiveData = new MutableLiveData<>();
        this.errorLiveData = new MutableLiveData<>();

    }

    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }
    public LiveData<String> getMessage() { return messageLiveData; }
    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<List<Snapshot>> getSnapshots() { return snapshotsLiveData; }

    public void loadSnapshots(VirtualMachine vm) {
        executorService.execute(() -> {
            try {
                loadingLiveData.postValue(true);

                List<Snapshot> snapshots = listSnapshotsUseCase.execute(vm);
                snapshotsLiveData.postValue(snapshots);
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            } finally {
                loadingLiveData.postValue(false);
            }
        });
    }

    public void deleteSnapshot(VirtualMachine vm, Snapshot snapshot) {
        executorService.execute(() -> {
            try {
                loadingLiveData.postValue(true);
                deleteSnapshotUseCase.execute(vm, snapshot);

                List<Snapshot> snapshots = listSnapshotsUseCase.execute(vm);
                snapshotsLiveData.postValue(snapshots);

                messageLiveData.postValue("Snapshot successfully deleted");
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            } finally {
                loadingLiveData.postValue(false);
            }
        });
    }

    public void revertSnapshot(VirtualMachine vm, Snapshot snapshot) {
        executorService.execute(() -> {
            try {
                loadingLiveData.postValue(true);

                revertSnapshotUseCase.execute(vm, snapshot);

                messageLiveData.postValue("Snapshot successfully reverted");
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            } finally {
                loadingLiveData.postValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

}