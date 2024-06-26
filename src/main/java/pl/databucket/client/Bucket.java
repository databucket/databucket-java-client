package pl.databucket.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import pl.databucket.HttpClient.HttpEntityDelete;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Getter
public class Bucket {

    public final Databucket databucket;
    public final String bucketName;
    protected final Gson gson;

    public Bucket(Databucket databucket, String bucketName) {
        this.databucket = databucket;
        this.bucketName = bucketName;
        gson = new GsonBuilder().disableHtmlEscaping().create();
    }

    private void setHeaders(Invocation.Builder builder) {
        for (Map.Entry<String, List<String>> entry : databucket.getHeaders().entrySet()) {
            String value = entry.getValue().toString();
            builder = builder.header(entry.getKey(), value.substring(1, value.length() - 1));
        }
    }

    public RequestResponse insertData(Data data) {
        Map<String, Object> json = new HashMap<>();
        if (data.getReserved() != null)
            json.put(Field.RESERVED, data.getReserved());

        if (data.getTagId() != null)
            json.put(Field.TAG_ID, data.getTagId());

        if (data.getProperties() != null)
            json.put(Field.PROPERTIES, data.getProperties());

        WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s", bucketName)));
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
        setHeaders(builder);

        String payload = gson.toJson(json);

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("POST");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(payload);
        requestResponse.setRequestUrl(webTarget.getUri().toString());

        long start = System.currentTimeMillis();
        try {
            Response response = builder.post(Entity.json(json));
            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 201);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            requestResponse.setResponseDuration(System.currentTimeMillis() - start);
        }

