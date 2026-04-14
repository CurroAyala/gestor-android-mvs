package com.curro.gestormvs.domain.useCases;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.models.VirtualMachine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class VmOperationsUseCasesTest {

    @Mock
    private SshRepository mockSshRepository;

    @Mock
    private CheckVMStateUseCase mockCheckVMStateUseCase;

    private PowerVMUseCase powerVMUseCase;
    private RebootVMUseCase rebootVMUseCase;
    private PauseVMUseCase pauseVMUseCase;
    private HibernateVMUseCase hibernateVMUseCase;


    @Before
    public void setUp() {

        powerVMUseCase    = new PowerVMUseCase(mockSshRepository, mockCheckVMStateUseCase);
        rebootVMUseCase   = new RebootVMUseCase(mockSshRepository, mockCheckVMStateUseCase);
        pauseVMUseCase    = new PauseVMUseCase(mockSshRepository);
        hibernateVMUseCase = new HibernateVMUseCase(mockSshRepository);
    }


    // =========================================================================
    // POWER VM — PowerVMUseCase
    // =========================================================================

    @Test
    public void givenNullVm_whenPower_thenThrowsIllegalArgumentException() {
        // Given — no VM provided

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> powerVMUseCase.execute(null));
    }

    @Test
    public void givenNullVm_whenPower_thenRepositoryIsNeverCalled() {
        // Given — no VM provided

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> powerVMUseCase.execute(null));
        verify(mockSshRepository, never()).shutdownVM(null);
        verify(mockSshRepository, never()).startVM(null);
    }

    @Test
    public void givenVmInShuttingDownState_whenPower_thenThrowsIllegalArgumentException() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "shutdown");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> powerVMUseCase.execute(vm));
    }

    @Test
    public void givenVmInShuttingDownState_whenPower_thenRepositoryIsNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "shutdown");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> powerVMUseCase.execute(vm));
        verify(mockSshRepository, never()).shutdownVM(vm);
        verify(mockSshRepository, never()).startVM(vm);
        verify(mockSshRepository, never()).forceShutdownVM(vm);
    }

    @Test
    public void givenRunningVm_whenPowerAndGracefulShutdownSucceeds_thenShutdownVmIsCalledOnce() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        when(mockCheckVMStateUseCase.execute(vm.getName(), "shut off")).thenReturn(true);

        // When
        powerVMUseCase.execute(vm);

        // Then
        verify(mockSshRepository, times(1)).shutdownVM(vm);
    }

    @Test
    public void givenRunningVm_whenPowerAndGracefulShutdownSucceeds_thenForceShutdownIsNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        when(mockCheckVMStateUseCase.execute(vm.getName(), "shut off")).thenReturn(true);

        // When
        powerVMUseCase.execute(vm);

        // Then
        verify(mockSshRepository, never()).forceShutdownVM(vm);
    }

    @Test
    public void givenRunningVm_whenGracefulShutdownFailsAndForceSucceeds_thenForceShutdownIsCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        when(mockCheckVMStateUseCase.execute(vm.getName(), "shut off"))
                .thenReturn(false)  // graceful shutdown did not reach "shut off"
                .thenReturn(true);  // force shutdown reached "shut off"

        // When
        powerVMUseCase.execute(vm);

        // Then
        verify(mockSshRepository, times(1)).shutdownVM(vm);
        verify(mockSshRepository, times(1)).forceShutdownVM(vm);
    }

    @Test
    public void givenRunningVm_whenBothShutdownsFail_thenThrowsRuntimeException() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        when(mockCheckVMStateUseCase.execute(vm.getName(), "shut off"))
                .thenReturn(false)
                .thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> powerVMUseCase.execute(vm));
    }

    @Test
    public void givenRunningVm_whenBothShutdownsFail_thenBothShutdownMethodsAreCalledBeforeException() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        when(mockCheckVMStateUseCase.execute(vm.getName(), "shut off"))
                .thenReturn(false)
                .thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> powerVMUseCase.execute(vm));
        verify(mockSshRepository, times(1)).shutdownVM(vm);
        verify(mockSshRepository, times(1)).forceShutdownVM(vm);
    }

    @Test
    public void givenRunningVm_whenBothShutdownsFail_thenStartVmIsNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        when(mockCheckVMStateUseCase.execute(vm.getName(), "shut off"))
                .thenReturn(false)
                .thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> powerVMUseCase.execute(vm));
        verify(mockSshRepository, never()).startVM(vm);
    }

    @Test
    public void givenShutOffVm_whenPower_thenStartVmIsCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "centos-7", "shut off");

        // When
        powerVMUseCase.execute(vm);

        // Then
        verify(mockSshRepository, times(1)).startVM(vm);
    }

    @Test
    public void givenShutOffVm_whenPower_thenShutdownIsNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "centos-7", "shut off");

        // When
        powerVMUseCase.execute(vm);

        // Then
        verify(mockSshRepository, never()).shutdownVM(vm);
        verify(mockSshRepository, never()).forceShutdownVM(vm);
    }

    @Test
    public void givenRepositoryThrowsOnShutdown_whenPower_thenExceptionPropagates() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        doThrow(new RuntimeException("Error creating channel")).when(mockSshRepository).shutdownVM(vm);

        // When & Then
        assertThrows(RuntimeException.class, () -> powerVMUseCase.execute(vm));
    }

    @Test
    public void givenRepositoryThrowsOnStart_whenPower_thenExceptionPropagates() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "centos-7", "shut off");
        doThrow(new RuntimeException("Error creating channel")).when(mockSshRepository).startVM(vm);

        // When & Then
        assertThrows(RuntimeException.class, () -> powerVMUseCase.execute(vm));
    }


    // =========================================================================
    // REBOOT VM — RebootVMUseCase
    // =========================================================================

    @Test
    public void givenNullVm_whenReboot_thenThrowsIllegalArgumentException() {
        // Given — no VM provided

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> rebootVMUseCase.execute(null));
    }

    @Test
    public void givenShutOffVm_whenReboot_thenThrowsIllegalArgumentException() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "centos-7", "shut off");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> rebootVMUseCase.execute(vm));
    }

    @Test
    public void givenPausedVm_whenReboot_thenThrowsIllegalArgumentException() {
        // Given
        VirtualMachine vm = new VirtualMachine(2, "debian-11", "paused");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> rebootVMUseCase.execute(vm));
    }

    @Test
    public void givenNonRunningVm_whenReboot_thenRepositoryIsNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "centos-7", "shut off");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> rebootVMUseCase.execute(vm));
        verify(mockSshRepository, never()).shutdownVM(vm);
        verify(mockSshRepository, never()).startVM(vm);
        verify(mockSshRepository, never()).forceShutdownVM(vm);
    }

    @Test
    public void givenRunningVm_whenRebootAndGracefulShutdownSucceeds_thenShutdownAndStartAreCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        when(mockCheckVMStateUseCase.execute(vm.getName(), "shut off")).thenReturn(true);
        when(mockCheckVMStateUseCase.execute(vm.getName(), "running")).thenReturn(true);

        // When
        rebootVMUseCase.execute(vm);

        // Then
        verify(mockSshRepository, times(1)).shutdownVM(vm);
        verify(mockSshRepository, times(1)).startVM(vm);
    }

    @Test
    public void givenRunningVm_whenRebootAndGracefulShutdownSucceeds_thenForceShutdownIsNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        when(mockCheckVMStateUseCase.execute(vm.getName(), "shut off")).thenReturn(true);
        when(mockCheckVMStateUseCase.execute(vm.getName(), "running")).thenReturn(true);

        // When
        rebootVMUseCase.execute(vm);

        // Then
        verify(mockSshRepository, never()).forceShutdownVM(vm);
    }

    @Test
    public void givenRunningVm_whenGracefulShutdownFailsAndForceSucceeds_thenBothShutdownsAndStartAreCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        when(mockCheckVMStateUseCase.execute(vm.getName(), "shut off"))
                .thenReturn(false)
                .thenReturn(true);
        when(mockCheckVMStateUseCase.execute(vm.getName(), "running")).thenReturn(true);

        // When
        rebootVMUseCase.execute(vm);

        // Then
        verify(mockSshRepository, times(1)).shutdownVM(vm);
        verify(mockSshRepository, times(1)).forceShutdownVM(vm);
        verify(mockSshRepository, times(1)).startVM(vm);
    }

    @Test
    public void givenRunningVm_whenBothShutdownsFail_thenThrowsRuntimeExceptionBeforeStart() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        when(mockCheckVMStateUseCase.execute(vm.getName(), "shut off"))
                .thenReturn(false)
                .thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> rebootVMUseCase.execute(vm));
        verify(mockSshRepository, never()).startVM(vm);
    }

    @Test
    public void givenRunningVm_whenShutdownSucceedsButVmNeverComesBackUp_thenThrowsRuntimeException() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        when(mockCheckVMStateUseCase.execute(vm.getName(), "shut off")).thenReturn(true);
        when(mockCheckVMStateUseCase.execute(vm.getName(), "running")).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> rebootVMUseCase.execute(vm));
    }

    @Test
    public void givenRunningVm_whenShutdownSucceedsButVmNeverComesBackUp_thenStartVmWasStillCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        when(mockCheckVMStateUseCase.execute(vm.getName(), "shut off")).thenReturn(true);
        when(mockCheckVMStateUseCase.execute(vm.getName(), "running")).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> rebootVMUseCase.execute(vm));
        verify(mockSshRepository, times(1)).startVM(vm);
    }

    @Test
    public void givenRepositoryThrowsOnShutdown_whenReboot_thenExceptionPropagates() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        doThrow(new RuntimeException("Error creating channel")).when(mockSshRepository).shutdownVM(vm);

        // When & Then
        assertThrows(RuntimeException.class, () -> rebootVMUseCase.execute(vm));
    }


    // =========================================================================
    // PAUSE VM — PauseVMUseCase
    // =========================================================================

    @Test
    public void givenNullVm_whenPause_thenThrowsIllegalArgumentException() {
        // Given — no VM provided

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> pauseVMUseCase.execute(null));
    }

    @Test
    public void givenShutOffVm_whenPause_thenThrowsIllegalArgumentException() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "centos-7", "shut off");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> pauseVMUseCase.execute(vm));
    }

    @Test
    public void givenCrashedVm_whenPause_thenThrowsIllegalArgumentException() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "ubuntu-20.04", "crashed");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> pauseVMUseCase.execute(vm));
    }

    @Test
    public void givenInvalidStateVm_whenPause_thenRepositoryIsNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "centos-7", "shut off");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> pauseVMUseCase.execute(vm));
        verify(mockSshRepository, never()).pauseVM(vm);
        verify(mockSshRepository, never()).resumeVM(vm);
    }

    @Test
    public void givenRunningVm_whenPause_thenPauseVmIsCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");

        // When
        pauseVMUseCase.execute(vm);

        // Then
        verify(mockSshRepository, times(1)).pauseVM(vm);
    }

    @Test
    public void givenRunningVm_whenPause_thenResumeVmIsNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");

        // When
        pauseVMUseCase.execute(vm);

        // Then
        verify(mockSshRepository, never()).resumeVM(vm);
    }

    @Test
    public void givenPausedVm_whenPause_thenResumeVmIsCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(2, "debian-11", "paused");

        // When
        pauseVMUseCase.execute(vm);

        // Then
        verify(mockSshRepository, times(1)).resumeVM(vm);
    }

    @Test
    public void givenPausedVm_whenPause_thenPauseVmIsNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(2, "debian-11", "paused");

        // When
        pauseVMUseCase.execute(vm);

        // Then
        verify(mockSshRepository, never()).pauseVM(vm);
    }

    @Test
    public void givenRepositoryThrowsOnPause_whenPause_thenExceptionPropagates() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        doThrow(new RuntimeException("Error creating channel")).when(mockSshRepository).pauseVM(vm);

        // When & Then
        assertThrows(RuntimeException.class, () -> pauseVMUseCase.execute(vm));
    }

    @Test
    public void givenRepositoryThrowsOnResume_whenPause_thenExceptionPropagates() {
        // Given
        VirtualMachine vm = new VirtualMachine(2, "debian-11", "paused");
        doThrow(new RuntimeException("Error creating channel")).when(mockSshRepository).resumeVM(vm);

        // When & Then
        assertThrows(RuntimeException.class, () -> pauseVMUseCase.execute(vm));
    }


    // =========================================================================
    // HIBERNATE VM — HibernateVMUseCase
    // =========================================================================

    @Test
    public void givenNullVm_whenHibernate_thenThrowsIllegalArgumentException() {
        // Given — no VM provided

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> hibernateVMUseCase.execute(null));
    }

    @Test
    public void givenPausedVm_whenHibernate_thenThrowsIllegalArgumentException() {
        // Given
        VirtualMachine vm = new VirtualMachine(2, "debian-11", "paused");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> hibernateVMUseCase.execute(vm));
    }

    @Test
    public void givenCrashedVm_whenHibernate_thenThrowsIllegalArgumentException() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "ubuntu-20.04", "crashed");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> hibernateVMUseCase.execute(vm));
    }

    @Test
    public void givenInvalidStateVm_whenHibernate_thenRepositoryIsNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(2, "debian-11", "paused");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> hibernateVMUseCase.execute(vm));
        verify(mockSshRepository, never()).hibernateVM(vm);
        verify(mockSshRepository, never()).restoreVM(vm);
        verify(mockSshRepository, never()).checkSaveFile(vm.getName());
    }

    @Test
    public void givenRunningVm_whenHibernate_thenHibernateVmIsCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");

        // When
        hibernateVMUseCase.execute(vm);

        // Then
        verify(mockSshRepository, times(1)).hibernateVM(vm);
    }

    @Test
    public void givenRunningVm_whenHibernate_thenRestoreAndCheckSaveFileAreNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");

        // When
        hibernateVMUseCase.execute(vm);

        // Then
        verify(mockSshRepository, never()).restoreVM(vm);
        verify(mockSshRepository, never()).checkSaveFile(vm.getName());
    }

    @Test
    public void givenShutOffVmWithExistingSaveFile_whenHibernate_thenRestoreVmIsCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "centos-7", "shut off");
        when(mockSshRepository.checkSaveFile(vm.getName())).thenReturn(true);

        // When
        hibernateVMUseCase.execute(vm);

        // Then
        verify(mockSshRepository, times(1)).restoreVM(vm);
    }

    @Test
    public void givenShutOffVmWithExistingSaveFile_whenHibernate_thenHibernateVmIsNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "centos-7", "shut off");
        when(mockSshRepository.checkSaveFile(vm.getName())).thenReturn(true);

        // When
        hibernateVMUseCase.execute(vm);

        // Then
        verify(mockSshRepository, never()).hibernateVM(vm);
    }

    @Test
    public void givenShutOffVmWithNoSaveFile_whenHibernate_thenThrowsRuntimeException() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "centos-7", "shut off");
        when(mockSshRepository.checkSaveFile(vm.getName())).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> hibernateVMUseCase.execute(vm));
    }

    @Test
    public void givenShutOffVmWithNoSaveFile_whenHibernate_thenRestoreVmIsNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "centos-7", "shut off");
        when(mockSshRepository.checkSaveFile(vm.getName())).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> hibernateVMUseCase.execute(vm));
        verify(mockSshRepository, never()).restoreVM(vm);
    }

    @Test
    public void givenShutOffVmWithNoSaveFile_whenHibernate_thenSaveFileWasCheckedOnce() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "centos-7", "shut off");
        when(mockSshRepository.checkSaveFile(vm.getName())).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> hibernateVMUseCase.execute(vm));
        verify(mockSshRepository, times(1)).checkSaveFile(vm.getName());
    }

    @Test
    public void givenRepositoryThrowsOnHibernate_whenHibernate_thenExceptionPropagates() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        doThrow(new RuntimeException("Access denied to virtual machine"))
                .when(mockSshRepository).hibernateVM(vm);

        // When & Then
        assertThrows(RuntimeException.class, () -> hibernateVMUseCase.execute(vm));
    }

    @Test
    public void givenRepositoryThrowsOnRestore_whenHibernate_thenExceptionPropagates() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "centos-7", "shut off");
        when(mockSshRepository.checkSaveFile(vm.getName())).thenReturn(true);
        doThrow(new RuntimeException("Error creating channel"))
                .when(mockSshRepository).restoreVM(vm);

        // When & Then
        assertThrows(RuntimeException.class, () -> hibernateVMUseCase.execute(vm));
    }

}
