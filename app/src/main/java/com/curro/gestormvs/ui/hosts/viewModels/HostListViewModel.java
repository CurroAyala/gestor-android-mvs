package com.curro.gestormvs.ui.hosts.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.curro.gestormvs.domain.models.Host;
import com.curro.gestormvs.domain.useCases.ListHostsUseCase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HostListViewModel extends ViewModel {

    private final ListHostsUseCase listHostsUseCase;
    private final MutableLiveData<List<Host>> hostsLiveData;
    private final MutableLiveData<String> errorLiveData;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public HostListViewModel(ListHostsUseCase useCase) {
        this.listHostsUseCase = useCase;
        this.hostsLiveData = new MutableLiveData<>();
        this.errorLiveData = new MutableLiveData<>();
    }


    public LiveData<List<Host>> getHosts() {
        return hostsLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void loadHosts() {
        executorService.execute(() -> {
            try {
                List<Host> hosts = listHostsUseCase.execute();
                hostsLiveData.postValue(hosts);
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

}
