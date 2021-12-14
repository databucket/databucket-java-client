package pl.databucket.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import com.sun.jersey.client.urlconnection.HttpURLConnectionFactory;


public class ConnectionFactory implements HttpURLConnectionFactory {

    Proxy proxy;

    public void initializeProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection(proxy);
    }
}