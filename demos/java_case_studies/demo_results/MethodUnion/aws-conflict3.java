/*
 * Copyright 2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.services.dynamodb.sessionmanager.util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.apache.catalina.Session;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.dynamodb.sessionmanager.DynamoDBSessionManager;
import com.amazonaws.services.dynamodb.sessionmanager.SessionTableAttributes;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import java.util.ArrayList;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import java.util.List;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.Select;

public class DynamoUtils{

    public static final String SESSION_ID_KEY = "sessionId";,
    public static final String SESSION_DATA_ATTRIBUTE = "sessionData";,

    public static Map<String, AttributeValue> loadItemBySessionId(AmazonDynamoDB dynamo,
                                                                  String tableName,
                                                                  String sessionId) {
        Map<String, AttributeValue> map = newAttributeValueMap();
        map.put(SessionTableAttributes.SESSION_ID_KEY, new AttributeValue(sessionId));
        GetItemRequest request = new GetItemRequest(tableName, map);
        appendUserAgent(request);

        try {
            GetItemResult result = dynamo.getItem(request);
            return result.getItem();
        } catch (Exception e) {
            DynamoDBSessionManager.warn("Unable to load session " + sessionId, e);
        }

        return null;
    }

    public static List<String> loadKeys(AmazonDynamoDB dynamo, String tableName) {
		ScanRequest request = new ScanRequest(tableName);
		request.setSelect(Select.SPECIFIC_ATTRIBUTES);
		request.withAttributesToGet(SESSION_ID_KEY);

		ArrayList<String> list = new ArrayList<String>();
		ScanResult scanResult = null;
		do {
			if (scanResult != null)
				request.setExclusiveStartKey(scanResult.getLastEvaluatedKey());

			scanResult = dynamo.scan(request);
			List<Map<String, AttributeValue>> items = scanResult.getItems();
			for (Map<String, AttributeValue> item : items) {
				list.add(item.get(SESSION_ID_KEY).getS());
			}
		} while (scanResult.getLastEvaluatedKey() != null);

		return list;
	}

    public static void deleteSession(AmazonDynamoDB dynamo, String tableName, String sessionId) {
		Map<String, AttributeValue> key = newAttributeValueMap();
		key.put(SESSION_ID_KEY, new AttributeValue(sessionId));

<<<<<<< left_content.java
		DeleteItemRequest request = new DeleteItemRequest(tableName, key);
		addClientMarker(request);
=======
        DeleteItemRequest request = new DeleteItemRequest(tableName, key);
        appendUserAgent(request);
>>>>>>> right_content.java

		try {
			dynamo.deleteItem(request);
		} catch (Exception e) {
			DynamoDBSessionManager.warn("Unable to delete session " + sessionId, e);
		}
	}


    public static ByteBuffer loadItemBySessionId(AmazonDynamoDB dynamo, String tableName, String sessionId) {
		Map<String, AttributeValue> key = newAttributeValueMap();
		key.put(SESSION_ID_KEY, new AttributeValue(sessionId));
		GetItemRequest request = new GetItemRequest(tableName, key);
		addClientMarker(request);
		try {
			Map<String, AttributeValue> item = dynamo.getItem(request).getItem();
			if (item == null || !item.containsKey(SESSION_ID_KEY) || !item.containsKey(SESSION_DATA_ATTRIBUTE)) {
				DynamoDBSessionManager.warn("Unable to load session attributes for session " + sessionId);
				return null;
			}
			return item.get(SESSION_DATA_ATTRIBUTE).getB();
		} catch (Exception e) {
			DynamoDBSessionManager.warn("Unable to load session " + sessionId, e);
		}
		return null;
	}

    public static void storeSession(AmazonDynamoDB dynamo, String tableName, Session session) throws IOException {
        Map<String, Object> sessionAttributes = new HashMap<String, Object>();

        HttpSession httpSession = session.getSession();
        Enumeration<String> attributeNames = httpSession.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = httpSession.getAttribute(attributeName);
            sessionAttributes.put(attributeName, attributeValue);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(sessionAttributes);
        objectOutputStream.close();

        byte[] byteArray = byteArrayOutputStream.toByteArray();

        Map<String, AttributeValue> attributes = newAttributeValueMap();
        attributes.put(SessionTableAttributes.SESSION_ID_KEY, new AttributeValue(session.getId()));
        ByteBuffer b = ByteBuffer.wrap(byteArray);
        attributes.put(SessionTableAttributes.SESSION_DATA_ATTRIBUTE, new AttributeValue().withB(b));
        attributes.put(SessionTableAttributes.CREATED_AT_ATTRIBUTE,
                new AttributeValue().withN(Long.toString(session.getCreationTime())));
        attributes.put(SessionTableAttributes.LAST_UPDATED_AT_ATTRIBUTE,
                new AttributeValue().withN(Long.toString(System.currentTimeMillis())));

        try {
            PutItemRequest request = new PutItemRequest(tableName, attributes);
            appendUserAgent(request);
            dynamo.putItem(request);
        } catch (Exception e) {
            DynamoDBSessionManager.error("Unable to save session " + session.getId(), e);
        }
    }

    public static void storeSession(AmazonDynamoDB dynamo, String tableName, String sessionId, ByteBuffer byteBuffer)
			throws IOException {

		Map<String, AttributeValue> attributes = newAttributeValueMap();
		attributes.put(SESSION_ID_KEY, new AttributeValue(sessionId));
		attributes.put(SESSION_DATA_ATTRIBUTE, new AttributeValue().withB(byteBuffer));

		try {
			PutItemRequest request = new PutItemRequest(tableName, attributes);
			addClientMarker(request);
			dynamo.putItem(request);
		} catch (Exception e) {
			DynamoDBSessionManager.error("Unable to save session " + sessionId, e);
		}
	}

    public static boolean doesTableExist(AmazonDynamoDBClient dynamo, String tableName) {
<<<<<<< left_content.java
		try {
			DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
			addClientMarker(request);

			TableDescription table = dynamo.describeTable(request).getTable();
			if (table == null)
				return false;
			else
				return true;
		} catch (AmazonServiceException ase) {
			if (ase.getErrorCode().equalsIgnoreCase("ResourceNotFoundException"))
				return false;
			else
				throw ase;
		}
	}
=======
        try {
            DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
            appendUserAgent(request);

            TableDescription table = dynamo.describeTable(request).getTable();
            return table != null;
        } catch (AmazonServiceException ase) {
            if (ase.getErrorCode().equalsIgnoreCase("ResourceNotFoundException")) {
                return false;
            } else {
                throw ase;
            }
        }
    }
>>>>>>> right_content.java


    public static void waitForTableToBecomeActive(AmazonDynamoDBClient dynamo, String tableName) {
<<<<<<< left_content.java
		long startTime = System.currentTimeMillis();
		long endTime = startTime + (10 * 60 * 1000);
		while (System.currentTimeMillis() < endTime) {
			try {
				DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
				addClientMarker(request);

				TableDescription tableDescription = dynamo.describeTable(request).getTable();
				if (tableDescription == null)
					continue;

				String tableStatus = tableDescription.getTableStatus();
				if (tableStatus.equals(TableStatus.ACTIVE.toString()))
					return;
			} catch (AmazonServiceException ase) {
				if (ase.getErrorCode().equalsIgnoreCase("ResourceNotFoundException") == false)
					throw ase;
			}

			try {
				Thread.sleep(1000 * 5);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new AmazonClientException("Interrupted while waiting for table '" + tableName
						+ "' to become active.", e);
			}
		}
=======
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (10 * 60 * 1000);
        while (System.currentTimeMillis() < endTime) {
            try {
                DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
                appendUserAgent(request);

                TableDescription tableDescription = dynamo.describeTable(request).getTable();
                if (tableDescription == null) {
                    continue;
                }

                String tableStatus = tableDescription.getTableStatus();
                if (tableStatus.equals(TableStatus.ACTIVE.toString())) {
                    return;
                }
            } catch (AmazonServiceException ase) {
                if (!ase.getErrorCode().equalsIgnoreCase("ResourceNotFoundException")) {
                    throw ase;
                }
            }

            try {
                Thread.sleep(1000 * 5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AmazonClientException("Interrupted while waiting for table '" + tableName
                        + "' to become active.", e);
            }
        }
>>>>>>> right_content.java

		throw new AmazonClientException("Table '" + tableName + "' never became active");
	}


    public static void createSessionTable(AmazonDynamoDBClient dynamo, String tableName, long readCapacityUnits, long writeCapacityUnits) {
        CreateTableRequest request = new CreateTableRequest().withTableName(tableName);
        addClientMarker(request);

        request.withKeySchema(new KeySchemaElement()
            .withAttributeName(SessionTableAttributes.SESSION_ID_KEY)
            .withKeyType(KeyType.HASH));

        request.withAttributeDefinitions(new AttributeDefinition()
            .withAttributeName(SessionTableAttributes.SESSION_ID_KEY)
            .withAttributeType(ScalarAttributeType.S));

        request.setProvisionedThroughput(new ProvisionedThroughput()
            .withReadCapacityUnits(readCapacityUnits)
            .withWriteCapacityUnits(writeCapacityUnits));

        dynamo.createTable(request);
    }

    public static void createSessionTable(AmazonDynamoDBClient dynamo,
                                          String tableName,
                                          long readCapacityUnits,
                                          long writeCapacityUnits) {
        CreateTableRequest request = new CreateTableRequest().withTableName(tableName);
        appendUserAgent(request);

        request.withKeySchema(new KeySchemaElement()
            .withAttributeName(SessionTableAttributes.SESSION_ID_KEY)
            .withKeyType(KeyType.HASH));

        request.withAttributeDefinitions(new AttributeDefinition()
            .withAttributeName(SessionTableAttributes.SESSION_ID_KEY)
            .withAttributeType(ScalarAttributeType.S));

        request.setProvisionedThroughput(new ProvisionedThroughput()
            .withReadCapacityUnits(readCapacityUnits)
            .withWriteCapacityUnits(writeCapacityUnits));

        dynamo.createTable(request);
    }

    public static void createSessionTable(AmazonDynamoDBClient dynamo, String tableName, long readCapacityUnits,
			long writeCapacityUnits) {
		CreateTableRequest request = new CreateTableRequest().withTableName(tableName);
		addClientMarker(request);

		request.withKeySchema(new KeySchemaElement().withAttributeName(SESSION_ID_KEY).withKeyType(KeyType.HASH));

		request.withAttributeDefinitions(new AttributeDefinition().withAttributeName(SESSION_ID_KEY).withAttributeType(
				ScalarAttributeType.S));

		request.setProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(readCapacityUnits)
				.withWriteCapacityUnits(writeCapacityUnits));

		dynamo.createTable(request);
	}

    public static void addClientMarker(AmazonWebServiceRequest request) {
		request.getRequestClientOptions().addClientMarker("DynamoSessionManager/2.0");
	}

    private static Map<String, AttributeValue> newAttributeValueMap() {
		return new HashMap<String, AttributeValue>();
	}

    public static void appendUserAgent(AmazonWebServiceRequest request) {
        request.getRequestClientOptions().appendUserAgent("DynamoSessionManager/1.0");
    }

}