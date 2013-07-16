package com.mike724.networkapi;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.KeysAndAttributes;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import org.apache.http.NameValuePair;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mike724.networkapi.HTTPUtils.basicAuthPost;

public class DataStorage {
    final static String TABLE_NAME = "object_storage";

    private AmazonDynamoDBClient db;
    private Gson gson;

    public DataStorage(String username, String password, String key) throws Exception {
        //post data
        final NameValuePair nvp = new BasicNameValuePair("key", key);
        //basic auth
        Credentials creds = new UsernamePasswordCredentials(username, password);
        //server response text
        String out = basicAuthPost("https://agentgaming.net/api/get_creds.php", new ArrayList<NameValuePair>() {{ add(nvp); }}, creds);

        if (out.trim().equals("0")) {
            throw new Exception("Nice try.");
        }

        String decrypted = Security.decrypt(out, "l215/n13f63902pa");
        InputStream is = new ByteArrayInputStream(decrypted.getBytes());

        AWSCredentials credentials = new PropertiesCredentials(is);

        db = new AmazonDynamoDBClient(credentials);
        gson = new Gson();
    }

    public Object getObject(Class c, String id) {
        Condition hashKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue(c.getName()));

        Condition rangeKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue(id));

        Map<String, Condition> keyConditions = new HashMap<String, Condition>();
        keyConditions.put("object_class", hashKeyCondition);
        keyConditions.put("object_id", rangeKeyCondition);

        QueryRequest request = new QueryRequest()
                .withTableName(TABLE_NAME)
                .withAttributesToGet("object_data")
                .withKeyConditions(keyConditions)
                .withLimit(1);

        QueryResult result = db.query(request);

        if(result.getItems().size() < 1) return null;

        String objectData = result.getItems().get(0).get("object_data").getS();

        return gson.fromJson(objectData, c);
    }

    public void writeObject(Object o, String id) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("object_class", new AttributeValue(o.getClass().getName()));
        item.put("object_id",new AttributeValue(id));
        item.put("object_data",new AttributeValue(gson.toJson(o)));

        PutItemRequest putItemRequest = new PutItemRequest(TABLE_NAME, item);
        PutItemResult putItemResult = db.putItem(putItemRequest);
    }

    public Multimap<String,Object> getObjects(Class c, String... ids) {
        Map<String,KeysAndAttributes> items = new HashMap<String,KeysAndAttributes>();
        ArrayList<Map<String, AttributeValue>> keys = new ArrayList<Map<String, AttributeValue>>();
        for(String s : ids) {
            String key = c.getName();
            String range = s;

            HashMap<String, AttributeValue> data = new HashMap<String, AttributeValue>();
            data.put("object_class", new AttributeValue().withS(key));
            data.put("object_id", new AttributeValue().withS(range));
            keys.add(data);
        }
        items.put(TABLE_NAME, new KeysAndAttributes().withKeys(keys));
        BatchGetItemRequest bgir = new BatchGetItemRequest().withRequestItems(items);
        BatchGetItemResult result = db.batchGetItem(bgir);

        if(result.getResponses().get(TABLE_NAME).size() < 1) return null;

        Multimap<String,Object> objs = ArrayListMultimap.create();

        for(Map<String, AttributeValue> i : result.getResponses().get(TABLE_NAME)) {
            String id = i.get("object_id").getS();
            objs.put(id,gson.fromJson(i.get("object_data").getS(),c));
        }

        return objs;
    }

    public void writeObjects(HashMap<Object,String> objectMap) {
        HashMap<String,List<WriteRequest>> reqItems = new HashMap<String,List<WriteRequest>>();
        ArrayList<WriteRequest> items = new ArrayList<WriteRequest>();
        for(Object o : objectMap.keySet()) {
            Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
            item.put("object_class", new AttributeValue(o.getClass().getName()));
            item.put("object_id",new AttributeValue(objectMap.get(o)));
            item.put("object_data",new AttributeValue(gson.toJson(o)));
            PutRequest pr = new PutRequest().withItem(item);
            WriteRequest wr = new WriteRequest().withPutRequest(pr);
            items.add(wr);
        }

        reqItems.put(TABLE_NAME,items);

        BatchWriteItemRequest bwir = new BatchWriteItemRequest().withRequestItems(reqItems);
        BatchWriteItemResult result = db.batchWriteItem(bwir);
    }

    public HashMap<String,Object> getObjectsByClass(Class c) {
        Condition hashKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue(c.getName()));

        Map<String, Condition> keyConditions = new HashMap<String, Condition>();
        keyConditions.put("object_class", hashKeyCondition);

        QueryRequest request = new QueryRequest()
                .withTableName(TABLE_NAME)
                .withAttributesToGet("object_data", "object_id")
                .withKeyConditions(keyConditions);

        QueryResult result = db.query(request);

        if(result.getItems().size() < 1) return null;

        String objectData = result.getItems().get(0).get("object_data").getS();

        HashMap<String,Object> objs = new HashMap<String,Object>();

        for(Map<String, AttributeValue> i : result.getItems()) {
            objs.put(i.get("object_id").getS(), gson.fromJson(i.get("object_data").getS(), c));
        }

        return objs;
    }

    public HashMap<String,Object> getObjectsByClass(Class c, Integer limit) {
        Condition hashKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue(c.getName()));

        Map<String, Condition> keyConditions = new HashMap<String, Condition>();
        keyConditions.put("object_class", hashKeyCondition);

        QueryRequest request = new QueryRequest()
                .withTableName(TABLE_NAME)
                .withAttributesToGet("object_data", "object_id")
                .withKeyConditions(keyConditions)
                .withLimit(limit);

        QueryResult result = db.query(request);

        if(result.getItems().size() < 1) return null;

        String objectData = result.getItems().get(0).get("object_data").getS();

        HashMap<String,Object> objs = new HashMap<String,Object>();

        for(Map<String, AttributeValue> i : result.getItems()) {
            objs.put(i.get("object_id").getS(), gson.fromJson(i.get("object_data").getS(), c));
        }

        return objs;
    }
}
