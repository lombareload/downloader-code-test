package com.fabian.downloader.execution;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadExecutor {

    private static final String DOWNLOAD_FILE_NAME = "downloadedFile";
    private final ExecutorService executor = Executors.newFixedThreadPool(6);
    private final String fileLocation;
    private final int chunkSize;
    private final RandomAccessFile randomAccessFile;
    private AtomicBoolean executing = new AtomicBoolean(false);

    public DownloadExecutor(String fileLocation, int chunkSize) throws IOException {
        this.fileLocation = fileLocation;
        this.chunkSize = chunkSize;
        this.randomAccessFile = resetFile();
    }

    public boolean isExecuting(){
        return executing.get();
    }

    public void pauseExecution(){
        executing.set(false);
    }

    public void continueExecution(){
        executing.set(true);
    }

    public void startExecution() throws IOException, URISyntaxException, InterruptedException {
        HttpURLConnection connection = DownloadHelper.getConnection(getFileLocation());
        connection.setRequestMethod("HEAD");
        long contentLenght = connection.getHeaderFieldLong("Content-Length", 0);
        System.out.println(connection.getHeaderFields());

        List<Callable<byte[]>> callables = initializeThreadInstances(contentLenght);

        executor.invokeAll(callables);
        executing.set(true);
    }

    public String getFileLocation() {
        return fileLocation;
    }

    private RandomAccessFile resetFile() throws IOException{
        Path path = Paths.get(DOWNLOAD_FILE_NAME);
        Files.deleteIfExists(path);
        Path filePath = Files.createFile(path);
        RandomAccessFile randomAccessFile = new RandomAccessFile(filePath.toFile(), "rw");
        return randomAccessFile;
    }

    private List<Callable<byte[]>> initializeThreadInstances(long totalFileLength){
        List<Callable<byte[]>> callables = new ArrayList<>();
        for(long currentByte = 0; currentByte < totalFileLength; currentByte += chunkSize){
            long endPosition = currentByte + chunkSize;
            endPosition = endPosition > totalFileLength ? totalFileLength : endPosition;
            callables.add(new DownloadThread(this, currentByte, endPosition));
        }
        return callables;
    }


    private class DownloadThread implements Callable<byte[]> {

        private final DownloadExecutor downloadExecutor;
        private final long startPosition;
        private final long endPosition;

        public DownloadThread(DownloadExecutor downloadExecutor, long startPosition, long endPosition) {
            this.downloadExecutor = downloadExecutor;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }

        @Override
        public byte[] call() throws IOException, URISyntaxException, InterruptedException {
            HttpURLConnection connection = DownloadHelper.getConnection(downloadExecutor.getFileLocation());
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Range", "bytes="+ startPosition + "-" + endPosition);
            InputStream inputStream = connection.getInputStream();
            int currentByte;
            while((currentByte = inputStream.read())!= -1){
                // pause
                while(!downloadExecutor.isExecuting()){
                    wait();
                }

            }
            //            urlConnection.addRequestProperty();
//            RandomAccessFile randomAccessFile = new RandomAccessFile("downloadedFile", "rw");
            return null;
        }
    }
}
