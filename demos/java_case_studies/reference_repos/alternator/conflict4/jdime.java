package com.michelboudreau.test;
import com.amazonaws.services.dynamodb.model.*;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@RunWith(value = SpringJUnit4ClassRunner.class) @ContextConfiguration(locations = { "classpath:/applicationContext.xml" }) public class AlternatorBatchItemTest extends AlternatorTest {
  private String tableName;


<<<<<<< left.java
  @Test public void vanillaBatchWriteItemTest() {
    BatchWriteItemRequest batchWriteItemRequest = new BatchWriteItemRequest();
    BatchWriteItemResult result;
    Map<String, List<WriteRequest>> requestItems = new HashMap<String, List<WriteRequest>>();
    Map<String, AttributeValue> forumItem = new HashMap<String, AttributeValue>();
    forumItem.put("range", new AttributeValue().withN("1"));
    forumItem.put("range", new AttributeValue().withN("2"));
    forumItem.put("range", new AttributeValue().withN("3"));
    forumItem.put("range", new AttributeValue().withN("4"));
    forumItem.put("range", new AttributeValue().withN("5"));
    forumItem.put("range", new AttributeValue().withN("6"));
    forumItem.put("range", new AttributeValue().withN("7"));
    forumItem.put("range", new AttributeValue().withN("8"));
    List<WriteRequest> forumList = new ArrayList<WriteRequest>();
    forumList.add(new WriteRequest().withPutRequest(new PutRequest().withItem(forumItem)));
    do {
      System.out.println("Making the request.");
      batchWriteItemRequest.withRequestItems(requestItems);
      result = client.batchWriteItem(batchWriteItemRequest);
      for (Map.Entry<String, BatchWriteResponse> entry : result.getResponses().entrySet()) {
        String tableName = entry.getKey();
        Double consumedCapacityUnits = entry.getValue().getConsumedCapacityUnits();
        System.out.println("Consumed capacity units for table " + tableName + ": " + consumedCapacityUnits);
      }
      System.out.println("Unprocessed Put and Delete requests: \n" + result.getUnprocessedItems());
      requestItems = result.getUnprocessedItems();
    } while(result.getUnprocessedItems().size() > 0);
  }
=======
>>>>>>> Unknown file: This is a bug in JDime.
}