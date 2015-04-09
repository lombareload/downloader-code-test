package com.fabian.downloader.execution;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DownloadExecutor {

    private final ExecutorService executor = Executors.newFixedThreadPool(6);
    private AtomicBoolean executing = new AtomicBoolean(false);
    private final Lock lock = new ReentrantLock();
    private Condition isPaused = lock.newCondition();


    private final String fileLocation;
    private final int chunkSize;
    private final RandomAccessFile randomAccessFile;

    public DownloadExecutor(String fileLocation, int chunkSize) throws IOException {
        this.fileLocation = fileLocation;
        this.chunkSize = chunkSize;
        this.randomAccessFile = DownloadHelper.resetFile(DownloadHelper.extractFileName(fileLocation));
    }

    public boolean isExecuting(){
        return executing.get();
    }

    public void pauseExecution(){
        executing.set(false);
    }

    public void continueExecution(){
        lock.lock();
        executing.set(true);
        isPaused.signalAll();
        lock.unlock();
    }

    public void waitIfPaused() throws InterruptedException {
        if (!isExecuting()){
            lock.lock();
            isPaused.await();
            lock.unlock();
        }
    }

    public void startExecution() throws IOException, URISyntaxException, InterruptedException, ExecutionException {
        HttpURLConnection connection = DownloadHelper.getConnection(getFileLocation());
        connection.setRequestMethod("HEAD");
        int contentLength = connection.getHeaderFieldInt("Content-Length", 0);

        List<Callable<byte[]>> callables = initializeThreadInstances(contentLength);

        executing.set(true);
        List<Future<byte[]>> futures = executor.invokeAll(callables);
        for (Future<byte[]> future : futures) {
            randomAccessFile.write(future.get());
        }
        executor.shutdown();
        randomAccessFile.close();
        System.exit(0);
    }

    public String getFileLocation() {
        return fileLocation;
    }

    private List<Callable<byte[]>> initializeThreadInstances(int totalFileLength) {
        List<Callable<byte[]>> callables = new ArrayList<>();
        for(int currentByte = 0; currentByte < totalFileLength; currentByte += chunkSize){
            int endPosition = currentByte + chunkSize;
            endPosition = endPosition > totalFileLength ? totalFileLength : endPosition;
            callables.add(new DownloadThread(this, currentByte, endPosition));
        }
        return callables;
    }
}
