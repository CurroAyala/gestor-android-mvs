package com.curro.gestormvs.ui.hosts.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.curro.gestormvs.domain.models.Host;
import com.curro.gestormvs.domain.useCases.UpdateHostUseCase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HostUpdateViewModel extends ViewModel {

    private final UpdateHostUseCase updateHostUseCase;
    private final MutableLiveData<Host> hostLiveData;
    private final MutableLiveData<String> errorLiveData;
    public MutableLiveData<Boolean> hostUpdated = new MutableLiveData<>();


    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public HostUpdateViewModel(UpdateHostUseCase useCase) {
        this.updateHostUseCase = useCase;
        this.hostLiveData = new MutableLiveData<>();
        this.errorLiveData = new MutableLiveData<>();
        this.hostUpdated.setValue(false);
    }


    public void loadHost(long Id) {
        executorService.execute(() -> {
            try {
                Host host = updateHostUseCase.getHostById(Id);
                if (host != null) {
                    hostLiveData.postValue(host);
                } else {
                    errorLiveData.postValue("Host not found");
                }
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            }
        });
    }

    public void saveExistingHost (long id, String name, String user,
                                  String ip, String portText, String passwordText) {
        executorService.execute(() -> {
            try {
                int port;
                if (portText.isBlank()) {
                    port = 22;
                } else {
                    port = Integer.parseInt(portText);
                }

                String password;
                if (passwordText.isBlank()) {
                    password = null;
                } else {
                    password = passwordText.trim();
                }

                Host updatedCost = new Host(id, name, user, ip, port, password);
                updateHostUseCase.execute(updatedCost);

                hostUpdated.postValue(true);
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            }
        });
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Host> getHostData() {
        return hostLiveData;
    }

}
