package com.michelboudreau.alternator;
import org.apache.commons.logging.Log;
import com.amazonaws.*;
import org.apache.commons.logging.LogFactory;
import com.amazonaws.services.dynamodb.model.*;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.services.dynamodb.AmazonDynamoDB;
import com.amazonaws.services.dynamodb.model.BatchGetItemRequest;
import com.amazonaws.services.dynamodb.model.BatchGetItemResult;
import com.amazonaws.services.dynamodb.model.BatchWriteItemRequest;
import com.amazonaws.services.dynamodb.model.BatchWriteItemResult;
import com.amazonaws.services.dynamodb.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodb.model.CreateTableRequest;
import com.amazonaws.services.dynamodb.model.CreateTableResult;
import com.amazonaws.services.dynamodb.model.DeleteItemRequest;
import com.amazonaws.services.dynamodb.model.DeleteItemResult;
import com.amazonaws.services.dynamodb.model.DeleteTableRequest;
import com.amazonaws.services.dynamodb.model.DeleteTableResult;
import com.amazonaws.services.dynamodb.model.DescribeTableRequest;
import com.amazonaws.services.dynamodb.model.DescribeTableResult;
import com.amazonaws.services.dynamodb.model.GetItemRequest;
import com.amazonaws.services.dynamodb.model.GetItemResult;
import com.amazonaws.services.dynamodb.model.ListTablesRequest;
import com.amazonaws.services.dynamodb.model.ListTablesResult;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.dynamodb.model.PutItemResult;
import com.amazonaws.services.dynamodb.model.QueryRequest;
import com.amazonaws.services.dynamodb.model.QueryResult;
import com.amazonaws.services.dynamodb.model.ScanRequest;
import com.amazonaws.services.dynamodb.model.ScanResult;
import com.amazonaws.services.dynamodb.model.UpdateItemRequest;
import com.amazonaws.services.dynamodb.model.UpdateItemResult;
import com.amazonaws.services.dynamodb.model.UpdateTableRequest;
import com.amazonaws.services.dynamodb.model.UpdateTableResult;

public class AlternatorDBInProcessClient extends AmazonWebServiceClient implements AmazonDynamoDB{

    private static final Log log = LogFactory.getLog(AlternatorDBInProcessClient.class);,
    private AlternatorDBHandler handler = new AlternatorDBHandler();,

    public AlternatorDBInProcessClient() {
		this(new ClientConfiguration());
	}

    public AlternatorDBInProcessClient(ClientConfiguration clientConfiguration) {
		super(clientConfiguration);
		init();
	}

    private void init() {
	}

    public ListTablesResult listTables(ListTablesRequest listTablesRequest)
			throws AmazonServiceException, AmazonClientException {
		return handler.listTables(listTablesRequest);
	}

    public QueryResult query(QueryRequest queryRequest)
			throws AmazonServiceException, AmazonClientException {
		return handler.query(queryRequest);
	}

    public BatchWriteItemResult batchWriteItem(BatchWriteItemRequest batchWriteItemRequest)
			throws AmazonServiceException, AmazonClientException {
		return handler.batchWriteItem(batchWriteItemRequest);
	}

    public DescribeTableResult describeTable(DescribeTableRequest describeTableRequest)
			throws AmazonServiceException, AmazonClientException {
		return handler.describeTable(describeTableRequest);
	}

    public ScanResult scan(ScanRequest scanRequest)
			throws AmazonServiceException, AmazonClientException {
		return handler.scan(scanRequest);
	}

    public UpdateItemResult updateItem(UpdateItemRequest updateItemRequest)
			throws AmazonServiceException, AmazonClientException {
<<<<<<< left_content.java
		return handler.updateItem(updateItemRequest);
=======

            return handler.updateItem(updateItemRequest);
>>>>>>> right_content.java
	}


    public PutItemResult putItem(PutItemRequest putItemRequest)
<<<<<<< left_content.java
			throws AmazonServiceException, AmazonClientException, ConditionalCheckFailedException {
		return handler.putItem(putItemRequest);
	}
=======
			throws AmazonServiceException, AmazonClientException {

            return handler.putItem(putItemRequest);
}
>>>>>>> right_content.java


    public BatchGetItemResult batchGetItem(BatchGetItemRequest batchGetItemRequest)
			throws AmazonServiceException, AmazonClientException {
		return handler.batchGetItem(batchGetItemRequest);
	}

    public ListTablesResult listTables() throws AmazonServiceException, AmazonClientException {
		return listTables(new ListTablesRequest());
	}

    public CreateTableResult createTable(CreateTableRequest createTableRequest)
			throws AmazonServiceException, AmazonClientException {
<<<<<<< left_content.java
		return handler.createTable(createTableRequest);
=======

            return handler.createTable(createTableRequest);
>>>>>>> right_content.java
	}


    public UpdateTableResult updateTable(UpdateTableRequest updateTableRequest)
			throws AmazonServiceException, AmazonClientException {
<<<<<<< left_content.java
		return handler.updateTable(updateTableRequest);
=======

            return handler.updateTable(updateTableRequest);
>>>>>>> right_content.java
	}


    public DeleteTableResult deleteTable(DeleteTableRequest deleteTableRequest)
			throws AmazonServiceException, AmazonClientException {
<<<<<<< left_content.java
		return handler.deleteTable(deleteTableRequest);
=======

            return handler.deleteTable(deleteTableRequest);
>>>>>>> right_content.java
	}


    public DeleteItemResult deleteItem(DeleteItemRequest deleteItemRequest)
			throws AmazonServiceException, AmazonClientException {
<<<<<<< left_content.java
		return handler.deleteItem(deleteItemRequest);
	}
=======

            return handler.deleteItem(deleteItemRequest);
    }
>>>>>>> right_content.java


    public GetItemResult getItem(GetItemRequest getItemRequest)
			throws AmazonServiceException, AmazonClientException {
<<<<<<< left_content.java
		return handler.getItem(getItemRequest);
=======

            return handler.getItem(getItemRequest);
>>>>>>> right_content.java
	}


    @Override
	public void setEndpoint(String endpoint) throws IllegalArgumentException {
		super.setEndpoint(endpoint);
	}

    @Override
	public ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest request) {
		return client.getResponseMetadataForRequest(request);
	}

}