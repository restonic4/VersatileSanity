package com.restonic4.versatilesanity.modules;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ErrorWindow extends JDialog {
    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JLabel lblNewLabel;

    public ErrorWindow(String text) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        int width = 450;
        int height = 300;

        setTitle("Game crashed!");
        setAlwaysOnTop(true);
        setBounds(screenWidth / 2 - width / 2, screenHeight / 2 - height / 2, width, height);
        getContentPane().setLayout(new BorderLayout());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        {
            lblNewLabel = new JLabel(text);
            String htmlText = "<html><div style='text-align: center; width: 100%;'>" + text + "</div></html>";
            lblNewLabel.setText(htmlText);
            lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }
        GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
        gl_contentPanel.setHorizontalGroup(
                gl_contentPanel.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
        );
        gl_contentPanel.setVerticalGroup(
                gl_contentPanel.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
        );
        contentPanel.setLayout(gl_contentPanel);
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        close();
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        close();
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }

        setVisible(true);
    }

    public void close() {
        this.dispose();
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");

        String receivedMessage = args.length > 0 ? args[0] : "Something went wrong!";

        javax.swing.SwingUtilities.invokeLater(() -> {
            new ErrorWindow(receivedMessage);
        });
    }

    public static void execute(String message) {
        new Thread(() -> {
            try {
                String javaBin = System.getProperty("java.home") +
                        File.separator + "bin" +
                        File.separator + "java";
                String classpath = System.getProperty("java.class.path");
                String className = ErrorWindow.class.getName();

                ProcessBuilder builder = new ProcessBuilder(
                        javaBin,
                        "-Djava.awt.headless=false",
                        "-cp",
                        classpath,
                        className,
                        message  // Aquí pasamos el String como argumento
                );

                builder.inheritIO();
                Process process = builder.start();
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}