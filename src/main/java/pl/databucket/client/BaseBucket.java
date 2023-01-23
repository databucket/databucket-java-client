package pl.databucket.client;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;

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

    private void setHeaders(Invocation.Builder builder) {
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
            WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s", bucketName)));
            Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
            setHeaders(builder);

            String payload = Mapper.objectMapper.writeValueAsString(data);

            requestResponse.setRequestMethod("POST");
            requestResponse.setRequestHeaders(databucket.getHeaders());
            requestResponse.setRequestBody(payload);
            requestResponse.setRequestUrl(webTarget.getUri().toString());

            start = System.currentTimeMillis();
            Response response = builder.post(Entity.json(data));
            end = System.currentTimeMillis();

            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 201);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
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
            WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s/multi", bucketName)));
            Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
            setHeaders(builder);

            String payload = Mapper.objectMapper.writeValueAsString(dataList);

            requestResponse.setRequestMethod("POST");
            requestResponse.setRequestHeaders(databucket.getHeaders());
            requestResponse.setRequestBody(payload);
            requestResponse.setRequestUrl(webTarget.getUri().toString());

            start = System.currentTimeMillis();
            Response response = builder.post(Entity.json(dataList));
            end = System.currentTimeMillis();

            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 201);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
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
            Map<String, Object> payload = new HashMap<>();
            payload.put("columns", customDataDefs);
            payload.put("rules", rules.toNativeObject());

            String payloadStr = Mapper.objectMapper.writeValueAsString(payload);

            WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s/get", bucketName)));
            webTarget.queryParam("limit", "1");
            if (random)
                webTarget.queryParam("sort", "random");

            Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
            setHeaders(builder);

            requestResponse.setRequestMethod("POST");
            requestResponse.setRequestHeaders(databucket.getHeaders());
            requestResponse.setRequestBody(payloadStr);
            requestResponse.setRequestUrl(webTarget.getUri().toString());

            start = System.currentTimeMillis();
            Response response = builder.post(Entity.json(payload));
            end = System.currentTimeMillis();

            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 200);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
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
            WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s/%d", bucketName, id)));
            Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
            setHeaders(builder);

            requestResponse.setRequestMethod("GET");
            requestResponse.setRequestHeaders(databucket.getHeaders());
            requestResponse.setRequestUrl(webTarget.getUri().toString());

            start = System.currentTimeMillis();
            Response response = builder.get();
            end = System.currentTimeMillis();

            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 200);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
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
            WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s/get", bucketName)));
            webTarget.queryParam("limit", "1");
            if (random)
                webTarget.queryParam("sort", "random");
            Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
            setHeaders(builder);

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("rules", rules.toNativeObject());

            String payload = Mapper.objectMapper.writeValueAsString(payloadMap);

            requestResponse.setRequestMethod("POST");
            requestResponse.setRequestHeaders(databucket.getHeaders());
            requestResponse.setRequestBody(payload);
            requestResponse.setRequestUrl(webTarget.getUri().toString());

            start = System.currentTimeMillis();
            Response response = builder.post(Entity.json(payloadMap));
            end = System.currentTimeMillis();

            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 200);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
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

            WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s/reserve", bucketName)));
            webTarget.queryParam("limit", "1");
            if (random)
                webTarget.queryParam("sort", "random");
            Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
            setHeaders(builder);

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("rules", rules.toNativeObject());

            String payload = Mapper.objectMapper.writeValueAsString(payloadMap);

            requestResponse.setRequestMethod("POST");
            requestResponse.setRequestHeaders(databucket.getHeaders());
            requestResponse.setRequestBody(payload);
            requestResponse.setRequestUrl(webTarget.getUri().toString());

            start = System.currentTimeMillis();
            Response response = builder.post(Entity.json(payloadMap));
            end = System.currentTimeMillis();

            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 200);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
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
            WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s/%d", bucketName, data.getId())));
            Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
            setHeaders(builder);

            String payload = Mapper.objectMapper.writeValueAsString(data);

            requestResponse.setRequestMethod("PUT");
            requestResponse.setRequestHeaders(databucket.getHeaders());
            requestResponse.setRequestBody(payload);
            requestResponse.setRequestUrl(webTarget.getUri().toString());

            start = System.currentTimeMillis();
            Response response = builder.put(Entity.json(data));
            end = System.currentTimeMillis();

            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 200);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
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
            WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s/%d", bucketName, data.getId())));
            Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
            setHeaders(builder);

            requestResponse.setRequestMethod("DELETE");
            requestResponse.setRequestHeaders(databucket.getHeaders());
            requestResponse.setRequestBody(null);
            requestResponse.setRequestUrl(webTarget.getUri().toString());

            start = System.currentTimeMillis();
            Response response = builder.delete();
            end = System.currentTimeMillis();

            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 201);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
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
