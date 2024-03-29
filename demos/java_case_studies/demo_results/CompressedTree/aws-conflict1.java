package com.amazonaws.services.dynamodb.sessionmanager;
import java.io.File;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.session.PersistentManagerBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.dynamodb.sessionmanager.util.DynamoUtils;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.util.StringUtils;
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




/**
 * Tomcat persistent session manager implementation that uses Amazon DynamoDB to store HTTP session
 * data.
 */
public class DynamoDBSessionManager extends PersistentManagerBase {

    private static final String DEFAULT_TABLE_NAME = "Tomcat_SessionState";

    private static final String name = "AmazonDynamoDBSessionManager";
    private static final String info = name + "/2.0";

    private String regionId = "us-east-1";
    private String endpoint;
    private File credentialsFile;
    private String accessKey;
    private String secretKey;
    private long readCapacityUnits = 10;
    private long writeCapacityUnits = 5;
    private boolean createIfNotExist = true;
    private String tableName = DEFAULT_TABLE_NAME;
    private String proxyHost;
    private Integer proxyPort;

    private final DynamoDBSessionStore dynamoSessionStore;

<<<<<<< left_content.java
    private static final Log logger = LogFactory.getLog(DynamoDBSessionManager.class);
=======
    private static Log logger;

>>>>>>> right_content.java

    public DynamoDBSessionManager() {
        dynamoSessionStore = new DynamoDBSessionStore();
        setStore(dynamoSessionStore);
        setSaveOnRestart(true);

        // MaxInactiveInterval controls when sessions are removed from the store
        setMaxInactiveInterval(60 * 60 * 2); // 2 hours

        // MaxIdleBackup controls when sessions are persisted to the store
        setMaxIdleBackup(30); // 30 seconds
    }

    public String getInfo() {
        return info;
    }

    @Override
    public String getName() {
        return name;
    }

    //
    // Context.xml Configuration Members
    //

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setAwsAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setAwsSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setAwsCredentialsFile(String credentialsFile) {
        this.credentialsFile = new File(credentialsFile);
    }

    public void setTable(String table) {
        this.tableName = table;
    }

    public void setReadCapacityUnits(int readCapacityUnits) {
        this.readCapacityUnits = readCapacityUnits;
    }

    public void setWriteCapacityUnits(int writeCapacityUnits) {
        this.writeCapacityUnits = writeCapacityUnits;
    }

    public void setCreateIfNotExist(boolean createIfNotExist) {
        this.createIfNotExist = createIfNotExist;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    //
    // Private Interface
    //

    @Override
    protected void initInternal() throws LifecycleException {
        this.setDistributable(true);

        AWSCredentialsProvider credentialsProvider = initCredentials();
        ClientConfiguration clientConfiguration = initClientConfiguration();

        AmazonDynamoDBClient dynamo = new AmazonDynamoDBClient(credentialsProvider, clientConfiguration);
        if (this.regionId != null) {
            dynamo.setRegion(RegionUtils.getRegion(this.regionId));
        }
        if (this.endpoint != null) {
            dynamo.setEndpoint(this.endpoint);
        }

        initDynamoTable(dynamo);

        // init session store
        dynamoSessionStore.setDynamoClient(dynamo);
        dynamoSessionStore.setSessionTableName(this.tableName);
<<<<<<< left_content.java
=======

    }

    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        super.stopInternal();
>>>>>>> right_content.java
    }

    private void initDynamoTable(AmazonDynamoDBClient dynamo) {
        boolean tableExists = DynamoUtils.doesTableExist(dynamo, this.tableName);

        if (!tableExists && !createIfNotExist) {
            throw new AmazonClientException("Session table '" + tableName + "' does not exist, "
                    + "and automatic table creation has been disabled in context.xml");
        }

        if (!tableExists) {
            DynamoUtils.createSessionTable(dynamo, this.tableName, this.readCapacityUnits, this.writeCapacityUnits);
        }

        DynamoUtils.waitForTableToBecomeActive(dynamo, this.tableName);
    }

    private AWSCredentialsProvider initCredentials() {
        // Attempt to use any credentials specified in context.xml first
        if (credentialsExistInContextConfig()) {
            // Fail fast if credentials aren't valid as user has likely made a configuration mistake
            if (credentialsInContextConfigAreValid()) {
                throw new AmazonClientException("Incomplete AWS security credentials specified in context.xml.");
            }
            debug("Using AWS access key ID and secret key from context.xml");
            return new StaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
        }

        // Use any explicitly specified credentials properties file next
        if (credentialsFile != null) {
            try {
                debug("Reading security credentials from properties file: " + credentialsFile);
                PropertiesCredentials credentials = new PropertiesCredentials(credentialsFile);
                debug("Using AWS credentials from file: " + credentialsFile);
                return new StaticCredentialsProvider(credentials);
            } catch (Exception e) {
                throw new AmazonClientException(
                        "Unable to read AWS security credentials from file specified in context.xml: "
                                + credentialsFile, e);
            }
        }

        // Fall back to the default credentials chain provider if credentials weren't explicitly set
        AWSCredentialsProvider defaultChainProvider = new DefaultAWSCredentialsProviderChain();
        if (defaultChainProvider.getCredentials() == null) {
            debug("Loading security credentials from default credentials provider chain.");
            throw new AmazonClientException(
                    "Unable find AWS security credentials.  "
                            + "Searched JVM system properties, OS env vars, and EC2 instance roles.  "
                            + "Specify credentials in Tomcat's context.xml file or put them in one of the places mentioned above.");
        }
        debug("Using default AWS credentials provider chain to load credentials");
        return defaultChainProvider;
    }

    /**
     * @return True if the user has set their AWS credentials either partially or completely in
     *         context.xml. False otherwise
     */
    private boolean credentialsExistInContextConfig() {
        return accessKey != null || secretKey != null;
    }

    /**
     * @return True if both the access key and secret key were set in context.xml config. False
     *         otherwise
     */
    private boolean credentialsInContextConfigAreValid() {
        return StringUtils.isNullOrEmpty(accessKey) || StringUtils.isNullOrEmpty(secretKey);
    }

    private ClientConfiguration initClientConfiguration() {
        ClientConfiguration clientConfiguration = new ClientConfiguration();

        // Attempt to use an explicit proxy configuration
        if (proxyHost != null || proxyPort != null) {
            debug("Reading proxy settings from context.xml");
            if (proxyHost == null || proxyPort == null) {
                throw new AmazonClientException("Incomplete proxy settings specified in context.xml."
                        + " Both proxy hot and proxy port needs to be specified");
            }
            debug("Using proxy host and port from context.xml");
            clientConfiguration.withProxyHost(proxyHost).withProxyPort(proxyPort);
        }

        return clientConfiguration;
    }

    //
    // Logger Utility Functions
    //

    public static void debug(String s) {
        logger.debug(s);
    }

    public static void warn(String s) {
        logger.warn(s);
    }

    public static void warn(String s, Exception e) {
        logger.warn(s, e);
    }

    public static void error(String s) {
        logger.error(s);
    }

    public static void error(String s, Exception e) {
        logger.error(s, e);
    }
}

