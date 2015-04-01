package com.fabian.downloader.execution;

import java.io.IOException;
import java.net.*;

public class DownloadHelper {
    static HttpURLConnection getConnection(String fileLocation) throws IOException, URISyntaxException {
        URI uri= new URI(fileLocation);

        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        return (HttpURLConnection) urlConnection;
    }
}
