package basic_console;

import com.pump.swing.BasicConsole;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * https://github.com/mickleness/pumpernickel
 */

public class JavaConsole implements Runnable {
    private static TelnetConnector telnetConnector;

    private BasicConsole console;
    private JPanel controls;
    private JTextField commandLine;
    private JPanel buttonPanel;
    private JButton buttonExit;
    private static PrintStream out;

    public JavaConsole() throws IOException {
        console = BasicConsole.create("Console Application", false, true, true);
        console.setLineWrap(true);
        console.setWrapStyleWord(true);

        out = console.createPrintStream(false);
        telnetConnector = new TelnetConnector("10.41.99.64", 23, out);



        controls = new JPanel();

        commandLine = new JTextField();

        buttonPanel = new JPanel();
        buttonExit = new JButton("Exit");

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(buttonExit);

        buttonExit.addActionListener(e -> {
            String exitString = "exit\n";
            out.print(exitString);
            OutputStream outstr = telnetConnector.tc.getOutputStream();
            try {
                outstr.write(exitString.getBytes());
                outstr.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        commandLine.addActionListener(e -> {
            String cmd = commandLine.getText() + "\n";
            System.out.println("cmd: " + cmd);
            out.print(cmd);
            OutputStream outstr = telnetConnector.tc.getOutputStream();
            try {
                outstr.write(cmd.getBytes());
                outstr.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            commandLine.setText("");
        });

        commandLine.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE && (e.getModifiers() | KeyEvent.CTRL_MASK) == KeyEvent.CTRL_MASK) {
                    String cmd = commandLine.getText();
                    System.out.println(cmd + "ctrl+space");
                    try {
                        telnetConnector.sendCommand(cmd);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    commandLine.setText("");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });

        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(commandLine);
        controls.add(buttonPanel);

        Container rootPanel = console.getRootPane().getContentPane();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        rootPanel.add(controls);

    }

    public static void main(String[] args) throws InterruptedException, IOException {
        JavaConsole javaConsole = new JavaConsole();
        Thread main = new Thread(javaConsole);
        main.start();
    }

    @Override
    public void run() {
        try {
            Thread reader = new Thread(telnetConnector);
            telnetConnector.connect();
            telnetConnector.tc.registerNotifHandler(telnetConnector);
            reader.start();

            OutputStream outstr = telnetConnector.tc.getOutputStream();

            byte[] buff = new byte[1024];
            int ret_read = 0;

            do {
                try {
                    ret_read = System.in.read(buff);
                    if (ret_read > 0) {
                        try {
                            outstr.write(buff, 0, ret_read);
                            outstr.flush();
                        } catch (IOException e) {
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Exception while reading keyboard:" + e.getMessage());
                }
            } while ((ret_read > 0));

            try {
                telnetConnector.tc.disconnect();
            } catch (IOException e) {
                System.err.println("Exception while connecting:" + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Exception while connecting:" + e.getMessage());
            System.exit(1);
        }
    }
}