package com.curro.gestormvs.domain.useCases;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.curro.gestormvs.data.repositories.HostRepository;
import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.models.Host;
import com.curro.gestormvs.domain.models.VirtualMachine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SshConnectionUseCasesTest {

    @Mock
    private SshRepository mockSshRepository;

    @Mock
    private HostRepository mockHostRepository;

    private ConnectHostUseCase connectHostUseCase;
    private DisconnectHostUseCase disconnectHostUseCase;
    private ListVMsUseCase listVMsUseCase;


    @Before
    public void setUp() {
        connectHostUseCase    = new ConnectHostUseCase(mockHostRepository, mockSshRepository);
        disconnectHostUseCase = new DisconnectHostUseCase(mockSshRepository);
        listVMsUseCase        = new ListVMsUseCase(mockSshRepository);
    }


    // =========================================================================
    // CONNECT — execute()
    // =========================================================================

    @Test
    public void givenValidHost_whenConnect_thenSshRepositoryConnectIsCalled() {
        // Given
        Host host = new Host(1L, "School Server", "root", "192.168.1.10", 22, "secret");

        // When
        connectHostUseCase.execute(host);

        // Then
        verify(mockSshRepository, times(1)).connect(host);
    }

    @Test
    public void givenValidHost_whenConnect_thenHostRepositoryIsNeverInvoked() {
        // Given
        Host host = new Host(1L, "School Server", "root", "192.168.1.10", 22, "secret");

        // When
        connectHostUseCase.execute(host);

        // Then — the execute() method only delegates to SshRepository; HostRepository is not involved
        verify(mockHostRepository, never()).getHostById(host.getId());
    }

    @Test
    public void givenRepositoryThrowsOnConnect_whenConnect_thenExceptionPropagates() {
        // Given
        Host host = new Host(1L, "School Server", "root", "192.168.1.10", 22, "wrongpass");
        org.mockito.Mockito.doThrow(new RuntimeException("Error connecting to ssh server"))
                .when(mockSshRepository).connect(host);

        // When & Then
        assertThrows(RuntimeException.class, () -> connectHostUseCase.execute(host));
    }


    // =========================================================================
    // CONNECT — getHostById()
    // =========================================================================

    @Test
    public void givenExistingHostId_whenGetHostById_thenReturnsCorrectHost() {
        // Given
        Host expected = new Host(3L, "Lab Server", "admin", "10.0.0.3", 22, null);
        when(mockHostRepository.getHostById(3L)).thenReturn(expected);

        // When
        Host result = connectHostUseCase.getHostById(3L);

        // Then
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Lab Server", result.getName());
        verify(mockHostRepository, times(1)).getHostById(3L);
    }

    @Test
    public void givenNonExistingHostId_whenGetHostById_thenReturnsNull() {
        // Given
        when(mockHostRepository.getHostById(999L)).thenReturn(null);

        // When
        Host result = connectHostUseCase.getHostById(999L);

        // Then
        org.junit.Assert.assertNull(result);
        verify(mockHostRepository, times(1)).getHostById(999L);
    }

    @Test
    public void givenAnyId_whenGetHostById_thenSshRepositoryIsNeverInvoked() {
        // Given
        when(mockHostRepository.getHostById(1L)).thenReturn(null);

        // When
        connectHostUseCase.getHostById(1L);

        // Then
        verify(mockSshRepository, never()).connect(org.mockito.ArgumentMatchers.any());
    }


    // =========================================================================
    // DISCONNECT
    // =========================================================================

    @Test
    public void whenDisconnect_thenSshRepositoryDisconnectIsCalled() {
        // Given — no precondition needed; disconnect is unconditional

        // When
        disconnectHostUseCase.execute();

        // Then
        verify(mockSshRepository, times(1)).disconnect();
    }

    @Test
    public void whenDisconnectCalledMultipleTimes_thenRepositoryIsCalledEachTime() {
        // Given — disconnect must be idempotent at the use-case level

        // When
        disconnectHostUseCase.execute();
        disconnectHostUseCase.execute();
        disconnectHostUseCase.execute();

        // Then
        verify(mockSshRepository, times(3)).disconnect();
    }

    @Test
    public void givenRepositoryThrowsOnDisconnect_whenDisconnect_thenExceptionPropagates() {
        // Given
        org.mockito.Mockito.doThrow(new RuntimeException("Error disconnecting"))
                .when(mockSshRepository).disconnect();

        // When & Then
        assertThrows(RuntimeException.class, () -> disconnectHostUseCase.execute());
    }


    // =========================================================================
    // LIST VMs
    // =========================================================================

    @Test
    public void givenRepositoryHasVMs_whenList_thenReturnsAllVMs() {
        // Given
        List<VirtualMachine> expected = Arrays.asList(
                new VirtualMachine(1, "ubuntu-20.04", "running"),
                new VirtualMachine(null, "centos-7", "shut off"),
                new VirtualMachine(2, "debian-11", "paused")
        );
        when(mockSshRepository.getAllVMs()).thenReturn(expected);

        // When
        List<VirtualMachine> result = listVMsUseCase.execute();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("ubuntu-20.04", result.get(0).getName());
        assertEquals("running", result.get(0).getState());
        assertEquals("centos-7", result.get(1).getName());
        assertEquals("shut off", result.get(1).getState());
        assertEquals("debian-11", result.get(2).getName());
        assertEquals("paused", result.get(2).getState());
    }

    @Test
    public void givenRepositoryIsEmpty_whenList_thenReturnsEmptyList() {
        // Given
        when(mockSshRepository.getAllVMs()).thenReturn(Collections.emptyList());

        // When
        List<VirtualMachine> result = listVMsUseCase.execute();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void givenRepositoryHasVMs_whenList_thenRepositoryIsCalledExactlyOnce() {
        // Given
        when(mockSshRepository.getAllVMs()).thenReturn(Collections.emptyList());

        // When
        listVMsUseCase.execute();

        // Then
        verify(mockSshRepository, times(1)).getAllVMs();
    }

    @Test
    public void givenRepositoryThrowsOnList_whenList_thenExceptionPropagates() {
        // Given — simulates a broken/closed SSH channel
        when(mockSshRepository.getAllVMs())
                .thenThrow(new RuntimeException("Error creating channel"));

        // When & Then
        assertThrows(RuntimeException.class, () -> listVMsUseCase.execute());
    }

    @Test
    public void givenRepositoryHasOneVM_whenList_thenIdCanBeNull() {
        // Given — VMs that are shut off have no numeric Id in virsh (represented as "-")
        List<VirtualMachine> expected = Collections.singletonList(
                new VirtualMachine(null, "centos-7", "shut off")
        );
        when(mockSshRepository.getAllVMs()).thenReturn(expected);

        // When
        List<VirtualMachine> result = listVMsUseCase.execute();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        org.junit.Assert.assertNull(result.get(0).getId());
        assertEquals("centos-7", result.get(0).getName());
    }

}