        return requestResponse;
    }

    public RequestResponse insertMultiData(List<? extends Data> dataList) {
        WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s/multi", bucketName)));
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
        setHeaders(builder);

        String payload = gson.toJson(dataList);

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("POST");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(payload);
        requestResponse.setRequestUrl(webTarget.getUri().toString());

        long start = System.currentTimeMillis();
        try {
            Response response = builder.post(Entity.json(dataList));
            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 201);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            requestResponse.setResponseDuration(System.currentTimeMillis() - start);
        }

        return requestResponse;
    }

    public RequestResponse getData(Long id, List<String> fields) {
        return getData(new Rules(new Rule("id", Operator.equal, id)), fields);
    }

    public RequestResponse getData(Rules rules, List<String> fields) {
        return getData(rules, true, fields);
    }

    public RequestResponse getData(Rules rules, boolean random, List<String> fields) {
        return getData(rules, random, fields, 1L);
    }

    public RequestResponse getData(Rules rules, boolean random, List<String> fields, Long limit) {
        Map<String, Object> json = new HashMap<>();
        json.put("columns", fieldsToColumns(fields));
        json.put("rules", rules.toNativeObject());

        WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s/get", bucketName)));
        webTarget = webTarget.queryParam("limit", limit);
        if (random)
            webTarget = webTarget.queryParam("sort", "random");
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
        setHeaders(builder);

        String payload = gson.toJson(json);

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("POST");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(payload);
        requestResponse.setRequestUrl(webTarget.getUri().toString());

        long start = System.currentTimeMillis();
        try {
            Response response = builder.post(Entity.json(json));
            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 200);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            requestResponse.setResponseDuration(System.currentTimeMillis() - start);
        }

        return requestResponse;
    }

    public RequestResponse getData(Long id) {
        WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s/%d", bucketName, id)));
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
        setHeaders(builder);

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("GET");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestUrl(webTarget.getUri().toString());

        long start = System.currentTimeMillis();
        try {
            Response response = builder.get();
            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 200);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            requestResponse.setResponseDuration(System.currentTimeMillis() - start);
        }

        return requestResponse;
    }

    public RequestResponse getData(Rules rules) {
        return getData(rules, true);
    }

    public RequestResponse getData(Rules rules, boolean random) {
        WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s/get", bucketName)));
        webTarget = webTarget.queryParam("limit", "1");
        if (random)
            webTarget = webTarget.queryParam("sort", "random");
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
        setHeaders(builder);

        Map<String, Object> json = new HashMap<>();
        json.put("rules", rules.toNativeObject());

        String payload = gson.toJson(json);

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("POST");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(payload);
        requestResponse.setRequestUrl(webTarget.getUri().toString());

        long start = System.currentTimeMillis();
        try {
            Response response = builder.post(Entity.json(json));
            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 200);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            requestResponse.setResponseDuration(System.currentTimeMillis() - start);
        }

        return requestResponse;
    }

    public RequestResponse reserveData(Rules rules, boolean random) {
        WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s/reserve", bucketName)));
        webTarget = webTarget.queryParam("limit", "1");
        if (random)
            webTarget = webTarget.queryParam("sort", "random");
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
        setHeaders(builder);

        Map<String, Object> json = new HashMap<>();
        json.put("rules", rules.toNativeObject());

        String payload = gson.toJson(json);

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("POST");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(payload);
        requestResponse.setRequestUrl(webTarget.getUri().toString());

        long start = System.currentTimeMillis();
        try {
            Response response = builder.post(Entity.json(json));
            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 200);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            requestResponse.setResponseDuration(System.currentTimeMillis() - start);
        }

        return requestResponse;
    }

    public RequestResponse updateData(
            Rules rules,
            Boolean reserved,
            Integer tagId,
            Map<String, Object> properties,
            Map<String, Object> propertiesToSet,
            List<String> propertiesToRemove) {

        Map<String, Object> json = new HashMap<>();
        json.put("rules", rules.toNativeObject());

        if (reserved != null)
            json.put(Field.RESERVED, reserved);

        if (tagId != null)
            json.put(Field.TAG_ID, tagId);

        if (properties != null)
            json.put("properties", properties);

        if (propertiesToSet != null)
            json.put("propertiesToSet", propertiesToSet);

        if (propertiesToRemove != null)
            json.put("propertiesToRemove", propertiesToRemove);

        WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s", bucketName)));
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
        setHeaders(builder);

        String payload = gson.toJson(json);

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("PUT");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(payload);
        requestResponse.setRequestUrl(webTarget.getUri().toString());

        long start = System.currentTimeMillis();
        try {
            Response response = builder.put(Entity.json(json));
            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 200);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            requestResponse.setResponseDuration(System.currentTimeMillis() - start);
        }

        return requestResponse;
    }

    public RequestResponse updateData(Data data) {
        Map<String, Object> json = new HashMap<>();
        if (data.getReserved() != null)
            json.put(Field.RESERVED, data.getReserved());

        if (data.getTagId() != null)
            json.put(Field.TAG_ID, data.getTagId());

        if (data.getProperties() != null)
            json.put("properties", data.getProperties());

        WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s/%d", bucketName, data.getId())));
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
        setHeaders(builder);

        String payload = gson.toJson(json);

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("PUT");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(payload);
        requestResponse.setRequestUrl(webTarget.getUri().toString());

        long start = System.currentTimeMillis();
        try {
            Response response = builder.put(Entity.json(data));
            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 200);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            requestResponse.setResponseDuration(System.currentTimeMillis() - start);
        }

        return requestResponse;
    }

    public RequestResponse deleteData(Data data) {
        WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s/%d", bucketName, data.getId())));
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
        setHeaders(builder);

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("DELETE");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(null);
        requestResponse.setRequestUrl(webTarget.getUri().toString());

        long start = System.currentTimeMillis();
        try {
            Response response = builder.delete();
            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 201);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            requestResponse.setResponseDuration(System.currentTimeMillis() - start);
        }

        return requestResponse;
    }

    public RequestResponse deleteData(String ids) {
        WebTarget webTarget = databucket.getClient().target(databucket.buildUrl(String.format("/api/bucket/%s/%s", bucketName, ids)));
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
        setHeaders(builder);

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("DELETE");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(null);
        requestResponse.setRequestUrl(webTarget.getUri().toString());

        long start = System.currentTimeMillis();
        try {
            Response response = builder.delete();
            requestResponse.setResponseStatus(response.getStatus());
            requestResponse.setResponseCorrect(response.getStatus() == 201);
            requestResponse.setResponseHeaders(response.getHeaders());
            requestResponse.setResponseBody(response.readEntity(String.class));
        } catch (Exception e) {
            requestResponse.setResponseCorrect(false);
            requestResponse.setException(e);
        } finally {
            requestResponse.setResponseDuration(System.currentTimeMillis() - start);
        }

        return requestResponse;
    }

    public RequestResponse deleteData(Rules rules) {

        Map<String, Object> json = new HashMap<>();
        json.put("rules", rules.toNativeObject());
        String payload = gson.toJson(json);

        String url = databucket.buildUrl(String.format("/api/bucket/%s", bucketName));

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("DELETE");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(payload);
        requestResponse.setRequestUrl(url);

        long start = System.currentTimeMillis();
        try {
            StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);

            HttpEntityDelete httpEntityDelete = new HttpEntityDelete(url);
            httpEntityDelete.setHeaders(databucket.getHeaders2());
            httpEntityDelete.setEntity(entity);
            HttpResponse response = databucket.getHttpClient().execute(httpEntityDelete);

            String content = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            requestResponse.setResponseStatus(response.getStatusLine().getStatusCode());
            requestResponse.setResponseCorrect(response.getStatusLine().getStatusCode() == 201);
            requestResponse.setResponseBody(content);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            requestResponse.setResponseDuration(System.currentTimeMillis() - start);
        }

        return requestResponse;
    }

    public Map<String, Object> strToJson(String text) {
        return gson.fromJson(text, Map.class);
    }

    public Data strToData(String dataStr) {
        return jsonToData(gson.fromJson(dataStr, Map.class));
    }

    public Data jsonToData(Map<String, Object> jsonObj) {
        Long id = null;
        Integer tagId = null;
        Boolean reserved = null;
        String owner = null;
        Map<String, Object> properties = null;
        String createdBy = null;
        Date createdAt = null;
        String modifiedBy = null;
        Date modifiedAt = null;

        if (jsonObj.containsKey(Field.ID))
            id = ((Double) jsonObj.get(Field.ID)).longValue();

        if (jsonObj.containsKey(Field.TAG_ID))
            if (jsonObj.get(Field.TAG_ID) != null)
                tagId = ((Double) jsonObj.get(Field.TAG_ID)).intValue();

        if (jsonObj.containsKey(Field.RESERVED)) {
            Object objReserved = jsonObj.get(Field.RESERVED);
            if (objReserved instanceof Boolean)
                reserved = (Boolean) objReserved;
            else
                reserved = ((Double) objReserved) == 1;
        }

        if (jsonObj.containsKey(Field.OWNER))
            owner = ((String) jsonObj.get(Field.OWNER));

        if (jsonObj.containsKey(Field.PROPERTIES))
            properties = (Map<String, Object>) jsonObj.get(Field.PROPERTIES);

        if (jsonObj.containsKey(Field.CREATED_BY))
            createdBy = (String) jsonObj.get(Field.CREATED_BY);

        String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        if (jsonObj.containsKey(Field.CREATED_AT)) {
            String dateTimeStr = (String) jsonObj.get(Field.CREATED_AT);
            DateFormat format = new SimpleDateFormat(DATE_FORMAT);
            try {
                createdAt = format.parse(dateTimeStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (jsonObj.containsKey(Field.MODIFIED_BY))
            modifiedBy = (String) jsonObj.get(Field.MODIFIED_BY);

        if (jsonObj.containsKey(Field.MODIFIED_AT)) {
            Object obj = jsonObj.get(Field.MODIFIED_AT);
            if (obj != null) {
                String dateTimeStr = (String) jsonObj.get(Field.MODIFIED_AT);
                DateFormat format = new SimpleDateFormat(DATE_FORMAT);
                try {
                    modifiedAt = format.parse(dateTimeStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return new Data(id, tagId, reserved, owner, properties, createdAt, createdBy, modifiedAt, modifiedBy);
    }

    public Data customJsonToData(Map<String, Object> customJsonObj) {
        Long id = null;
        Integer tagId = null;
        Boolean reserved = null;
        String owner = null;
        Map<String, Object> properties;
        String createdBy = null;
        Date createdAt = null;
        String modifiedBy = null;
        Date modifiedAt = null;

        if (customJsonObj.containsKey(Field.ID))
            id = ((Double) customJsonObj.get(Field.ID)).longValue();

        if (customJsonObj.containsKey(Field.TAG_ID))
            if (customJsonObj.get(Field.TAG_ID) != null)
                tagId = ((Double) customJsonObj.get(Field.TAG_ID)).intValue();

        if (customJsonObj.containsKey(Field.RESERVED))
            reserved = (Boolean) customJsonObj.get(Field.RESERVED);

        if (customJsonObj.containsKey(Field.OWNER))
            owner = ((String) customJsonObj.get(Field.OWNER));

        if (customJsonObj.containsKey(Field.CREATED_BY))
            createdBy = (String) customJsonObj.get(Field.CREATED_BY);

        String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        if (customJsonObj.containsKey(Field.CREATED_AT)) {
            String dateTimeStr = (String) customJsonObj.get(Field.CREATED_AT);
            DateFormat format = new SimpleDateFormat(DATE_FORMAT);
            try {
                createdAt = format.parse(dateTimeStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (customJsonObj.containsKey(Field.MODIFIED_BY))
            modifiedBy = (String) customJsonObj.get(Field.MODIFIED_BY);

        if (customJsonObj.containsKey(Field.MODIFIED_AT)) {
            Object obj = customJsonObj.get(Field.MODIFIED_AT);
            if (obj != null) {
                String dateTimeStr = (String) customJsonObj.get(Field.MODIFIED_AT);
                DateFormat format = new SimpleDateFormat(DATE_FORMAT);
                try {
                    modifiedAt = format.parse(dateTimeStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        properties = new HashMap<>();
        Data data = new Data(id, tagId, reserved, owner, properties, createdAt, createdBy, modifiedAt, modifiedBy);

        customJsonObj.forEach((key, value) -> {
            if (key.startsWith("$."))
                data.setProperty(key.substring(2), value);
        });

        return data;
    }

    private List<Map<String, String>> fieldsToColumns(List<String> fields) {
        List<Map<String, String>> columns = new ArrayList<>();
        fields.forEach(field -> {
            Map<String, String> map = new HashMap<>();
            map.put("field", field);
            map.put("title", field);
            columns.add(map);
        });
        return columns;
    }

}
