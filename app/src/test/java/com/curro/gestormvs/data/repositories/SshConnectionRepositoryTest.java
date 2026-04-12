package com.curro.gestormvs.data.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.util.Log;

import com.curro.gestormvs.domain.models.Host;
import com.curro.gestormvs.domain.models.VirtualMachine;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SshConnectionRepositoryTest {

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Typical output produced by {@code virsh list --all}. */
    private static final String VIRSH_OUTPUT_THREE_VMS =
            " Id   Name          State\n"
                    + "-----------------------------------\n"
                    + " 1    ubuntu-20.04  running\n"
                    + " -    centos-7      shut off\n"
                    + " 2    debian-11     paused\n";

    private static final String VIRSH_OUTPUT_EMPTY =
            " Id   Name   State\n"
                    + "-------------------\n";

    private static final String VIRSH_OUTPUT_RUNNING_ONLY =
            " Id   Name          State\n"
                    + "-----------------------------------\n"
                    + " 1    ubuntu-20.04  running\n";

    /** Injects a pre-built {@link Session} mock into the private {@code session} field. */
    private void injectSession(SshRepository repo, Session session) throws Exception {
        Field sessionField = SshRepository.class.getDeclaredField("session");
        sessionField.setAccessible(true);
        sessionField.set(repo, session);
    }

    /** Builds a {@link Host} with the given password (may be null). */
    private Host buildHost(String password) {
        return new Host(1L, "School Server", "root", "192.168.1.10", 22, password);
    }

    /** Creates an {@link InputStream} from a plain String, simulating SSH channel output. */
    private InputStream streamOf(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }


    // =========================================================================
    // connect()
    // =========================================================================

    @Test
    public void givenValidHost_whenConnect_thenSessionIsCreatedAndConnected() throws Exception {
        // Given
        Host host = buildHost("secret");
        Session mockSession = Mockito.mock(Session.class);

        try (MockedConstruction<JSch> mockedJsch = mockConstruction(JSch.class,
                (mock, ctx) -> when(mock.getSession(
                        eq(host.getUser()), eq(host.getIp()), eq(host.getPort())))
                        .thenReturn(mockSession))) {

            SshRepository repository = new SshRepository();

            // When
            repository.connect(host);

            // Then
            assertEquals(1, mockedJsch.constructed().size());
            verify(mockSession, times(1)).setPassword(host.getPassword());
            verify(mockSession, times(1)).setConfig(any(java.util.Properties.class));
            verify(mockSession, times(1)).connect(10000);
        }
    }

    @Test
    public void givenValidHost_whenConnect_thenStrictHostKeyCheckingIsDisabled() throws Exception {
        // Given
        Host host = buildHost("secret");
        Session mockSession = Mockito.mock(Session.class);

        try (MockedConstruction<JSch> mockedJsch = mockConstruction(JSch.class,
                (mock, ctx) -> when(mock.getSession(anyString(), anyString(), anyInt()))
                        .thenReturn(mockSession))) {

            SshRepository repository = new SshRepository();

            // When
            repository.connect(host);

            // Then — verify that a Properties object was passed (containing StrictHostKeyChecking=no)
            org.mockito.ArgumentCaptor<java.util.Properties> captor =
                    org.mockito.ArgumentCaptor.forClass(java.util.Properties.class);
            verify(mockSession).setConfig(captor.capture());
            assertEquals("no", captor.getValue().getProperty("StrictHostKeyChecking"));
        }
    }

    @Test
    public void givenConnectionError_whenConnect_thenThrowsRuntimeException() throws Exception {
        // Given
        Host host = buildHost("wrongpass");
        Session mockSession = Mockito.mock(Session.class);

        try (MockedConstruction<JSch> ignored = mockConstruction(JSch.class,
                (mock, ctx) -> {
                    when(mock.getSession(anyString(), anyString(), anyInt()))
                            .thenReturn(mockSession);
                    doThrow(new JSchException("Auth fail"))
                            .when(mockSession).connect(anyInt());
                })) {

            SshRepository repository = new SshRepository();

            // When & Then
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> repository.connect(host));
            assertEquals("Error connecting to ssh server", ex.getMessage());
        }
    }

    @Test
    public void givenJSchCannotCreateSession_whenConnect_thenThrowsRuntimeException()
            throws Exception {
        // Given
        Host host = buildHost("secret");

        try (MockedConstruction<JSch> ignored = mockConstruction(JSch.class,
                (mock, ctx) -> doThrow(new JSchException("Unknown host"))
                        .when(mock).getSession(anyString(), anyString(), anyInt()))) {

            SshRepository repository = new SshRepository();

            // When & Then
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> repository.connect(host));
            assertEquals("Error creating ssh session", ex.getMessage());
        }
    }

    @Test
    public void givenAlreadyConnectedSession_whenConnectCalledAgain_thenPreviousSessionIsDisconnected()
            throws Exception {
        // Given — first connection
        Host host = buildHost("secret");
        Session firstSession  = Mockito.mock(Session.class);
        Session secondSession = Mockito.mock(Session.class);
        when(firstSession.isConnected()).thenReturn(true);

        try (MockedConstruction<JSch> mockedJsch = mockConstruction(JSch.class,
                (mock, ctx) -> {
                    // Return a different Session on each getSession() call
                    when(mock.getSession(anyString(), anyString(), anyInt()))
                            .thenReturn(firstSession)
                            .thenReturn(secondSession);
                })) {

            SshRepository repository = new SshRepository();
            repository.connect(host); // establishes firstSession

            // When — connect again (should disconnect the first)
            repository.connect(host);

            // Then
            verify(firstSession, times(1)).disconnect();
        }
    }


    // =========================================================================
    // disconnect()
    // =========================================================================

    @Test
    public void givenActiveSession_whenDisconnect_thenSessionIsDisconnected() throws Exception {
        // Given
        Session mockSession = Mockito.mock(Session.class);
        when(mockSession.isConnected()).thenReturn(true);

        SshRepository repository = new SshRepository();
        injectSession(repository, mockSession);

        // When
        repository.disconnect();

        // Then
        verify(mockSession, times(1)).disconnect();
    }

    @Test
    public void givenNoActiveSession_whenDisconnect_thenNoExceptionIsThrown() {
        // Given — session is null (never connected)
        SshRepository repository = new SshRepository();

        // When & Then — must not throw NullPointerException
        repository.disconnect();
    }

    @Test
    public void givenAlreadyDisconnectedSession_whenDisconnect_thenSessionDisconnectIsNeverCalled()
            throws Exception {
        // Given
        Session mockSession = Mockito.mock(Session.class);
        when(mockSession.isConnected()).thenReturn(false);

        SshRepository repository = new SshRepository();
        injectSession(repository, mockSession);

        // When
        repository.disconnect();

        // Then
        verify(mockSession, never()).disconnect();
    }


    // =========================================================================
    // getAllVMs()
    // =========================================================================

    @Test
    public void givenTypicalVirshOutput_whenGetAllVMs_thenReturnsCorrectlyParsedList()
            throws Exception {
        // Given
        Session mockSession    = Mockito.mock(Session.class);
        ChannelExec mockChannel = Mockito.mock(ChannelExec.class);

        when(mockSession.openChannel("exec")).thenReturn(mockChannel);
        when(mockChannel.getInputStream()).thenReturn(streamOf(VIRSH_OUTPUT_THREE_VMS));
        when(mockChannel.isConnected()).thenReturn(true);

        SshRepository repository = new SshRepository();
        injectSession(repository, mockSession);

        try (MockedStatic<Log> ignoredLog = mockStatic(Log.class)) {

            // When
            List<VirtualMachine> result = repository.getAllVMs();

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());

            assertEquals("ubuntu-20.04", result.get(0).getName());
            assertEquals("running",      result.get(0).getState());
            assertEquals(Integer.valueOf(1), result.get(0).getId());

            assertEquals("centos-7",  result.get(1).getName());
            assertEquals("shut off",  result.get(1).getState());
            org.junit.Assert.assertNull(result.get(1).getId()); // "-" maps to null

            assertEquals("debian-11", result.get(2).getName());
            assertEquals("paused",    result.get(2).getState());
            assertEquals(Integer.valueOf(2), result.get(2).getId());
        }
    }

    @Test
    public void givenEmptyVirshOutput_whenGetAllVMs_thenReturnsEmptyList() throws Exception {
        // Given
        Session mockSession     = Mockito.mock(Session.class);
        ChannelExec mockChannel = Mockito.mock(ChannelExec.class);

        when(mockSession.openChannel("exec")).thenReturn(mockChannel);
        when(mockChannel.getInputStream()).thenReturn(streamOf(VIRSH_OUTPUT_EMPTY));
        when(mockChannel.isConnected()).thenReturn(true);

        SshRepository repository = new SshRepository();
        injectSession(repository, mockSession);

        try (MockedStatic<Log> ignoredLog = mockStatic(Log.class)) {

            // When
            List<VirtualMachine> result = repository.getAllVMs();

            // Then
            assertNotNull(result);
            assertEquals(0, result.size());
        }
    }

    @Test
    public void givenSingleRunningVM_whenGetAllVMs_thenReturnsSingleEntry() throws Exception {
        // Given
        Session mockSession     = Mockito.mock(Session.class);
        ChannelExec mockChannel = Mockito.mock(ChannelExec.class);

        when(mockSession.openChannel("exec")).thenReturn(mockChannel);
        when(mockChannel.getInputStream()).thenReturn(streamOf(VIRSH_OUTPUT_RUNNING_ONLY));
        when(mockChannel.isConnected()).thenReturn(true);

        SshRepository repository = new SshRepository();
        injectSession(repository, mockSession);

        try (MockedStatic<Log> ignoredLog = mockStatic(Log.class)) {

            // When
            List<VirtualMachine> result = repository.getAllVMs();

            // Then
            assertEquals(1, result.size());
            assertEquals("ubuntu-20.04",  result.get(0).getName());
            assertEquals("running",        result.get(0).getState());
            assertEquals(Integer.valueOf(1), result.get(0).getId());
        }
    }

    @Test
    public void givenChannelOpenFails_whenGetAllVMs_thenThrowsRuntimeException() throws Exception {
        // Given
        Session mockSession = Mockito.mock(Session.class);
        when(mockSession.openChannel("exec")).thenThrow(new JSchException("Channel error"));

        SshRepository repository = new SshRepository();
        injectSession(repository, mockSession);

        try (MockedStatic<Log> ignoredLog = mockStatic(Log.class)) {

            // When & Then
            RuntimeException ex = assertThrows(RuntimeException.class,
                    repository::getAllVMs);
            assertEquals("Error creating channel", ex.getMessage());
        }
    }

    @Test
    public void givenChannelConnects_whenGetAllVMs_thenChannelIsAlwaysDisconnectedOnSuccess()
            throws Exception {
        // Given
        Session mockSession     = Mockito.mock(Session.class);
        ChannelExec mockChannel = Mockito.mock(ChannelExec.class);

        when(mockSession.openChannel("exec")).thenReturn(mockChannel);
        when(mockChannel.getInputStream()).thenReturn(streamOf(VIRSH_OUTPUT_RUNNING_ONLY));
        when(mockChannel.isConnected()).thenReturn(true);

        SshRepository repository = new SshRepository();
        injectSession(repository, mockSession);

        try (MockedStatic<Log> ignoredLog = mockStatic(Log.class)) {

            // When
            repository.getAllVMs();

            // Then — the finally-block must always disconnect the channel
            verify(mockChannel, times(1)).disconnect();
        }
    }

    @Test
    public void givenChannelThrows_whenGetAllVMs_thenChannelIsAlwaysDisconnectedOnFailure()
            throws Exception {
        // Given
        Session mockSession     = Mockito.mock(Session.class);
        ChannelExec mockChannel = Mockito.mock(ChannelExec.class);

        when(mockSession.openChannel("exec")).thenReturn(mockChannel);
        doThrow(new JSchException("connect failed")).when(mockChannel).connect();
        when(mockChannel.isConnected()).thenReturn(true);

        SshRepository repository = new SshRepository();
        injectSession(repository, mockSession);

        try (MockedStatic<Log> ignoredLog = mockStatic(Log.class)) {

            // When & Then
            assertThrows(RuntimeException.class, repository::getAllVMs);

            // Then — the finally-block must still disconnect the channel
            verify(mockChannel, times(1)).disconnect();
        }
    }

    @Test
    public void givenVirshOutput_whenGetAllVMs_thenCorrectCommandIsSentToChannel()
            throws Exception {
        // Given
        Session mockSession     = Mockito.mock(Session.class);
        ChannelExec mockChannel = Mockito.mock(ChannelExec.class);

        when(mockSession.openChannel("exec")).thenReturn(mockChannel);
        when(mockChannel.getInputStream()).thenReturn(streamOf(VIRSH_OUTPUT_EMPTY));
        when(mockChannel.isConnected()).thenReturn(false);

        SshRepository repository = new SshRepository();
        injectSession(repository, mockSession);

        try (MockedStatic<Log> ignoredLog = mockStatic(Log.class)) {

            // When
            repository.getAllVMs();

            // Then — the exact virsh command must be sent
            verify(mockChannel, times(1))
                    .setCommand("virsh --connect qemu:///system list --all");
        }
    }

}
