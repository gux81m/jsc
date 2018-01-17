package basic_console;

import com.pump.swing.BasicConsole;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintStream;

/**
 * https://github.com/mickleness/pumpernickel
 */

public class JavaConsole extends JFrame {
    private JPanel buttonPanel;
    private JButton buttonExit;
    private BasicConsole console;
    private static PrintStream out;
    private JTextField commandLine;

    public JavaConsole() {
        setTitle("Console Application");
        console = new BasicConsole(false, true);
        console.setPreferredSize(new Dimension(1024, 768));
        console.setLineWrap(true);

        commandLine = new JTextField();
        commandLine.setPreferredSize(new Dimension(120, 30));

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

        add(console);
        add(commandLine);
        add(buttonPanel);

        setLayout(new FlowLayout());
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        JavaConsole javaConsole = new JavaConsole();
        out = javaConsole.console.createPrintStream(false);
    }
}