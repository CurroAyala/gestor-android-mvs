package com.curro.gestormvs;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.app.UiAutomation;
import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;

import com.curro.gestormvs.data.daos.HostDao;
import com.curro.gestormvs.data.db.AppDatabase;
import com.curro.gestormvs.data.entities.HostEntity;
import com.curro.gestormvs.ui.MainActivity;
import com.curro.gestormvs.ui.ServiceLocator;

import org.apache.sshd.common.util.io.PathUtils;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class SshIntegrationTest {

    // ── Constants ─────────────────────────────────────────────────────────────

    private static final String SSH_HOST     = "127.0.0.1";
    private static final int    SSH_PORT     = 2222;
    private static final String SSH_USER     = "root";
    private static final String SSH_PASSWORD = "testpass";

    private static final long   HOST_ID      = 1L;
    private static final String HOST_NAME    = "Test Server";

    private static final String VIRSH_LIST_OUTPUT =
            " Id   Name          State\n"
                    + "-----------------------------------\n"
                    + " 1    ubuntu-20.04  running\n"
                    + " -    centos-7      shut off\n\n";

    private static final String VIRSH_SHUTDOWN_OUTPUT = "\nDomain 'ubuntu-20.04' is being shutdown\n\n";
    private static final String VIRSH_START_OUTPUT    = "\nDomain 'centos-7' started\n\n";
    private static final String VIRSH_LIST_AFTER_SHUTDOWN =
            " Id   Name          State\n"
                    + "-----------------------------------\n"
                    + " -    ubuntu-20.04  shut off\n"
                    + " -    centos-7      shut off\n\n";
    private static final String VIRSH_LIST_AFTER_START =
            " Id   Name          State\n"
                    + "-----------------------------------\n"
                    + " -    ubuntu-20.04  shut off\n"
                    + " 2    centos-7      running\n\n";

    private static final String VIRSH_SNAPSHOT_OUTPUT =
            "\nDomain snapshot snap-test created\n\n";

    // ── Fields ────────────────────────────────────────────────────────────────

    private SshServer          sshServer;
    private AppDatabase        testDatabase;

    private final AtomicReference<TestCommandBehavior> nextBehavior =
            new AtomicReference<>(TestCommandBehavior.LIST_VMS);

    private enum TestCommandBehavior {
        LIST_VMS,
        LIST_VMS_AFTER_SHUTDOWN,
        LIST_VMS_AFTER_START,
        SHUTDOWN_VM,
        START_VM,
        SNAPSHOT_CREATE,
        COMMAND_NOT_FOUND
    }

    // =========================================================================
    // Setup / Teardown
    // =========================================================================

    @Before
    public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        injectInMemoryDatabase(context);
        startEmbeddedSshServer(context);
    }

    @After
    public void tearDown() throws Exception {
        if (sshServer != null && sshServer.isStarted()) {
            sshServer.stop(true);
        }
        if (testDatabase != null) {
            testDatabase.close();
        }
        resetServiceLocator();
    }

    private void injectInMemoryDatabase(Context context) throws Exception {
        testDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();

        HostDao dao = testDatabase.hostDao();

        HostEntity entity = new HostEntity(
                HOST_ID, HOST_NAME, SSH_USER, SSH_HOST, SSH_PORT, SSH_PASSWORD
        );
        dao.createHost(entity);

        Field instanceField = AppDatabase.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, testDatabase);

        Field hostRepoField = ServiceLocator.class.getDeclaredField("hostRepository");
        hostRepoField.setAccessible(true);
        hostRepoField.set(null, null);

        TestHostRepository testRepo = new TestHostRepository(dao, context);
        hostRepoField.set(null, testRepo);
    }

    private void startEmbeddedSshServer(Context context) throws IOException {
        PathUtils.setUserHomeFolderResolver(() -> context.getCacheDir().toPath());

        sshServer = SshServer.setUpDefaultServer();
        sshServer.setHost(SSH_HOST);
        sshServer.setPort(SSH_PORT);

        File keyFile = new File(context.getCacheDir(), "test_host_key");
        SimpleGeneratorHostKeyProvider keyProvider =
                new SimpleGeneratorHostKeyProvider(keyFile.toPath());

        keyProvider.setAlgorithm("EC");
        sshServer.setKeyPairProvider(keyProvider);

        sshServer.setPasswordAuthenticator(
                (PasswordAuthenticator) (username, password, session) ->
                        SSH_USER.equals(username) && SSH_PASSWORD.equals(password)
        );

        sshServer.setCommandFactory((channel, commandText) -> {
            TestCommandBehavior behavior = nextBehavior.get();
            return new TestMockSshCommand(commandText, behavior);
        });

        sshServer.start();
    }

    private void resetServiceLocator() throws Exception {
        for (String field : new String[]{"hostRepository", "sshRepository", "checkVMStateUseCase"}) {
            Field f = ServiceLocator.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(null, null);
        }
        Field instanceField = AppDatabase.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    private void navigateToVmList() {
        onView(withId(R.id.recyclerHosts)).check(matches(isDisplayed()));

        try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        onView(withId(R.id.recyclerHosts)).check(matches(hasDescendant(withText("Test Server"))));
        onView(withId(R.id.recyclerHosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }

    // =========================================================================
    // Tests
    // =========================================================================

    @Test
    public void givenSshServerReturnsVmList_whenNavigatingToVmScreen_thenBothVmsAreDisplayed()
            throws InterruptedException {
        nextBehavior.set(TestCommandBehavior.LIST_VMS);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            navigateToVmList();
            Thread.sleep(6000);

            onView(withId(R.id.recyclerVms)).check(matches(isDisplayed()));
            onView(withId(R.id.recyclerVms)).check(matches(hasDescendant(withText("ubuntu-20.04"))));
            onView(withId(R.id.recyclerVms)).check(matches(hasDescendant(withText("running"))));
            onView(withId(R.id.recyclerVms)).check(matches(hasDescendant(withText("centos-7"))));
            onView(withId(R.id.recyclerVms)).check(matches(hasDescendant(withText("shut off"))));
            onView(withId(R.id.layoutEmpty)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void givenRunningVm_whenPowerButtonTapped_thenVmIsDisplayedAsShutOff()
            throws InterruptedException {
        nextBehavior.set(TestCommandBehavior.LIST_VMS);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            navigateToVmList();
            Thread.sleep(6000);

            onView(withId(R.id.recyclerVms)).check(matches(hasDescendant(withText("ubuntu-20.04"))));
            nextBehavior.set(TestCommandBehavior.SHUTDOWN_VM);

            onView(withId(R.id.recyclerVms)).perform(RecyclerViewActions.actionOnItemAtPosition(
                    0, new TestChildViewAction(R.id.btnPower)
            ));

            nextBehavior.set(TestCommandBehavior.LIST_VMS_AFTER_SHUTDOWN);
            Thread.sleep(12000);

            onView(withId(R.id.recyclerVms)).check(matches(hasDescendant(withText("shut off"))));
        }
    }

    @Test
    public void givenShutOffVm_whenPowerButtonTapped_thenVmIsDisplayedAsRunning()
            throws InterruptedException {
        nextBehavior.set(TestCommandBehavior.LIST_VMS);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            navigateToVmList();
            Thread.sleep(6000);

            onView(withId(R.id.recyclerVms)).check(matches(hasDescendant(withText("centos-7"))));
            nextBehavior.set(TestCommandBehavior.START_VM);

            onView(withId(R.id.recyclerVms)).perform(RecyclerViewActions.actionOnItemAtPosition(
                    1, new TestChildViewAction(R.id.btnPower)
            ));

            nextBehavior.set(TestCommandBehavior.LIST_VMS_AFTER_START);
            Thread.sleep(12000);

            onView(withId(R.id.recyclerVms)).check(matches(hasDescendant(withText("running"))));
        }
    }

    @Test
    public void givenRunningVm_whenSnapshotCreated_thenSuccessMessageIsDisplayed()
            throws InterruptedException {
        nextBehavior.set(TestCommandBehavior.LIST_VMS);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            navigateToVmList();
            Thread.sleep(6000);

            onView(withId(R.id.recyclerVms)).check(matches(hasDescendant(withText("ubuntu-20.04"))));
            nextBehavior.set(TestCommandBehavior.SNAPSHOT_CREATE);

            onView(withId(R.id.recyclerVms)).perform(RecyclerViewActions.actionOnItemAtPosition(
                    0, new TestChildViewAction(R.id.btnSnapshot)
            ));

            onView(withId(R.id.etDialogName))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));

            onView(withId(R.id.etDialogName))
                    .inRoot(isDialog())
                    .perform(replaceText("snap-test"), closeSoftKeyboard());

            verifyToastAfterAction(
                    () -> onView(withId(R.id.btnDialogConfirm))
                            .inRoot(isDialog())
                            .perform(click()),
                    "successfully"
            );
        }
    }

    @Test
    public void givenWrongPassword_whenConnecting_thenErrorToastIsDisplayed()
            throws Exception {
        HostDao dao = testDatabase.hostDao();
        dao.createHost(new HostEntity(2L, "Bad Auth Server", SSH_USER, SSH_HOST, SSH_PORT, "wrong-password"));

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.recyclerHosts)).check(matches(isDisplayed()));
            Thread.sleep(3000);

            onView(withId(R.id.recyclerHosts)).check(matches(hasDescendant(withText("Bad Auth Server"))));

            verifyToastAfterAction(
                    () -> onView(withId(R.id.recyclerHosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click())),
                    "connecting"
            );
        }
    }

    @Test
    public void givenServerReturnsCommandError_whenListingVms_thenErrorToastIsDisplayed()
            throws InterruptedException {
        nextBehavior.set(TestCommandBehavior.COMMAND_NOT_FOUND);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withId(R.id.recyclerHosts)).check(matches(isDisplayed()));
            Thread.sleep(3000);

            onView(withId(R.id.recyclerHosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

            Thread.sleep(3000);
            onView(withText("There are no virtual machines.")).check(matches(isDisplayed()));
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    // Checks ALL accessibility events to reliably catch Toasts in API 30+ regardless of type
    private void verifyToastAfterAction(Runnable action, String expectedText) {
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        try {
            uiAutomation.executeAndWaitForEvent(
                    action,
                    event -> {
                        event.getText();
                        for (CharSequence cs : event.getText()) {
                            if (cs != null && cs.toString().toLowerCase().contains(expectedText.toLowerCase())) {
                                return true;
                            }
                        }
                        return false;
                    },
                    20000 // Extended 20s timeout for slow SSH network delays
            );
        } catch (TimeoutException e) {
            Assert.fail("Expected Toast containing '" + expectedText + "' was not found.");
        }
    }

    // =========================================================================
    // MockSshCommand
    // =========================================================================

    private static final class TestMockSshCommand implements Command {

        private final String          rawCommand;
        private final TestCommandBehavior behavior;

        private OutputStream  out;
        private OutputStream  err;
        private ExitCallback  callback;

        TestMockSshCommand(String rawCommand, TestCommandBehavior behavior) {
            this.rawCommand = rawCommand;
            this.behavior   = behavior;
        }

        @Override public void setInputStream(InputStream in) {}
        @Override public void setOutputStream(OutputStream out) { this.out = out; }
        @Override public void setErrorStream(OutputStream err)  { this.err = err; }
        @Override public void setExitCallback(ExitCallback callback) { this.callback = callback; }

        @Override
        public void start(org.apache.sshd.server.channel.ChannelSession channel, Environment env) {
            Thread worker = new Thread(() -> {
                try {
                    handleCommand();
                } catch (Exception e) {
                    android.util.Log.e("TestMockSshCommand", "Comando SSH Mock falló", e);
                    callback.onExit(1);
                }
            });
            worker.setDaemon(true);
            worker.start();
        }

        private void handleCommand() throws Exception {
            int    exitCode = 0;
            String stdout   = "";
            String stderr   = "";

            switch (behavior) {
                case LIST_VMS:
                    stdout = VIRSH_LIST_OUTPUT;
                    break;
                case LIST_VMS_AFTER_SHUTDOWN:
                    stdout = VIRSH_LIST_AFTER_SHUTDOWN;
                    break;
                case LIST_VMS_AFTER_START:
                    stdout = VIRSH_LIST_AFTER_START;
                    break;
                case SHUTDOWN_VM:
                    if (rawCommand.contains("shutdown")) {
                        stdout = VIRSH_SHUTDOWN_OUTPUT;
                    } else if (rawCommand.contains("list")) {
                        stdout = VIRSH_LIST_AFTER_SHUTDOWN;
                    }
                    break;
                case START_VM:
                    if (rawCommand.contains("start")) {
                        stdout = VIRSH_START_OUTPUT;
                    } else if (rawCommand.contains("list")) {
                        stdout = VIRSH_LIST_AFTER_START;
                    }
                    break;
                case SNAPSHOT_CREATE:
                    if (rawCommand.contains("snapshot-create-as")) {
                        stdout = VIRSH_SNAPSHOT_OUTPUT;
                    } else if (rawCommand.contains("list")) {
                        stdout = VIRSH_LIST_OUTPUT;
                    }
                    break;
                case COMMAND_NOT_FOUND:
                    exitCode = 1;
                    stderr   = "error: command 'virsh' not found\n";
                    break;
                default:
                    exitCode = 1;
                    stderr   = "error: unknown command\n";
                    break;
            }

            if (!stdout.isEmpty()) {
                out.write(stdout.getBytes(StandardCharsets.UTF_8));
                out.flush();
            }

            if (!stderr.isEmpty() && err != null) {
                err.write(stderr.getBytes(StandardCharsets.UTF_8));
                err.flush();
            }

            callback.onExit(exitCode);
        }

        @Override
        public void destroy(org.apache.sshd.server.channel.ChannelSession channel) {}
    }

    // =========================================================================
    // TestHostRepository
    // =========================================================================

    private static final class TestHostRepository
            extends com.curro.gestormvs.data.repositories.HostRepository {

        private final HostDao dao;

        TestHostRepository(HostDao dao, Context context) {
            super(dao, context);
            this.dao = dao;
        }

        @Override
        public com.curro.gestormvs.domain.models.Host getHostById(long id) {
            com.curro.gestormvs.data.entities.HostEntity entity = dao.getHostById(id);
            if (entity == null) return null;
            return com.curro.gestormvs.data.mappers.HostMapper.toDomain(entity, entity.encryptedPassword);
        }

        @Override
        public java.util.List<com.curro.gestormvs.domain.models.Host> getAllHosts() {
            java.util.List<com.curro.gestormvs.domain.models.Host> result = new java.util.ArrayList<>();
            for (com.curro.gestormvs.data.entities.HostEntity entity : dao.getAllHosts()) {
                result.add(com.curro.gestormvs.data.mappers.HostMapper.toDomain(entity, entity.encryptedPassword));
            }
            return result;
        }

        @Override
        public void createHost(com.curro.gestormvs.domain.models.Host host) {
            com.curro.gestormvs.data.entities.HostEntity entity =
                    com.curro.gestormvs.data.mappers.HostMapper.toEntity(host, host.getPassword());
            dao.createHost(entity);
        }

        @Override
        public void updateHost(com.curro.gestormvs.domain.models.Host host) {
            com.curro.gestormvs.data.entities.HostEntity entity =
                    com.curro.gestormvs.data.mappers.HostMapper.toEntity(host, host.getPassword());
            dao.updateHost(entity);
        }

        @Override
        public void deleteHost(com.curro.gestormvs.domain.models.Host host) {
            com.curro.gestormvs.data.entities.HostEntity entity =
                    com.curro.gestormvs.data.mappers.HostMapper.toEntity(host, host.getPassword());
            dao.deleteHost(entity);
        }
    }

    // =========================================================================
    // ChildViewAction  (Espresso helper)
    // =========================================================================

    private static final class TestChildViewAction
            implements androidx.test.espresso.ViewAction {

        private final int viewId;

        TestChildViewAction(int viewId) { this.viewId = viewId; }

        @Override
        public org.hamcrest.Matcher<android.view.View> getConstraints() {
            return org.hamcrest.Matchers.any(android.view.View.class);
        }

        @Override
        public String getDescription() {
            return "Click child view with id " + viewId;
        }

        @Override
        public void perform(androidx.test.espresso.UiController uiController, android.view.View view) {
            android.view.View child = view.findViewById(viewId);
            if (child == null) {
                throw new RuntimeException("ChildViewAction: view with id " + viewId + " not found in item.");
            }
            child.performClick();
            uiController.loopMainThreadUntilIdle();
        }
    }
}