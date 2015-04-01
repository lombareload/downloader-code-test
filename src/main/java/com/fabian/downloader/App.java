package com.fabian.downloader;

import com.fabian.downloader.execution.DownloadExecutor;

public class App {

    public static void main(String[] args) throws Exception {
        new DownloadExecutor("http://127.0.0.1:9999/AnypointStudio-for-macosx-64bit-JUL14-201407180556.zip").startExecution();
    }
}
