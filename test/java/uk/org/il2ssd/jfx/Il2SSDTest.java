package uk.org.il2ssd.jfx;

import javafx.scene.Parent;
import org.junit.BeforeClass;
import org.junit.Test;
import org.loadui.testfx.GuiTest;
import org.loadui.testfx.utils.FXTestUtils;
import uk.org.il2ssd.Core;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.loadui.testfx.Assertions.verifyThat;
import static org.loadui.testfx.controls.Commons.nodeLabeledBy;

/**
 * TestFX Tests
 */

public class Il2SSDTest {

    private static GuiTest controller;

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        FXTestUtils.launchApp(Core.class);
        controller = new GuiTest() {
            @Override
            public Parent getRootNode() {
                return Core.getStage().getScene().getRoot();
            }
        };
        controller.sleep(5, TimeUnit.SECONDS);
    }

    @Test
    public void testConnect() throws Exception {
        controller.click("Settings");
        controller.click(nodeLabeledBy("IP Address:")).type("ghserver");
        controller.click(nodeLabeledBy("Port:")).type("21003");
        controller.click("\uf090  Connect");
    }
}
