package pl.databucket.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import javax.ws.rs.core.MultivaluedMap;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("unchecked")
public class Bucket {

    private final Databucket databucket;
    private final String bucketName;
    protected final Gson gson;

    public Bucket(Databucket databucket, String bucketName) {
        this.databucket = databucket;
        this.bucketName = bucketName;
        gson = new GsonBuilder().disableHtmlEscaping().create();
    }

    private void setHeaders(WebResource.Builder builder) {
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

        WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s", bucketName)));
        WebResource.Builder builder = webResource.getRequestBuilder();
        setHeaders(builder);

        String payload = gson.toJson(json);
        long start = System.currentTimeMillis();
        ClientResponse response = builder.post(ClientResponse.class, payload);
        long end = System.currentTimeMillis();

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("POST");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(payload);
        requestResponse.setRequestUrl(webResource.getURI().toString());

        requestResponse.setResponseDuration(end-start);
        requestResponse.setResponseStatus(response.getStatus());
        requestResponse.setResponseCorrect(response.getStatus() == 201);
        requestResponse.setResponseHeaders(response.getHeaders());
        requestResponse.setResponseBody(response.getEntity(String.class));

        return requestResponse;
    }

    public RequestResponse insertMultiData(List<? extends Data> dataList) {
        WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/multi", bucketName)));
        WebResource.Builder builder = webResource.getRequestBuilder();
        setHeaders(builder);

        String payload = gson.toJson(dataList);
        long start = System.currentTimeMillis();
        ClientResponse response = builder.post(ClientResponse.class, payload);
        long end = System.currentTimeMillis();

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("POST");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(payload);
        requestResponse.setRequestUrl(webResource.getURI().toString());

        requestResponse.setResponseDuration(end-start);
        requestResponse.setResponseStatus(response.getStatus());
        requestResponse.setResponseCorrect(response.getStatus() == 201);
        requestResponse.setResponseHeaders(response.getHeaders());
        requestResponse.setResponseBody(response.getEntity(String.class));

        return requestResponse;
    }

    public RequestResponse getData(Long id, List<String> fields) {
        return getData(new Rules(new Rule("id", Operator.equal, id)), fields);
    }

    public RequestResponse getData(Rules rules, List<String> fields) {
        return getData(rules, true, fields);
    }

    public RequestResponse getData(Rules rules, boolean random, List<String> fields) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("limit", "1");
        if (random)
            queryParams.add("sort", "random");

        Map<String, Object> json = new HashMap<>();
        json.put("columns", fieldsToColumns(fields));
        json.put("rules", rules.toNativeObject());

        WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/get", bucketName)));
        webResource = webResource.queryParams(queryParams);
        WebResource.Builder builder = webResource.getRequestBuilder();
        setHeaders(builder);

        String payload = gson.toJson(json);
        long start = System.currentTimeMillis();
        ClientResponse response = builder.post(ClientResponse.class, payload);
        long end = System.currentTimeMillis();

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("POST");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(payload);
        requestResponse.setRequestUrl(webResource.getURI().toString());

        requestResponse.setResponseDuration(end-start);
        requestResponse.setResponseStatus(response.getStatus());
        requestResponse.setResponseCorrect(response.getStatus() == 200);
        requestResponse.setResponseHeaders(response.getHeaders());
        requestResponse.setResponseBody(response.getEntity(String.class));

