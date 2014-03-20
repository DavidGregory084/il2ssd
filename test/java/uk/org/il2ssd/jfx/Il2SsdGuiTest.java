package uk.org.il2ssd.jfx;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import org.loadui.testfx.GuiTest;
import org.loadui.testfx.exceptions.NoNodesFoundException;
import uk.org.il2ssd.Core;

import static org.hamcrest.CoreMatchers.is;
import static org.loadui.testfx.controls.Commons.nodeLabeledBy;
import static org.loadui.testfx.controls.TextInputControls.clearTextIn;
import static org.loadui.testfx.controls.impl.EnabledMatcher.disabled;
import static org.loadui.testfx.controls.impl.VisibleNodesMatcher.visible;

/**
 * GUI Test Controller
 */
public class Il2SsdGuiTest extends GuiTest {

    // Node identifiers
    static final String ipAddressLabel = "IP Address:";
    static final String portLabel = "Port:";
    static final String consoleTab = "Console";
    static final String missionTab = "Mission";
    static final String settingsTab = "Settings";
    static final String consoleTextArea = ".text-area";
    static final String connectButton = "\uf090  Connect";
    static final String disconnectButton = "\uf08b  Disconnect";
    static final String startButton = "\uf04b Start";
    static final String stopButton = "\uf04d Stop";
    static final String nextButton = "\uf04e Next";
    static final String loadButton = "\uf093 Load";
    static final String unloadButton = "\uf05e Unload";
    static final String singleMisFieldLabel = "Remote:";
    static final String singleMisPathLabel = "Mission Path:";
    static final String singleMisRemoteSelect = "\uf00c Select";
    static final String getDifficultiesButton = "\uf019 Get";
    static final String putDifficultiesButton = "\uf093 Put";
    static final String fileMenu = "File";
    static final String exitMenuItem = "Exit";

    // Server IP and port
    static String ipAddress;
    static String port;
    static String singleMission;

    public Il2SsdGuiTest(String ipAddress, String port, String singleMission) {
        Il2SsdGuiTest.ipAddress = ipAddress;
        Il2SsdGuiTest.port = port;
        Il2SsdGuiTest.singleMission = singleMission;
    }

    public Il2SsdGuiTest(String ipAddress, String port) {
        Il2SsdGuiTest.ipAddress = ipAddress;
        Il2SsdGuiTest.port = port;
        Il2SsdGuiTest.singleMission = "Net/dogfight/DCG/dcgmission.mis";
    }

    public Il2SsdGuiTest() {
        Il2SsdGuiTest.ipAddress = "ghserver";
        Il2SsdGuiTest.port = "21003";
        Il2SsdGuiTest.singleMission = "Net/dogfight/DCG/dcgmission.mis";
    }

    @Override
    protected Parent getRootNode() {
        return Core.getStage().getScene().getRoot();
    }

    public void enterSettings() {
        this.click(settingsTab);
        TextField ipAddressField = (TextField) nodeLabeledBy(ipAddressLabel);
        TextField portField = (TextField) nodeLabeledBy(portLabel);
        if (!ipAddressField.getText().equals(ipAddress)) {
            clearTextIn((TextInputControl) nodeLabeledBy(ipAddressLabel));
            this.click(nodeLabeledBy(ipAddressLabel)).type(ipAddress);
        }
        if (!portField.getText().equals(port)) {
            clearTextIn((TextInputControl) nodeLabeledBy(portLabel));
            this.click(nodeLabeledBy(portLabel)).type(port);
        }
    }

    public void loadMission() {
        this.click(missionTab);
        Label singleMisPath = (Label) nodeLabeledBy(singleMisPathLabel);
        try {
            find(loadButton);
        } catch (NoNodesFoundException e) {
            this.click(unloadButton);
            waitUntil(loadButton, is(visible()));
        }
        if (!singleMisPath.getText().equals(singleMission)) {
            clearTextIn((TextInputControl) nodeLabeledBy(singleMisFieldLabel));
            this.click(nodeLabeledBy(singleMisFieldLabel)).type(singleMission);
        }
        this.click(singleMisRemoteSelect).click(loadButton);
    }

    public void connect() {
        this.click(connectButton);
        waitUntil(connectButton, is(disabled()));
    }

    public void disconnect() {
        this.click(disconnectButton);
        waitUntil(disconnectButton, is(disabled()));
    }
}
