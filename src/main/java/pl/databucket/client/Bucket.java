package pl.databucket.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Bucket {

    private final Databucket databucket;
    private final String bucketName;
    private final Gson gson;


    public Bucket(Databucket databucket, String bucketName) {
        this.databucket = databucket;
        this.bucketName = bucketName;
        gson = new GsonBuilder().disableHtmlEscaping().create();
    }

    public Databucket getDatabucket() {
        return databucket;
    }

    public Data insertData(Data data) {
        Map<String, Object> json = new HashMap<>();
        if (data.getReserved() != null)
            json.put(Field.RESERVED, data.getReserved());

        if (data.getTagId() != null)
            json.put(Field.TAG_ID, data.getTagId());

        if (data.getProperties() != null)
            json.put(Field.PROPERTIES, data.getProperties());

        WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s", bucketName)));
        WebResource.Builder builder = webResource.type(MediaType.APPLICATION_JSON);
        databucket.setHeaders(builder);

        ClientResponse response = builder.post(ClientResponse.class, gson.toJson(json));
        String responseBody = response.getEntity(String.class);

        if (response.getStatus() == 201) {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = gson.fromJson(responseBody, Map.class);
            return castJsonToData(result);
        } else
            throw new RuntimeException("Response status: " + response.getStatus() + "\n\n" + responseBody);
    }


    @SuppressWarnings("unchecked")
    public Data getData(Long id) {
        WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/%d", bucketName, id)));
        WebResource.Builder builder = webResource.type(MediaType.APPLICATION_JSON);
        databucket.setHeaders(builder);

        ClientResponse response = builder.get(ClientResponse.class);
        String responseBody = response.getEntity(String.class);

        if (response.getStatus() == 200) {
            Map<String, Object> result = gson.fromJson(responseBody, Map.class);
            return castJsonToData(result);
        } else
            throw new RuntimeException("Response status: " + response.getStatus());
    }

    public Data getData(Rules rules) {
        return getData(rules, false);
    }

    public Data getData(Rules rules, boolean random) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("limit", "1");
        if (random)
            queryParams.add("sort", "random");

        WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/get", bucketName)));
        webResource = webResource.queryParams(queryParams);
        WebResource.Builder builder = webResource.type(MediaType.APPLICATION_JSON);
        databucket.setHeaders(builder);

        ClientResponse response = builder.post(ClientResponse.class, rules.toJsonString());
        String responseBody = response.getEntity(String.class);

        if (response.getStatus() == 200) {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = gson.fromJson(responseBody, Map.class);
            if (result.containsKey("data") && ((ArrayList) result.get("data")).size() > 0) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) result.get("data");
                return castJsonToData(dataList.get(0));
            } else
                return null;
        } else
            throw new RuntimeException("Response status: " + response.getStatus());
    }



    @SuppressWarnings("unchecked")
    public Data reserveData(Rules matchRules, boolean random) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("limit", "1");
        if (random)
            queryParams.add("sort", "random");

        WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/reserve", bucketName)));
        webResource = webResource.queryParams(queryParams);
        WebResource.Builder builder = webResource.type(MediaType.APPLICATION_JSON);
        databucket.setHeaders(builder);

        ClientResponse response = builder.post(ClientResponse.class, matchRules.toJsonString());
        String responseBody = response.getEntity(String.class);

        if (response.getStatus() == 200) {
            Map<String, Object> result = gson.fromJson(responseBody, Map.class);

            if (result.containsKey("data") && ((ArrayList) result.get("data")).size() > 0) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
                return castJsonToData(data.get(0));
            } else
                return null;
        } else
            throw new RuntimeException("Response status: " + response.getStatus());
    }

    public Data updateData(Data data) {
        Map<String, Object> json = new HashMap<String, Object>();
        if (data.getReserved() != null)
            json.put(Field.RESERVED, data.getReserved());

        if (data.getTagId() != null)
            json.put(Field.TAG_ID, data.getTagId());

        if (data.getProperties() != null)
            json.put("properties", data.getProperties());

        WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/%d", bucketName, data.getId())));
        WebResource.Builder builder = webResource.type(MediaType.APPLICATION_JSON);
        databucket.setHeaders(builder);

        ClientResponse response = builder.put(ClientResponse.class, gson.toJson(json));
        String responseBody = response.getEntity(String.class);

        if (response.getStatus() == 200) {
            return getData(data.getId());
        } else
            throw new RuntimeException("Response status: " + response.getStatus() + "\n\n" + responseBody);
    }

    public void deleteData(Data data) {
        WebResource webResource = databucket.getClient().resource(databucket.buildUrl(String.format("/api/bucket/%s/%d", bucketName, data.getId())));
        WebResource.Builder builder = webResource.type(MediaType.APPLICATION_JSON);
        databucket.setHeaders(builder);

        ClientResponse response = builder.delete(ClientResponse.class);
        String responseBody = response.getEntity(String.class);

        if (response.getStatus() != 200)
            throw new RuntimeException("Response status: " + response.getStatus() + "\n\n" + responseBody);
    }

    @SuppressWarnings("unchecked")
    private Data castJsonToData(Map<String, Object> jsonObj) {
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

}
