package com.curro.gestormvs.ui.virtualMachines.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.curro.gestormvs.domain.models.Host;
import com.curro.gestormvs.domain.models.VirtualMachine;
import com.curro.gestormvs.domain.useCases.ConnectHostUseCase;
import com.curro.gestormvs.domain.useCases.DisconnectHostUseCase;
import com.curro.gestormvs.domain.useCases.HibernateVMUseCase;
import com.curro.gestormvs.domain.useCases.ListVMsUseCase;
import com.curro.gestormvs.domain.useCases.PowerVMUseCase;
import com.curro.gestormvs.domain.useCases.RebootVMUseCase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualMachineListViewModel extends ViewModel {

    private final ConnectHostUseCase connectHostUseCase;
    private final DisconnectHostUseCase disconnectHostUseCase;
    private final ListVMsUseCase listMVsUseCase;
    private final PowerVMUseCase powerVMUseCase;
    private final RebootVMUseCase rebootVMUseCase;
    private final HibernateVMUseCase hibernateVMUseCase;

    private final MutableLiveData<Host> hostLiveData;
    private final MutableLiveData<List<VirtualMachine>> vmsLiveData;
    private final MutableLiveData<Boolean> loadingLiveData;
    private final MutableLiveData<String> messageLiveData;
    private final MutableLiveData<String> errorLiveData;


    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public VirtualMachineListViewModel(ConnectHostUseCase connectUseCase,
                                       DisconnectHostUseCase disconnectHostUseCase,
                                       ListVMsUseCase listUseCase,
                                       PowerVMUseCase powerVMUseCase,
                                       RebootVMUseCase rebootVMUseCase,
                                       HibernateVMUseCase hibernateVMUseCase) {
        this.connectHostUseCase = connectUseCase;
        this.disconnectHostUseCase = disconnectHostUseCase;
        this.listMVsUseCase = listUseCase;
        this.powerVMUseCase = powerVMUseCase;
        this.rebootVMUseCase = rebootVMUseCase;
        this.hibernateVMUseCase = hibernateVMUseCase;

        this.hostLiveData = new MutableLiveData<>();
        this.vmsLiveData = new MutableLiveData<>();
        this.loadingLiveData = new MutableLiveData<>();
        this.messageLiveData = new MutableLiveData<>();
        this.errorLiveData = new MutableLiveData<>();
    }

    public LiveData<List<VirtualMachine>> getVMs() {
        return vmsLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }

    public LiveData<Host> getHostData() { return hostLiveData; }

    public LiveData<List<VirtualMachine>> getVms()  { return vmsLiveData; }

    public LiveData<String> getMessage() { return messageLiveData; }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void loadHost(long hostId) {
        executorService.execute(() -> {
            try {
                Host host = connectHostUseCase.getHostById(hostId);
                if (host != null) {
                    hostLiveData.postValue(host);
                } else {
                    errorLiveData.postValue("Host not found.");
                }
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            }
        });
    }

    public void connectAndLoadVMs(Host host) {
        executorService.execute(() -> {
            try {
                loadingLiveData.postValue(true);
                connectHostUseCase.execute(host);
                List<VirtualMachine> vms = listMVsUseCase.execute();
                vmsLiveData.postValue(vms);
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            } finally {
                loadingLiveData.postValue(false);
            }
        });
    }

    public void disconnect() {
        executorService.execute(() -> {
            try {
                disconnectHostUseCase.execute();
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            }
        });
    }

    public void powerVM(VirtualMachine vm) {
        executorService.execute(() -> {
            try {
                loadingLiveData.postValue(true);
                boolean isStarting = vm.getState().trim().equalsIgnoreCase("shut off");

                powerVMUseCase.execute(vm);

                List<VirtualMachine> vms = listMVsUseCase.execute();
                vmsLiveData.postValue(vms);

                if (isStarting) {
                    messageLiveData.postValue("Virtual machine started");
                } else {
                    messageLiveData.postValue("Virtual machine stopped");
                }
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            } finally {
                loadingLiveData.postValue(false);
            }
        });
    }

    public void rebootVM(VirtualMachine vm) {
        executorService.execute(() -> {
            try {
                loadingLiveData.postValue(true);

                rebootVMUseCase.execute(vm);

                List<VirtualMachine> vms = listMVsUseCase.execute();
                vmsLiveData.postValue(vms);

                messageLiveData.postValue("Virtual machine rebooted");
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            } finally {
                loadingLiveData.postValue(false);
            }
        });
    }

    public void hibernate(VirtualMachine vm) {
        executorService.execute(() -> {
            try {
                loadingLiveData.postValue(true);
                boolean isHibernating = vm.getState().trim().equalsIgnoreCase("running");

                hibernateVMUseCase.execute(vm);

                List<VirtualMachine> vms = listMVsUseCase.execute();
                vmsLiveData.postValue(vms);

                if (isHibernating) {
                    messageLiveData.postValue("Virtual machine hibernated");
                } else {
                    messageLiveData.postValue("Virtual machine restored");
                }

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
