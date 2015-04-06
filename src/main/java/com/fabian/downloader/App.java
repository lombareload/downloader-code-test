package com.fabian.downloader;

import com.fabian.downloader.execution.DownloadExecutor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class App {

    public static final String FILE_URL = "execution.file";
    public static final String KB_SIZE = "execution.kb.size";

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.load(App.class.getClassLoader().getResourceAsStream("app.properties"));
        System.out.println("execution with properties: " + properties);

        int kbs = Integer.parseInt(properties.getProperty(KB_SIZE));

        DownloadExecutor executor = new DownloadExecutor(
                properties.getProperty(FILE_URL), 1024 * kbs);
        show(executor);
    }

    private static void show(final DownloadExecutor executor) {
        final JButton button = new JButton();
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                button.setText("Pause");
                button.removeActionListener(this);
                Thread thread = new Thread(){
                    @Override public void run(){
                        try {
                            executor.startExecution();
                        } catch (IOException | URISyntaxException |InterruptedException | ExecutionException ex) {
                            ex.printStackTrace();
                            System.exit(1);
                        }
                    }
                };
                thread.start();

                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(executor.isExecuting()) {
                            executor.pauseExecution();
                            button.setText("Resume");
                        } else {
                            executor.continueExecution();
                            button.setText("Pause");
                        }
                    }
                });
            }
        });
        button.setText("Start");
        JFrame frame = new JFrame("Downloader");
        frame.setSize(300, 100);
        JPanel panel = new JPanel();
        panel.add(button);
        frame.setContentPane(panel);
        frame.setVisible(true);
    }
}
