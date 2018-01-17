package basic_console;

import com.pump.swing.BasicConsole;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintStream;

/**
 * https://github.com/mickleness/pumpernickel
 */

public class JavaConsole {
    private BasicConsole console;
    private JPanel controls;
    private JTextField commandLine;
    private JPanel buttonPanel;
    private JButton buttonExit;
    private static PrintStream out;

    public JavaConsole() {
        console = BasicConsole.create("Console Application", false, true, true);
        console.setLineWrap(true);
        console.setWrapStyleWord(true);

        controls = new JPanel();

        commandLine = new JTextField();

        buttonPanel = new JPanel();
        buttonExit = new JButton("Exit");

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(buttonExit);

        buttonExit.addActionListener(e -> {
            String exitString = "exit";
            out.print(exitString);
        });

        commandLine.addActionListener(e -> {
            String cmd = commandLine.getText();
            System.out.println("cmd: " + cmd);
            out.print(cmd + "\n");
            commandLine.setText("");
        });

        commandLine.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE && (e.getModifiers() | KeyEvent.CTRL_MASK) == KeyEvent.CTRL_MASK) {
                    out.print("ctrl+space pressed: todo: SEND TAB\n");
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

    public static void main(String[] args) throws InterruptedException {
        JavaConsole javaConsole = new JavaConsole();
        out = javaConsole.console.createPrintStream(false);
    }
}