        return requestResponse;
    }

    public RequestResponse getData(Long id) {
        WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/%d", bucketName, id)));
        WebResource.Builder builder = webResource.getRequestBuilder();
        setHeaders(builder);

        long start = System.currentTimeMillis();
        ClientResponse response = builder.get(ClientResponse.class);
        long end = System.currentTimeMillis();

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("GET");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestUrl(webResource.getURI().toString());

        requestResponse.setResponseDuration(end-start);
        requestResponse.setResponseStatus(response.getStatus());
        requestResponse.setResponseCorrect(response.getStatus() == 200);
        requestResponse.setResponseHeaders(response.getHeaders());
        requestResponse.setResponseBody(response.getEntity(String.class));

        return requestResponse;
    }

    public RequestResponse getData(Rules rules) {
        return getData(rules, true);
    }

    public RequestResponse getData(Rules rules, boolean random) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("limit", "1");
        if (random)
            queryParams.add("sort", "random");

        WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/get", bucketName)));
        webResource = webResource.queryParams(queryParams);
        WebResource.Builder builder = webResource.getRequestBuilder();
        setHeaders(builder);

        Map<String, Object> json = new HashMap<>();
        json.put("rules", rules.toNativeObject());

        String payload = gson.toJson(json);
        long start = System.currentTimeMillis();
        ClientResponse response = builder.post(ClientResponse.class, payload);
        long end = System.currentTimeMillis();

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("POST");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(payload);
        requestResponse.setRequestUrl(webResource.getURI().toString());

        requestResponse.setResponseDuration(end-start);
        requestResponse.setResponseStatus(response.getStatus());
        requestResponse.setResponseCorrect(response.getStatus() == 200);
        requestResponse.setResponseHeaders(response.getHeaders());
        requestResponse.setResponseBody(response.getEntity(String.class));

        return requestResponse;
    }

    public RequestResponse reserveData(Rules rules, boolean random) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("limit", "1");
        if (random)
            queryParams.add("sort", "random");

        WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/reserve", bucketName)));
        webResource = webResource.queryParams(queryParams);
        WebResource.Builder builder = webResource.getRequestBuilder();
        setHeaders(builder);

        Map<String, Object> json = new HashMap<>();
        json.put("rules", rules.toNativeObject());

        String payload = gson.toJson(json);
        long start = System.currentTimeMillis();
        ClientResponse response = builder.post(ClientResponse.class, payload);
        long end = System.currentTimeMillis();

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("POST");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(payload);
        requestResponse.setRequestUrl(webResource.getURI().toString());

        requestResponse.setResponseDuration(end-start);
        requestResponse.setResponseStatus(response.getStatus());
        requestResponse.setResponseCorrect(response.getStatus() == 200);
        requestResponse.setResponseHeaders(response.getHeaders());
        requestResponse.setResponseBody(response.getEntity(String.class));

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

        WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/%d", bucketName, data.getId())));
        WebResource.Builder builder = webResource.getRequestBuilder();
        setHeaders(builder);

        String payload = gson.toJson(json);
        long start = System.currentTimeMillis();
        ClientResponse response = builder.put(ClientResponse.class, payload);
        long end = System.currentTimeMillis();

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("PUT");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(payload);
        requestResponse.setRequestUrl(webResource.getURI().toString());

        requestResponse.setResponseDuration(end-start);
        requestResponse.setResponseStatus(response.getStatus());
        requestResponse.setResponseCorrect(response.getStatus() == 200);
        requestResponse.setResponseHeaders(response.getHeaders());
        requestResponse.setResponseBody(response.getEntity(String.class));

        return requestResponse;
    }

    public RequestResponse deleteData(Data data) {
        WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/%d", bucketName, data.getId())));
        WebResource.Builder builder = webResource.getRequestBuilder();
        setHeaders(builder);

        long start = System.currentTimeMillis();
        ClientResponse response = builder.delete(ClientResponse.class);
        long end = System.currentTimeMillis();

        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setRequestMethod("DELETE");
        requestResponse.setRequestHeaders(databucket.getHeaders());
        requestResponse.setRequestBody(null);
        requestResponse.setRequestUrl(webResource.getURI().toString());

        requestResponse.setResponseDuration(end-start);
        requestResponse.setResponseStatus(response.getStatus());
        requestResponse.setResponseCorrect(response.getStatus() == 201);
        requestResponse.setResponseHeaders(response.getHeaders());
        requestResponse.setResponseBody(response.getEntity(String.class));

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

        if (jsonObj.containsKey(Field.RESERVED))
            reserved = (Boolean) jsonObj.get(Field.RESERVED);

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
