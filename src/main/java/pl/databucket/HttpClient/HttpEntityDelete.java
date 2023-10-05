package pl.databucket.HttpClient;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

public class HttpEntityDelete extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "DELETE";

    public HttpEntityDelete() {
    }

    public HttpEntityDelete(URI uri) {
        this.setURI(uri);
    }

    public HttpEntityDelete(String uri) {
        this.setURI(URI.create(uri));
    }

    public String getMethod() {
        return METHOD_NAME;
    }
}
