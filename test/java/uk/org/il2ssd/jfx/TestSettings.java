package uk.org.il2ssd.jfx;

import org.junit.BeforeClass;
import org.junit.Test;
import org.loadui.testfx.exceptions.NoNodesFoundException;
import org.loadui.testfx.utils.FXTestUtils;
import uk.org.il2ssd.Core;

import static org.hamcrest.CoreMatchers.is;
import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.Assertions.verifyThat;
import static org.loadui.testfx.GuiTest.find;
import static org.loadui.testfx.GuiTest.waitUntil;
import static org.loadui.testfx.controls.impl.EnabledMatcher.disabled;
import static org.loadui.testfx.controls.impl.EnabledMatcher.enabled;
import static org.loadui.testfx.controls.impl.VisibleNodesMatcher.visible;

/**
 * Settings Tab GUI Tests
 */
public class TestSettings {

    private static Il2SsdGuiTest controller;

    @BeforeClass
    public static void setUpClass() {
        FXTestUtils.launchApp(Core.class);
        controller = new Il2SsdGuiTest();
        waitUntil(Il2SsdGuiTest.settingsTab, is(visible()));
        controller.enterSettings();
    }

    @Test
    public void test1_difficultyButtons() {
        // Verify initial state
        verifyThat(Il2SsdGuiTest.getDifficultiesButton, is(disabled()));
        verifyThat(Il2SsdGuiTest.putDifficultiesButton, is(disabled()));
        controller.connect();
        controller.loadMission();
        waitUntil(Il2SsdGuiTest.unloadButton, is(visible()));
        controller.click(Il2SsdGuiTest.settingsTab);
        // Verify started and stopped state
        try {
            find(Il2SsdGuiTest.stopButton);
            verifyThat(Il2SsdGuiTest.getDifficultiesButton, is(enabled()));
            verifyThat(Il2SsdGuiTest.putDifficultiesButton, is(disabled()));
            controller.click(Il2SsdGuiTest.stopButton);
            verifyThat(Il2SsdGuiTest.getDifficultiesButton, is(enabled()));
            verifyThat(Il2SsdGuiTest.putDifficultiesButton, is(enabled()));
        } catch (NoNodesFoundException e) {
            assertNodeExists(Il2SsdGuiTest.startButton);
            verifyThat(Il2SsdGuiTest.getDifficultiesButton, is(enabled()));
            verifyThat(Il2SsdGuiTest.putDifficultiesButton, is(enabled()));
            controller.click(Il2SsdGuiTest.startButton);
            verifyThat(Il2SsdGuiTest.getDifficultiesButton, is(enabled()));
            verifyThat(Il2SsdGuiTest.putDifficultiesButton, is(disabled()));
        }
        // Verify disconnected state
        controller.disconnect();
        verifyThat(Il2SsdGuiTest.getDifficultiesButton, is(disabled()));
        verifyThat(Il2SsdGuiTest.putDifficultiesButton, is(disabled()));
    }
}
