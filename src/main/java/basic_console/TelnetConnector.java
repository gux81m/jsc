package basic_console;

import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetNotificationHandler;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;

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

    public void connect() throws IOException {
        tc.connect(remoteIp, remotePort);
    }

    public void sendCommand(String cmd) throws IOException {
        OutputStream outstr = tc.getOutputStream();
        outstr.write(cmd.getBytes());
        outstr.write(KeyEvent.VK_TAB);
        outstr.flush();
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
