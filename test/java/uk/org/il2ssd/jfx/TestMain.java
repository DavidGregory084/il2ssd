package uk.org.il2ssd.jfx;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.loadui.testfx.exceptions.NoNodesFoundException;
import org.loadui.testfx.utils.FXTestUtils;
import uk.org.il2ssd.Core;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.Assertions.verifyThat;
import static org.loadui.testfx.controls.Commons.hasText;
import static org.loadui.testfx.controls.impl.EnabledMatcher.disabled;
import static org.loadui.testfx.controls.impl.EnabledMatcher.enabled;

/**
 * Main GUI Tests
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestMain {

    private static Il2SsdGuiTest controller;

    @BeforeClass
    public static void setUpClass() {
        FXTestUtils.launchApp(Core.class);
        controller = new Il2SsdGuiTest();
        controller.sleep(5, TimeUnit.SECONDS);
        controller.enterSettings();
    }

    @Test
    public void test1_ConnectDisconnect() {
        controller.click(Il2SsdGuiTest.consoleTab);
        // Verify initial state
        verifyThat(Il2SsdGuiTest.connectButton, is(enabled()));
        verifyThat(Il2SsdGuiTest.disconnectButton, is(disabled()));
        verifyThat(Il2SsdGuiTest.consoleTextArea, hasText("<disconnected>"));

        controller.connect();
        // Verify connected state
        verifyThat(Il2SsdGuiTest.connectButton, is(disabled()));
        verifyThat(Il2SsdGuiTest.disconnectButton, is(enabled()));
        verifyThat(Il2SsdGuiTest.consoleTextArea, not(hasText("<disconnected>")));

        controller.disconnect();
        // Verify disconnected state
        verifyThat(Il2SsdGuiTest.connectButton, is(enabled()));
        verifyThat(Il2SsdGuiTest.disconnectButton, is(disabled()));
        verifyThat(Il2SsdGuiTest.consoleTextArea, hasText("<disconnected>"));
    }

    @Test(expected = NoNodesFoundException.class)
    public void test2_Exit() {
        controller.click(Il2SsdGuiTest.fileMenu).click(Il2SsdGuiTest.exitMenuItem);
        assertNodeExists(Il2SsdGuiTest.fileMenu);
    }

}
