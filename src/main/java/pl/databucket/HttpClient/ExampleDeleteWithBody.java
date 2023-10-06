package pl.databucket.HttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import pl.databucket.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ExampleDeleteWithBody {

    HttpHost proxy = new HttpHost("...", 0, "http");
    Databucket databucket = new Databucket("...", "...", "...", 1, true, proxy);

    public static void main(String[] args) {
        new ExampleDeleteWithBody().run();
    }

    private void run() {
        Rules rules = new Rules();
        rules.addRule(new Rule("id", Operator.less, 4539));

        Bucket glTestSchedulerBucket = new Bucket(databucket,"....");
        RequestResponse requestResponse = glTestSchedulerBucket.deleteData(rules);
        System.out.println(requestResponse.getResponseBody());

//        HttpClient client = HttpClientBuilder.create().build();
//        HttpResponse response = null;
//
//        try {
//
//            String myJSON = "{\"rules\":[[\"id\",\"<\",4532]]}";
//            StringEntity entity = new StringEntity(myJSON, ContentType.APPLICATION_JSON);
//
//            String url = "some url";
//
//            HttpEntityDelete httpEntityDelete = new HttpEntityDelete(url);
//            httpEntityDelete.setHeaders(databucket.getHeaders2());
//            httpEntityDelete.setEntity(entity);
//            response = client.execute(httpEntityDelete);
//
//            String content = new BufferedReader(
//                    new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))
//                    .lines()
//                    .collect(Collectors.joining("\n"));
//
//            System.out.println("-----------------------------------------------------------------------");
//            System.out.println(response.getStatusLine());
//            System.out.println(content);
//            System.out.println("-----------------------------------------------------------------------");
//
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        System.out.println("Response is " + response);
    }
}
