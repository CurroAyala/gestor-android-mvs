package com.curro.gestormvs.ui.hosts.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.curro.gestormvs.domain.models.Host;
import com.curro.gestormvs.domain.useCases.CreateHostUseCase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HostCreateViewModel extends ViewModel {

    private final CreateHostUseCase createHostUseCase;
    private final MutableLiveData<String> errorLiveData;
    public MutableLiveData<Boolean> hostSaved = new MutableLiveData<>();


    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public HostCreateViewModel(CreateHostUseCase useCase) {
        this.createHostUseCase = useCase;
        this.errorLiveData = new MutableLiveData<>();
        this.hostSaved.setValue(false);
    }


    public void saveNewHost(String name, String user, String ip, String portText, String password) {
        executorService.execute(() -> {
            try {
                int port;
                if (portText.isBlank()) {
                    port = 22;
                } else {
                    port = Integer.parseInt(portText);
                }

                Host newHost = new Host(0, name, user, ip, port, password);
                createHostUseCase.execute(newHost);

                hostSaved.postValue(true);
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            }
        });
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

}