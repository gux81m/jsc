package basic_console;

import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetNotificationHandler;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class TelnetConnector implements Runnable, TelnetNotificationHandler {
    public TelnetClient tc;
    private PrintStream out;
    private String remoteIp;
    private int remotePort;
    private String returnString;
    private JTextField commandLine;
    private boolean isTabPressed;

    TelnetConnector(String remoteIp, int remotePort, PrintStream out) throws IOException {
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.out = out;

        tc = new TelnetClient();

        TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
        EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
        SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);

        try {
            tc.addOptionHandler(ttopt);
            tc.addOptionHandler(echoopt);
            tc.addOptionHandler(gaopt);
        } catch (InvalidTelnetOptionException e) {
            System.err.println("Error registering option handlers: " + e.getMessage());
        }
    }

    public void setCommandLine(JTextField commandLine) {
        this.commandLine = commandLine;
    }

    public void connect() throws IOException {
        tc.connect(remoteIp, remotePort);
    }

    public void flush() throws IOException {
        OutputStream outstr = tc.getOutputStream();
        outstr.flush();
    }

    public void writeCommandToStream(String cmd) throws IOException {
        OutputStream outstr = tc.getOutputStream();
        outstr.write(cmd.getBytes());
    }

    public void writeCommandToStream(int cmd) throws IOException {
        OutputStream outstr = tc.getOutputStream();
        outstr.write(cmd);
    }

    public void sendAutoCompletion(String cmd) throws IOException {
        writeCommandToStream(cmd);
        writeCommandToStream(KeyEvent.VK_TAB);
        isTabPressed = true;
    }

    public void sendCommand(String cmd) throws IOException {
        writeCommandToStream(cmd);
        writeCommandToStream("\n");
        flush();
    }

    /***
     * Reader thread.
     * Reads lines from the TelnetClient and echoes them
     * on the screen.
     ***/
    @Override
    public void run() {
        InputStream instr = tc.getInputStream();

        try {
            byte[] buff = new byte[1024];
            int ret_read = 0;

            do {
                ret_read = instr.read(buff);
                if (ret_read > 0) {
                    returnString = new String(buff, 0, ret_read);
                    System.out.print(returnString);
                    out.print(returnString);
                    if (isTabPressed) {
                        commandLine.setText(returnString);
                        commandLine.setCaretPosition(returnString.length());
                        for (int i = 0; i < returnString.length(); i++) {
                            writeCommandToStream(KeyEvent.VK_BACK_SPACE);
                        }
                        flush();
                        isTabPressed = false;
                    } else {
                        commandLine.setText("");
                    }

                }
            } while (ret_read >= 0);
        } catch (IOException e) {
            System.err.println("Exception while reading socket:" + e.getMessage());
        }

        try {
            tc.disconnect();
        } catch (IOException e) {
            System.err.println("Exception while closing telnet:" + e.getMessage());
        }
    }

    /***
     * Callback method called when TelnetClient receives an option
     * negotiation command.
     *
     * @param negotiation_code - type of negotiation command received
     * (RECEIVED_DO, RECEIVED_DONT, RECEIVED_WILL, RECEIVED_WONT, RECEIVED_COMMAND)
     * @param option_code - code of the option negotiated
     ***/
    @Override
    public void receivedNegotiation(int negotiation_code, int option_code) {
        String command = null;
        switch (negotiation_code) {
            case TelnetNotificationHandler.RECEIVED_DO:
                command = "DO";
                break;
            case TelnetNotificationHandler.RECEIVED_DONT:
                command = "DONT";
                break;
            case TelnetNotificationHandler.RECEIVED_WILL:
                command = "WILL";
                break;
            case TelnetNotificationHandler.RECEIVED_WONT:
                command = "WONT";
                break;
            case TelnetNotificationHandler.RECEIVED_COMMAND:
                command = "COMMAND";
                break;
            default:
                command = Integer.toString(negotiation_code); // Should not happen
                break;
        }
        System.out.println("Received " + command + " for option code " + option_code);
    }
}
