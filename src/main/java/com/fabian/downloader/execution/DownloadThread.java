package com.fabian.downloader.execution;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

class DownloadThread implements Callable<byte[]> {

    private final DownloadExecutor downloadExecutor;
    private final int startPosition;
    private final int endPosition;

    public DownloadThread(DownloadExecutor downloadExecutor, int startPosition, int endPosition) {
        this.downloadExecutor = downloadExecutor;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    @Override
    public byte[] call() throws IOException, URISyntaxException, InterruptedException {
        HttpURLConnection connection = DownloadHelper.getConnection(downloadExecutor.getFileLocation());
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Range", "bytes="+ startPosition + "-" + endPosition);
        int delta = endPosition - startPosition;
        byte[] data = new byte[delta];
        int bytesRead = 0;

        try(InputStream inputStream = new BufferedInputStream(connection.getInputStream())){
            while((bytesRead += inputStream.read(data, bytesRead, data.length - bytesRead)) < delta) {
                // pause
                downloadExecutor.waitIfPaused();
            }
            return data;
        } catch (Exception e){
            throw e;
        }
    }
}
