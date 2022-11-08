package pl.databucket.client;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import lombok.Getter;
import lombok.Setter;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

@Getter
@Setter
public class BaseBucket {

    public final Databucket databucket;
    public final String bucketName;

    public BaseBucket(Databucket databucket, String bucketName) {
        this.databucket = databucket;
        this.bucketName = bucketName;
    }

    private void setHeaders(WebResource.Builder builder) {
        for (Map.Entry<String, List<String>> entry : databucket.getHeaders().entrySet()) {
            String value = entry.getValue().toString();
            builder = builder.header(entry.getKey(), value.substring(1, value.length() - 1));
        }
    }

    public RequestResponse insertData(BaseData data) {
        RequestResponse requestResponse = new RequestResponse();
        long start = System.currentTimeMillis();
        long end = 0;
        try {
            WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s", bucketName)));
            WebResource.Builder builder = webResource.getRequestBuilder();
            setHeaders(builder);

            String payload = Mapper.objectMapper.writeValueAsString(data);

            requestResponse.setRequestMethod("POST");
            requestResponse.setRequestHeaders(databucket.getHeaders());
            requestResponse.setRequestBody(payload);
            requestResponse.setRequestUrl(webResource.getURI().toString());

            start = System.currentTimeMillis();
            ClientResponse response = builder.post(ClientResponse.class, payload);
            end = System.currentTimeMillis();

            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 201);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.getEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            requestResponse.setResponseDuration(end - start);
        }

