package com.mike724.networkapi;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.gson.Gson;
import com.mike724.motoloader.Security;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
        URL api = new URL("http://meyernet.co/api/get_creds.php");
        String params = String.format("key=%s", key);
        String out = basicAuthPost(api, params, username, password);

        if (out.trim() == "0") {
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

    public Object[] getObjects(HashMap<Class,String> objectMap) {
        return null;
    }

    public void writeObjects(HashMap<Object,String> objectMap) {
    }

    public List<Object> getObjectsByClass(Class c) {
        Condition hashKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue(c.getName()));

        Map<String, Condition> keyConditions = new HashMap<String, Condition>();
        keyConditions.put("object_class", hashKeyCondition);

        QueryRequest request = new QueryRequest()
                .withTableName(TABLE_NAME)
                .withAttributesToGet("object_data")
                .withKeyConditions(keyConditions);

        QueryResult result = db.query(request);

        if(result.getItems().size() < 1) return null;

        String objectData = result.getItems().get(0).get("object_data").getS();

        ArrayList<Object> objs = new ArrayList<Object>();

        for(Map<String, AttributeValue> i : result.getItems()) {
            objs.add(gson.fromJson(i.get("object_data").getS(),c));
        }

        return objs;
    }
}
