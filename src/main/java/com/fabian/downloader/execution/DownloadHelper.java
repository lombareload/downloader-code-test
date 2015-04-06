package com.fabian.downloader.execution;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadHelper {

    private DownloadHelper() {
        throw new IllegalAccessError();
    }

    static HttpURLConnection getConnection(String fileLocation) throws IOException, URISyntaxException {
        URI uri= new URI(fileLocation);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        return (HttpURLConnection) urlConnection;
    }

    static RandomAccessFile resetFile(String fileName) throws IOException{
        Path path = Paths.get(fileName);
        Files.deleteIfExists(path);
        Path filePath = Files.createFile(path);
        RandomAccessFile randomAccessFile = new RandomAccessFile(filePath.toFile(), "rw");
        return randomAccessFile;
    }

    static String extractFileName(String filePath){
        return filePath.replaceFirst(".*/", "");
    }
}
