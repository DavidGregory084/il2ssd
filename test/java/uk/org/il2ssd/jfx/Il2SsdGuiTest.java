package uk.org.il2ssd.jfx;

import javafx.scene.Parent;
import javafx.scene.control.TextInputControl;
import org.loadui.testfx.GuiTest;
import uk.org.il2ssd.Core;

import java.util.concurrent.TimeUnit;

import static org.loadui.testfx.controls.Commons.nodeLabeledBy;
import static org.loadui.testfx.controls.TextInputControls.clearTextIn;

/**
 *
 */
public class Il2SsdGuiTest extends GuiTest {

    // Server IP and port
    static String ipAddress = "ghserver";
    static String port = "21003";

    // Node identifiers
    static String ipAddressLabel = "IP Address:";
    static String portLabel = "Port:";
    static String consoleTab = "Console";
    static String settingsTab = "Settings";
    static String consoleTextArea = ".text-area";
    static String connectButton = "\uf090  Connect";
    static String disconnectButton = "\uf08b  Disconnect";
    static String fileMenu = "File";
    static String exitMenuItem = "Exit";

    @Override
    protected Parent getRootNode() {
        return Core.getStage().getScene().getRoot();
    }

    public void enterSettings() {
        System.out.println("Entering settings...");
        this.click(settingsTab);
        clearTextIn((TextInputControl) nodeLabeledBy(ipAddressLabel));
        this.click(nodeLabeledBy(ipAddressLabel)).type(ipAddress);
        clearTextIn((TextInputControl) nodeLabeledBy(portLabel));
        this.click(nodeLabeledBy(portLabel)).type(port);
    }

    public void connect() {
        this.click(connectButton);
        this.sleep(1, TimeUnit.SECONDS);
    }

    public void disconnect() {
        this.click(disconnectButton);
        this.sleep(1, TimeUnit.SECONDS);
    }
}