        return requestResponse;
    }

    public RequestResponse insertMultiData(List<? extends BaseData> dataList) {
        RequestResponse requestResponse = new RequestResponse();
        long start = System.currentTimeMillis();
        long end = 0;
        try {
            WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/multi", bucketName)));
            WebResource.Builder builder = webResource.getRequestBuilder();
            setHeaders(builder);

            String payload = Mapper.objectMapper.writeValueAsString(dataList);

            requestResponse.setRequestMethod("POST");
            requestResponse.setRequestHeaders(databucket.getHeaders());
            requestResponse.setRequestBody(payload);
            requestResponse.setRequestUrl(webResource.getURI().toString());

            start = System.currentTimeMillis();
            ClientResponse response = builder.post(ClientResponse.class, payload);
            end = System.currentTimeMillis();

            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 201);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.getEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            if (end == 0)
                end = System.currentTimeMillis();
            requestResponse.setResponseDuration(end - start);
        }

        return requestResponse;
    }

    public RequestResponse getData(Long id, List<CustomDataDef> customDataDefs) {
        return getData(new Rules(new Rule("id", Operator.equal, id)), customDataDefs);
    }

    public RequestResponse getData(Rules rules, List<CustomDataDef> customDataDefs) {
        return getData(rules, true, customDataDefs);
    }

    public RequestResponse getData(Rules rules, boolean random, List<CustomDataDef> customDataDefs) {
        RequestResponse requestResponse = new RequestResponse();
        long start = System.currentTimeMillis();
        long end = 0;
        try {
            MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
            queryParams.add("limit", "1");
            if (random)
                queryParams.add("sort", "random");

            Map<String, Object> payload = new HashMap<>();
            payload.put("columns", customDataDefs);
            payload.put("rules", rules.toNativeObject());

            String payloadStr = Mapper.objectMapper.writeValueAsString(payload);

            WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/get", bucketName)));
            webResource = webResource.queryParams(queryParams);
            WebResource.Builder builder = webResource.getRequestBuilder();
            setHeaders(builder);

            requestResponse.setRequestMethod("POST");
            requestResponse.setRequestHeaders(databucket.getHeaders());
            requestResponse.setRequestBody(payloadStr);
            requestResponse.setRequestUrl(webResource.getURI().toString());

            start = System.currentTimeMillis();
            ClientResponse response = builder.post(ClientResponse.class, payloadStr);
            end = System.currentTimeMillis();

            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 200);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.getEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            if (end == 0)
                end = System.currentTimeMillis();
            requestResponse.setResponseDuration(end - start);
        }

        return requestResponse;
    }

    public RequestResponse getData(Long id) {
        RequestResponse requestResponse = new RequestResponse();
        long start = System.currentTimeMillis();
        long end = 0;
        try {
            WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/%d", bucketName, id)));
            WebResource.Builder builder = webResource.getRequestBuilder();
            setHeaders(builder);

            requestResponse.setRequestMethod("GET");
            requestResponse.setRequestHeaders(databucket.getHeaders());
            requestResponse.setRequestUrl(webResource.getURI().toString());

            start = System.currentTimeMillis();
            ClientResponse response = builder.get(ClientResponse.class);
            end = System.currentTimeMillis();

            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 200);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.getEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            if (end == 0)
                end = System.currentTimeMillis();
            requestResponse.setResponseDuration(end - start);
        }

        return requestResponse;
    }

    public RequestResponse getData(Rules rules) {
        return getData(rules, true);
    }

    public RequestResponse getData(Rules rules, boolean random) {
        RequestResponse requestResponse = new RequestResponse();
        long start = System.currentTimeMillis();
        long end = 0;
        try {
            MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
            queryParams.add("limit", "1");
            if (random)
                queryParams.add("sort", "random");

            WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/get", bucketName)));
            webResource = webResource.queryParams(queryParams);
            WebResource.Builder builder = webResource.getRequestBuilder();
            setHeaders(builder);

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("rules", rules.toNativeObject());

            String payload = Mapper.objectMapper.writeValueAsString(payloadMap);

            requestResponse.setRequestMethod("POST");
            requestResponse.setRequestHeaders(databucket.getHeaders());
            requestResponse.setRequestBody(payload);
            requestResponse.setRequestUrl(webResource.getURI().toString());

            start = System.currentTimeMillis();
            ClientResponse response = builder.post(ClientResponse.class, payload);
            end = System.currentTimeMillis();

            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 200);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.getEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            if (end == 0)
                end = System.currentTimeMillis();
            requestResponse.setResponseDuration(end - start);
        }

        return requestResponse;
    }

    public RequestResponse reserveData(Rules rules, boolean random) {
        RequestResponse requestResponse = new RequestResponse();
        long start = System.currentTimeMillis();
        long end = 0;
        try {
            MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
            queryParams.add("limit", "1");
            if (random)
                queryParams.add("sort", "random");

            WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/reserve", bucketName)));
            webResource = webResource.queryParams(queryParams);
            WebResource.Builder builder = webResource.getRequestBuilder();
            setHeaders(builder);

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("rules", rules.toNativeObject());

            String payload = Mapper.objectMapper.writeValueAsString(payloadMap);

            requestResponse.setRequestMethod("POST");
            requestResponse.setRequestHeaders(databucket.getHeaders());
            requestResponse.setRequestBody(payload);
            requestResponse.setRequestUrl(webResource.getURI().toString());

            start = System.currentTimeMillis();
            ClientResponse response = builder.post(ClientResponse.class, payload);
            end = System.currentTimeMillis();

            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 200);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.getEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            if (end == 0)
                end = System.currentTimeMillis();
            requestResponse.setResponseDuration(end - start);
        }

        return requestResponse;
    }

    public RequestResponse updateData(BaseData data) {
        RequestResponse requestResponse = new RequestResponse();
        long start = System.currentTimeMillis();
        long end = 0;
        try {
            WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/%d", bucketName, data.getId())));
            WebResource.Builder builder = webResource.getRequestBuilder();
            setHeaders(builder);

            String payload = Mapper.objectMapper.writeValueAsString(data);

            requestResponse.setRequestMethod("PUT");
            requestResponse.setRequestHeaders(databucket.getHeaders());
            requestResponse.setRequestBody(payload);
            requestResponse.setRequestUrl(webResource.getURI().toString());

            start = System.currentTimeMillis();
            ClientResponse response = builder.put(ClientResponse.class, payload);
            end = System.currentTimeMillis();

            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 200);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.getEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            if (end == 0)
                end = System.currentTimeMillis();
            requestResponse.setResponseDuration(end - start);
        }

        return requestResponse;
    }

    public RequestResponse deleteData(BaseData data) {
        RequestResponse requestResponse = new RequestResponse();
        long start = System.currentTimeMillis();
        long end = 0;
        try {
            WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/%d", bucketName, data.getId())));
            WebResource.Builder builder = webResource.getRequestBuilder();
            setHeaders(builder);

            requestResponse.setRequestMethod("DELETE");
            requestResponse.setRequestHeaders(databucket.getHeaders());
            requestResponse.setRequestBody(null);
            requestResponse.setRequestUrl(webResource.getURI().toString());

            start = System.currentTimeMillis();
            ClientResponse response = builder.delete(ClientResponse.class);
            end = System.currentTimeMillis();

            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 201);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.getEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            if (end == 0)
                end = System.currentTimeMillis();
            requestResponse.setResponseDuration(end - start);
        }

        return requestResponse;
    }

}
