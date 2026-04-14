package com.curro.gestormvs.domain.useCases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.curro.gestormvs.data.repositories.SshRepository;
import com.curro.gestormvs.domain.models.Snapshot;
import com.curro.gestormvs.domain.models.VirtualMachine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@RunWith(MockitoJUnitRunner.class)
public class SnapshotUseCasesTest {

    @Mock
    private SshRepository mockSshRepository;

    private CreateSnapshotUseCase createSnapshotUseCase;
    private DeleteSnapshotUseCase deleteSnapshotUseCase;
    private ListSnapshotsUseCase listSnapshotsUseCase;
    private RevertSnapshotUseCase revertSnapshotUseCase;


    @Before
    public void setUp() {

        createSnapshotUseCase = new CreateSnapshotUseCase(mockSshRepository);
        deleteSnapshotUseCase = new DeleteSnapshotUseCase(mockSshRepository);
        listSnapshotsUseCase  = new ListSnapshotsUseCase(mockSshRepository);
        revertSnapshotUseCase = new RevertSnapshotUseCase(mockSshRepository);
    }


    // =========================================================================
    // CREATE SNAPSHOT — CreateSnapshotUseCase
    // =========================================================================

    @Test
    public void givenNullVm_whenCreateSnapshot_thenThrowsIllegalArgumentException() {
        // Given
        String snapshotName = "snap-before-update";

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> createSnapshotUseCase.execute(null, snapshotName));
    }

    @Test
    public void givenNullVm_whenCreateSnapshot_thenRepositoryIsNeverCalled() {
        // Given
        String snapshotName = "snap-before-update";

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> createSnapshotUseCase.execute(null, snapshotName));
        verify(mockSshRepository, never()).createSnapshot(null, snapshotName);
    }

    @Test
    public void givenNullSnapshotName_whenCreateSnapshot_thenThrowsIllegalArgumentException() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> createSnapshotUseCase.execute(vm, null));
    }

    @Test
    public void givenEmptySnapshotName_whenCreateSnapshot_thenThrowsIllegalArgumentException() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> createSnapshotUseCase.execute(vm, ""));
    }

    @Test
    public void givenSnapshotNameTooLong_whenCreateSnapshot_thenThrowsIllegalArgumentException() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        String tooLongName = "s".repeat(31);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> createSnapshotUseCase.execute(vm, tooLongName));
    }

    @Test
    public void givenSnapshotNameTooShort_whenCreateSnapshot_thenThrowsIllegalArgumentException() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> createSnapshotUseCase.execute(vm, "ab"));
    }

    @Test
    public void givenSnapshotNameWithBoundaryLength3_whenCreateSnapshot_thenRepositoryIsCalledOnce() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        String boundaryName = "abc"; // exactly 3 characters — minimum allowed

        // When
        createSnapshotUseCase.execute(vm, boundaryName);

        // Then
        verify(mockSshRepository, times(1)).createSnapshot(vm, boundaryName);
    }

    @Test
    public void givenSnapshotNameWithBoundaryLength30_whenCreateSnapshot_thenRepositoryIsCalledOnce() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        String boundaryName = "s".repeat(30); // exactly 30 characters — maximum allowed

        // When
        createSnapshotUseCase.execute(vm, boundaryName);

        // Then
        verify(mockSshRepository, times(1)).createSnapshot(vm, boundaryName);
    }

    @Test
    public void givenInvalidNameLength_whenCreateSnapshot_thenRepositoryIsNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        String tooLongName = "s".repeat(31);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> createSnapshotUseCase.execute(vm, tooLongName));
        verify(mockSshRepository, never()).createSnapshot(vm, tooLongName);
    }

    @Test
    public void givenCrashedVm_whenCreateSnapshot_thenThrowsIllegalArgumentException() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "ubuntu-20.04", "crashed");

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> createSnapshotUseCase.execute(vm, "valid-snap-name"));
    }

    @Test
    public void givenCrashedVm_whenCreateSnapshot_thenRepositoryIsNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "ubuntu-20.04", "crashed");
        String snapshotName = "valid-snap-name";

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> createSnapshotUseCase.execute(vm, snapshotName));
        verify(mockSshRepository, never()).createSnapshot(vm, snapshotName);
    }

    @Test
    public void givenValidRunningVm_whenCreateSnapshot_thenRepositoryIsCalledOnce() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        String snapshotName = "snap-before-update";

        // When
        createSnapshotUseCase.execute(vm, snapshotName);

        // Then
        verify(mockSshRepository, times(1)).createSnapshot(vm, snapshotName);
    }

    @Test
    public void givenValidShutOffVm_whenCreateSnapshot_thenRepositoryIsCalledOnce() {
        // Given — snapshots can also be taken from shut-off VMs
        VirtualMachine vm = new VirtualMachine(null, "centos-7", "shut off");
        String snapshotName = "cold-state-backup";

        // When
        createSnapshotUseCase.execute(vm, snapshotName);

        // Then
        verify(mockSshRepository, times(1)).createSnapshot(vm, snapshotName);
    }

    @Test
    public void givenValidVm_whenCreateSnapshotAndRepositoryThrows_thenExceptionPropagates() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        String snapshotName = "snap-before-update";
        doThrow(new RuntimeException("Snapshot already exists"))
                .when(mockSshRepository).createSnapshot(vm, snapshotName);

        // When & Then
        assertThrows(RuntimeException.class,
                () -> createSnapshotUseCase.execute(vm, snapshotName));
    }


    // =========================================================================
    // DELETE SNAPSHOT — DeleteSnapshotUseCase
    // =========================================================================

    @Test
    public void givenNullVm_whenDeleteSnapshot_thenThrowsIllegalArgumentException() {
        // Given
        Snapshot snapshot = new Snapshot("snap-before-update", "2024-01-15 10:00:00", "shutoff");

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> deleteSnapshotUseCase.execute(null, snapshot));
    }

    @Test
    public void givenNullVm_whenDeleteSnapshot_thenRepositoryIsNeverCalled() {
        // Given
        Snapshot snapshot = new Snapshot("snap-before-update", "2024-01-15 10:00:00", "shutoff");

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> deleteSnapshotUseCase.execute(null, snapshot));
        verify(mockSshRepository, never()).deleteSnapshot(null, snapshot);
    }

    @Test
    public void givenSnapshotWithNullName_whenDeleteSnapshot_thenThrowsIllegalArgumentException() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        Snapshot snapshot = new Snapshot(null, "2024-01-15 10:00:00", "running");

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> deleteSnapshotUseCase.execute(vm, snapshot));
    }

    @Test
    public void givenSnapshotWithEmptyName_whenDeleteSnapshot_thenThrowsIllegalArgumentException() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        Snapshot snapshot = new Snapshot("", "2024-01-15 10:00:00", "running");

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> deleteSnapshotUseCase.execute(vm, snapshot));
    }

    @Test
    public void givenSnapshotWithInvalidName_whenDeleteSnapshot_thenRepositoryIsNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        Snapshot snapshot = new Snapshot("", "2024-01-15 10:00:00", "running");

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> deleteSnapshotUseCase.execute(vm, snapshot));
        verify(mockSshRepository, never()).deleteSnapshot(vm, snapshot);
    }

    @Test
    public void givenValidVmAndSnapshot_whenDeleteSnapshot_thenRepositoryDeleteIsCalledOnce() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        Snapshot snapshot = new Snapshot("snap-before-update", "2024-01-15 10:00:00", "running");

        // When
        deleteSnapshotUseCase.execute(vm, snapshot);

        // Then
        verify(mockSshRepository, times(1)).deleteSnapshot(vm, snapshot);
    }

    @Test
    public void givenValidVmAndSnapshot_whenDeleteCalledTwice_thenRepositoryIsCalledTwice() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        Snapshot snapshot = new Snapshot("snap-before-update", "2024-01-15 10:00:00", "running");

        // When
        deleteSnapshotUseCase.execute(vm, snapshot);
        deleteSnapshotUseCase.execute(vm, snapshot);

        // Then
        verify(mockSshRepository, times(2)).deleteSnapshot(vm, snapshot);
    }

    @Test
    public void givenRepositoryThrows_whenDeleteSnapshot_thenExceptionPropagates() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        Snapshot snapshot = new Snapshot("snap-before-update", "2024-01-15 10:00:00", "running");
        doThrow(new RuntimeException("Access denied to virtual machine"))
                .when(mockSshRepository).deleteSnapshot(vm, snapshot);

        // When & Then
        assertThrows(RuntimeException.class,
                () -> deleteSnapshotUseCase.execute(vm, snapshot));
    }


    // =========================================================================
    // LIST SNAPSHOTS — ListSnapshotsUseCase
    // =========================================================================

    @Test
    public void givenNullVm_whenListSnapshots_thenThrowsIllegalArgumentException() {
        // Given — no VM provided

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> listSnapshotsUseCase.execute(null));
    }

    @Test
    public void givenNullVm_whenListSnapshots_thenRepositoryIsNeverCalled() {
        // Given — no VM provided

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> listSnapshotsUseCase.execute(null));
        verify(mockSshRepository, never()).getAllSnapshots(null);
    }

    @Test
    public void givenVmWithSnapshots_whenListSnapshots_thenReturnsAllSnapshots() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        List<Snapshot> expectedSnapshots = Arrays.asList(
                new Snapshot("snap-v1", "2024-01-10 09:00:00", "running"),
                new Snapshot("snap-v2", "2024-01-15 14:30:00", "shutoff"),
                new Snapshot("snap-v3", "2024-01-20 11:15:00", "running")
        );
        when(mockSshRepository.getAllSnapshots(vm)).thenReturn(expectedSnapshots);

        // When
        List<Snapshot> result = listSnapshotsUseCase.execute(vm);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("snap-v1", result.get(0).getName());
        assertEquals("snap-v2", result.get(1).getName());
        assertEquals("snap-v3", result.get(2).getName());
    }

    @Test
    public void givenVmWithNoSnapshots_whenListSnapshots_thenReturnsEmptyList() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        when(mockSshRepository.getAllSnapshots(vm)).thenReturn(Collections.emptyList());

        // When
        List<Snapshot> result = listSnapshotsUseCase.execute(vm);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void givenValidVm_whenListSnapshots_thenRepositoryIsCalledExactlyOnce() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        when(mockSshRepository.getAllSnapshots(vm)).thenReturn(Collections.emptyList());

        // When
        listSnapshotsUseCase.execute(vm);

        // Then
        verify(mockSshRepository, times(1)).getAllSnapshots(vm);
    }

    @Test
    public void givenRepositoryThrows_whenListSnapshots_thenExceptionPropagates() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        when(mockSshRepository.getAllSnapshots(vm))
                .thenThrow(new RuntimeException("Error creating channel"));

        // When & Then
        assertThrows(RuntimeException.class, () -> listSnapshotsUseCase.execute(vm));
    }

    @Test
    public void givenShutOffVm_whenListSnapshots_thenReturnsSnapshotsCorrectly() {
        // Given — listing works regardless of the VM state
        VirtualMachine vm = new VirtualMachine(null, "centos-7", "shut off");
        List<Snapshot> expectedSnapshots = Collections.singletonList(
                new Snapshot("cold-backup", "2024-02-01 08:00:00", "shutoff")
        );
        when(mockSshRepository.getAllSnapshots(vm)).thenReturn(expectedSnapshots);

        // When
        List<Snapshot> result = listSnapshotsUseCase.execute(vm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("cold-backup", result.get(0).getName());
    }


    // =========================================================================
    // REVERT SNAPSHOT — RevertSnapshotUseCase
    // =========================================================================

    @Test
    public void givenCrashedVm_whenRevertSnapshot_thenThrowsIllegalArgumentException() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "ubuntu-20.04", "crashed");
        Snapshot snapshot = new Snapshot("snap-v1", "2024-01-10 09:00:00", "running");

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> revertSnapshotUseCase.execute(vm, snapshot));
    }

    @Test
    public void givenCrashedVm_whenRevertSnapshot_thenRepositoryIsNeverCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(null, "ubuntu-20.04", "crashed");
        Snapshot snapshot = new Snapshot("snap-v1", "2024-01-10 09:00:00", "running");

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> revertSnapshotUseCase.execute(vm, snapshot));
        verify(mockSshRepository, never()).reverSnapshot(vm, snapshot);
    }

    @Test
    public void givenRunningVmAndValidSnapshot_whenRevertSnapshot_thenReverSnapshotIsCalled() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        Snapshot snapshot = new Snapshot("snap-v1", "2024-01-10 09:00:00", "running");

        // When
        revertSnapshotUseCase.execute(vm, snapshot);

        // Then
        verify(mockSshRepository, times(1)).reverSnapshot(vm, snapshot);
    }

    @Test
    public void givenShutOffVmAndValidSnapshot_whenRevertSnapshot_thenReverSnapshotIsCalled() {
        // Given — reverting is allowed from "shut off" state
        VirtualMachine vm = new VirtualMachine(null, "centos-7", "shut off");
        Snapshot snapshot = new Snapshot("cold-backup", "2024-02-01 08:00:00", "shutoff");

        // When
        revertSnapshotUseCase.execute(vm, snapshot);

        // Then
        verify(mockSshRepository, times(1)).reverSnapshot(vm, snapshot);
    }

    @Test
    public void givenPausedVmAndValidSnapshot_whenRevertSnapshot_thenReverSnapshotIsCalled() {
        // Given — reverting is also allowed from "paused" state
        VirtualMachine vm = new VirtualMachine(2, "debian-11", "paused");
        Snapshot snapshot = new Snapshot("snap-v2", "2024-01-15 14:30:00", "paused");

        // When
        revertSnapshotUseCase.execute(vm, snapshot);

        // Then
        verify(mockSshRepository, times(1)).reverSnapshot(vm, snapshot);
    }

    @Test
    public void givenRepositoryThrows_whenRevertSnapshot_thenExceptionPropagates() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        Snapshot snapshot = new Snapshot("snap-v1", "2024-01-10 09:00:00", "running");
        doThrow(new RuntimeException("Access denied to virtual machine"))
                .when(mockSshRepository).reverSnapshot(vm, snapshot);

        // When & Then
        assertThrows(RuntimeException.class,
                () -> revertSnapshotUseCase.execute(vm, snapshot));
    }

    @Test
    public void givenValidVmAndSnapshot_whenRevertCalledTwice_thenRepositoryIsCalledTwice() {
        // Given
        VirtualMachine vm = new VirtualMachine(1, "ubuntu-20.04", "running");
        Snapshot snapshot = new Snapshot("snap-v1", "2024-01-10 09:00:00", "running");

        // When
        revertSnapshotUseCase.execute(vm, snapshot);
        revertSnapshotUseCase.execute(vm, snapshot);

        // Then
        verify(mockSshRepository, times(2)).reverSnapshot(vm, snapshot);
    }

}
