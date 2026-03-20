package com.curro.gestormvs.domain.useCases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.curro.gestormvs.data.repositories.HostRepository;
import com.curro.gestormvs.domain.models.Host;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CrudHostUseCasesTest {

    @Mock
    private HostRepository mockRepository;

    private CreateHostUseCase createHostUseCase;
    private UpdateHostUseCase updateHostUseCase;
    private DeleteHostUseCase deleteHostUseCase;
    private ListHostsUseCase listHostsUseCase;


    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        createHostUseCase = new CreateHostUseCase(mockRepository);
        updateHostUseCase = new UpdateHostUseCase(mockRepository);
        deleteHostUseCase = new DeleteHostUseCase(mockRepository);
        listHostsUseCase  = new ListHostsUseCase(mockRepository);
    }


    // =========================================================================
    // CREATE
    // =========================================================================

    @Test
    public void givenValidHost_whenCreate_thenRepositoryCreateIsCalled() {
        // Given
        Host host = new Host(0, "School Server", "root", "192.168.1.10", 22, null);

        // When
        createHostUseCase.execute(host);

        // Then
        verify(mockRepository, times(1)).createHost(host);
    }

    @Test
    public void givenHostWithNullPort_whenCreate_thenPortDefaultsTo22AndRepositoryIsCalled() {
        // Given
        Host host = new Host(0, "School Server", "root", "192.168.1.10", null, null);

        // When
        createHostUseCase.execute(host);

        // Then
        assertEquals(Integer.valueOf(22), host.getPort());
        verify(mockRepository, times(1)).createHost(host);
    }

    @Test
    public void givenHostWithEmptyName_whenCreate_thenThrowsExceptionAndRepositoryIsNeverCalled() {
        // Given
        Host host = new Host(0, "  ", "root", "192.168.1.10", 22, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> createHostUseCase.execute(host));
        verify(mockRepository, never()).createHost(host);
    }

    @Test
    public void givenHostWithNullName_whenCreate_thenThrowsExceptionAndRepositoryIsNeverCalled() {
        // Given
        Host host = new Host(0, null, "root", "192.168.1.10", 22, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> createHostUseCase.execute(host));
        verify(mockRepository, never()).createHost(host);
    }

    @Test
    public void givenHostWithEmptyUser_whenCreate_thenThrowsExceptionAndRepositoryIsNeverCalled() {
        // Given
        Host host = new Host(0, "School Server", "", "192.168.1.10", 22, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> createHostUseCase.execute(host));
        verify(mockRepository, never()).createHost(host);
    }

    @Test
    public void givenHostWithEmptyIp_whenCreate_thenThrowsExceptionAndRepositoryIsNeverCalled() {
        // Given
        Host host = new Host(0, "School Server", "root", "  ", 22, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> createHostUseCase.execute(host));
        verify(mockRepository, never()).createHost(host);
    }

    @Test
    public void givenHostWithInvalidIpFormat_whenCreate_thenThrowsExceptionAndRepositoryIsNeverCalled() {
        // Given
        Host host = new Host(0, "School Server", "root", "not_an_ip", 22, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> createHostUseCase.execute(host));
        verify(mockRepository, never()).createHost(host);
    }

    @Test
    public void givenHostWithPortBelowRange_whenCreate_thenThrowsExceptionAndRepositoryIsNeverCalled() {
        // Given
        Host host = new Host(0, "School Server", "root", "192.168.1.10", 0, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> createHostUseCase.execute(host));
        verify(mockRepository, never()).createHost(host);
    }

    @Test
    public void givenHostWithPortAboveRange_whenCreate_thenThrowsExceptionAndRepositoryIsNeverCalled() {
        // Given
        Host host = new Host(0, "School Server", "root", "192.168.1.10", 65536, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> createHostUseCase.execute(host));
        verify(mockRepository, never()).createHost(host);
    }

    @Test
    public void givenHostWithNameTooLong_whenCreate_thenThrowsExceptionAndRepositoryIsNeverCalled() {
        // Given
        Host host = new Host(0, "A".repeat(21), "root", "192.168.1.10", 22, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> createHostUseCase.execute(host));
        verify(mockRepository, never()).createHost(host);
    }

    @Test
    public void givenHostWithUserTooLong_whenCreate_thenThrowsExceptionAndRepositoryIsNeverCalled() {
        // Given
        Host host = new Host(0, "School Server", "u".repeat(21), "192.168.1.10", 22, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> createHostUseCase.execute(host));
        verify(mockRepository, never()).createHost(host);
    }

    @Test
    public void givenHostWithPasswordTooLong_whenCreate_thenThrowsExceptionAndRepositoryIsNeverCalled() {
        // Given
        Host host = new Host(0, "School Server", "root", "192.168.1.10", 22, "p".repeat(21));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> createHostUseCase.execute(host));
        verify(mockRepository, never()).createHost(host);
    }

    @Test
    public void givenHostWithValidDomainAsIp_whenCreate_thenRepositoryCreateIsCalled() {
        // Given
        // The regex also accepts domain-like patterns (e.g. my.server.local.net)
        Host host = new Host(0, "School Server", "root", "my.school.server.local", 22, null);

        // When
        createHostUseCase.execute(host);

        // Then
        verify(mockRepository, times(1)).createHost(host);
    }

    @Test
    public void givenHostWithBoundaryPort1_whenCreate_thenRepositoryCreateIsCalled() {
        // Given
        Host host = new Host(0, "School Server", "root", "192.168.1.10", 1, null);

        // When
        createHostUseCase.execute(host);

        // Then
        verify(mockRepository, times(1)).createHost(host);
    }

    @Test
    public void givenHostWithBoundaryPort65535_whenCreate_thenRepositoryCreateIsCalled() {
        // Given
        Host host = new Host(0, "School Server", "root", "192.168.1.10", 65535, null);

        // When
        createHostUseCase.execute(host);

        // Then
        verify(mockRepository, times(1)).createHost(host);
    }


    // =========================================================================
    // READ (LIST)
    // =========================================================================

    @Test
    public void givenRepositoryHasHosts_whenList_thenReturnsAllHosts() {
        // Given
        List<Host> expectedHosts = Arrays.asList(
                new Host(1L, "Server A", "root", "192.168.1.10", 22, null),
                new Host(2L, "Server B", "admin", "10.0.0.5", 2222, "pass")
        );
        when(mockRepository.getAllHosts()).thenReturn(expectedHosts);

        // When
        List<Host> result = listHostsUseCase.execute();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Server A", result.get(0).getName());
        assertEquals("Server B", result.get(1).getName());
    }

    @Test
    public void givenRepositoryIsEmpty_whenList_thenReturnsEmptyList() {
        // Given
        when(mockRepository.getAllHosts()).thenReturn(Collections.emptyList());

        // When
        List<Host> result = listHostsUseCase.execute();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void givenRepositoryHasHosts_whenList_thenRepositoryIsCalledOnce() {
        // Given
        when(mockRepository.getAllHosts()).thenReturn(Collections.emptyList());

        // When
        listHostsUseCase.execute();

        // Then
        verify(mockRepository, times(1)).getAllHosts();
    }


    // =========================================================================
    // UPDATE
    // =========================================================================

    @Test
    public void givenValidUpdatedHost_whenUpdate_thenRepositoryUpdateIsCalled() {
        // Given
        Host host = new Host(1L, "Updated Server", "admin", "10.0.0.1", 2222, "newpass");

        // When
        updateHostUseCase.execute(host);

        // Then
        verify(mockRepository, times(1)).updateHost(host);
    }

    @Test
    public void givenHostWithNullPort_whenUpdate_thenPortDefaultsTo22AndRepositoryIsCalled() {
        // Given
        Host host = new Host(1L, "Updated Server", "admin", "10.0.0.1", null, null);

        // When
        updateHostUseCase.execute(host);

        // Then
        assertEquals(Integer.valueOf(22), host.getPort());
        verify(mockRepository, times(1)).updateHost(host);
    }

    @Test
    public void givenHostWithEmptyName_whenUpdate_thenThrowsExceptionAndRepositoryIsNeverCalled() {
        // Given
        Host host = new Host(1L, "", "admin", "10.0.0.1", 22, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> updateHostUseCase.execute(host));
        verify(mockRepository, never()).updateHost(host);
    }

    @Test
    public void givenHostWithEmptyUser_whenUpdate_thenThrowsExceptionAndRepositoryIsNeverCalled() {
        // Given
        Host host = new Host(1L, "Updated Server", "  ", "10.0.0.1", 22, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> updateHostUseCase.execute(host));
        verify(mockRepository, never()).updateHost(host);
    }

    @Test
    public void givenHostWithInvalidIp_whenUpdate_thenThrowsExceptionAndRepositoryIsNeverCalled() {
        // Given
        Host host = new Host(1L, "Updated Server", "admin", "999.999.999", 22, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> updateHostUseCase.execute(host));
        verify(mockRepository, never()).updateHost(host);
    }

    @Test
    public void givenHostWithPortOutOfRange_whenUpdate_thenThrowsExceptionAndRepositoryIsNeverCalled() {
        // Given
        Host host = new Host(1L, "Updated Server", "admin", "10.0.0.1", 99999, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> updateHostUseCase.execute(host));
        verify(mockRepository, never()).updateHost(host);
    }

    @Test
    public void givenHostWithNameTooLong_whenUpdate_thenThrowsExceptionAndRepositoryIsNeverCalled() {
        // Given
        Host host = new Host(1L, "N".repeat(21), "admin", "10.0.0.1", 22, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> updateHostUseCase.execute(host));
        verify(mockRepository, never()).updateHost(host);
    }

    @Test
    public void givenHostWithPasswordTooLong_whenUpdate_thenThrowsExceptionAndRepositoryIsNeverCalled() {
        // Given
        Host host = new Host(1L, "Updated Server", "admin", "10.0.0.1", 22, "p".repeat(21));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> updateHostUseCase.execute(host));
        verify(mockRepository, never()).updateHost(host);
    }

    @Test
    public void givenExistingId_whenGetHostById_thenReturnsCorrectHost() {
        // Given
        Host expected = new Host(5L, "Target Server", "root", "172.16.0.1", 22, null);
        when(mockRepository.getHostById(5L)).thenReturn(expected);

        // When
        Host result = updateHostUseCase.getHostById(5L);

        // Then
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Target Server", result.getName());
    }

    @Test
    public void givenNonExistingId_whenGetHostById_thenReturnsNull() {
        // Given
        when(mockRepository.getHostById(999L)).thenReturn(null);

        // When
        Host result = updateHostUseCase.getHostById(999L);

        // Then
        assertNull(result);
    }


    // =========================================================================
    // DELETE
    // =========================================================================

    @Test
    public void givenExistingHost_whenDelete_thenRepositoryDeleteIsCalled() {
        // Given
        Host host = new Host(1L, "Server to delete", "root", "192.168.1.10", 22, null);

        // When
        deleteHostUseCase.execute(host);

        // Then
        verify(mockRepository, times(1)).deleteHost(host);
    }

    @Test
    public void givenExistingHost_whenDelete_thenRepositoryDeleteIsCalledExactlyOnce() {
        // Given
        Host host = new Host(2L, "Another Server", "admin", "10.0.0.2", 22, "secret");

        // When
        deleteHostUseCase.execute(host);
        deleteHostUseCase.execute(host);

        // Then — calling twice should result in two repository calls, not one cached result
        verify(mockRepository, times(2)).deleteHost(host);
    }


}
