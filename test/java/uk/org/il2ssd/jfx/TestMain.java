package uk.org.il2ssd.jfx;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.loadui.testfx.exceptions.NoNodesFoundException;
import org.loadui.testfx.utils.FXTestUtils;
import uk.org.il2ssd.Core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.loadui.testfx.Assertions.verifyThat;
import static org.loadui.testfx.GuiTest.waitUntil;
import static org.loadui.testfx.controls.Commons.hasText;
import static org.loadui.testfx.controls.impl.EnabledMatcher.disabled;
import static org.loadui.testfx.controls.impl.EnabledMatcher.enabled;
import static org.loadui.testfx.controls.impl.NodeExistsMatcher.exists;
import static org.loadui.testfx.controls.impl.VisibleNodesMatcher.visible;

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
        waitUntil(Il2SsdGuiTest.settingsTab, is(visible()));
        controller.enterSettings();
    }

    @Test
    public void test1_ConnectDisconnect() {
        // Verify initial state
        controller.click(Il2SsdGuiTest.consoleTab);
        verifyThat(Il2SsdGuiTest.connectButton, is(enabled()));
        verifyThat(Il2SsdGuiTest.disconnectButton, is(disabled()));
        verifyThat(Il2SsdGuiTest.consoleTextArea, hasText("<disconnected>"));
        // Verify connected state
        controller.connect();
        verifyThat(Il2SsdGuiTest.connectButton, is(disabled()));
        verifyThat(Il2SsdGuiTest.disconnectButton, is(enabled()));
        verifyThat(Il2SsdGuiTest.consoleTextArea, not(hasText("<disconnected>")));
        // Verify disconnected state
        controller.disconnect();
        verifyThat(Il2SsdGuiTest.connectButton, is(enabled()));
        verifyThat(Il2SsdGuiTest.disconnectButton, is(disabled()));
        verifyThat(Il2SsdGuiTest.consoleTextArea, hasText("<disconnected>"));
    }

    @Test(expected = NoNodesFoundException.class)
    public void test2_Exit() {
        controller.click(Il2SsdGuiTest.fileMenu).click(Il2SsdGuiTest.exitMenuItem);
        verifyThat(Il2SsdGuiTest.fileMenu, not(exists()));
    }
}
