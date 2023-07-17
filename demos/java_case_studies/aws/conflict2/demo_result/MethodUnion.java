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
package com.amazonaws.services.dynamodb.sessionmanager;
import static com.amazonaws.util.BinaryUtils.copyAllBytesFrom;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.catalina.Context;
import org.apache.catalina.Session;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.session.StoreBase;
import org.apache.catalina.util.CustomObjectInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import org.apache.juli.logging.Log;
import java.io.ByteArrayOutputStream;
import org.apache.juli.logging.LogFactory;
import com.amazonaws.services.dynamodb.sessionmanager.util.DynamoUtils;
import java.io.ObjectOutputStream;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import java.util.List;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.util.IOUtils;
import org.apache.catalina.Container;
import org.apache.catalina.Loader;

public class DynamoDBSessionStore extends StoreBase{

    private static final String name = "AmazonDynamoDBSessionStore";
    private static final String info = name + "/1.0";
    private AmazonDynamoDBClient dynamo;
    private String sessionTableName;
    private Set<String> keys = Collections.synchronizedSet(new HashSet<String>());
    private long keysTimestamp=0;
    private final Set<String> keys = Collections.synchronizedSet(new HashSet<String>());
    private static final Log logger = LogFactory.getLog(DynamoDBSessionStore.class);
    <<<<<<< left_content.java
@Override
	public String getInfo() {
		return info;
	}
=======
public String getInfo() {
        return info;
    }
>>>>>>> right_content.java

    @Override
	public String getStoreName() {
		return name;
	}
    public void setDynamoClient(AmazonDynamoDBClient dynamo) {
		this.dynamo = dynamo;
	}
    public void setSessionTableName(String tableName) {
		this.sessionTableName = tableName;
	}
    @Override
	public void clear() throws IOException {
		final Set<String> keysCopy = new HashSet<String>();
		keysCopy.addAll(keys);

		new Thread("dynamodb-session-manager-clear") {
			@Override
			public void run() {
				for (String sessionId : keysCopy) {
					remove(sessionId);
				}
			}
		}.start();

	}
    @Override
<<<<<<< left_content.java
	public int getSize() throws IOException {
		TableDescription table = dynamo.describeTable(new DescribeTableRequest().withTableName(sessionTableName))
				.getTable();
		long itemCount = table.getItemCount();

		return (int) itemCount;
	}
=======
    public int getSize() throws IOException {
        // The item count from describeTable is updated every ~6 hours
        TableDescription table = dynamo.describeTable(new DescribeTableRequest().withTableName(sessionTableName))
                .getTable();
        long itemCount = table.getItemCount();

        return (int) itemCount;
    }
>>>>>>> right_content.java

    @Override
	public String[] keys() throws IOException {
		// refresh the keys stored in memory in every hour.
		if(keysTimestamp<System.currentTimeMillis()-1000L*60*60) {
			// Other instances can also add or remove sessions, so we have to synchronise the keys set with the DB sometimes 
			List<String> list=DynamoUtils.loadKeys(dynamo, sessionTableName);
			keys.clear();
			keys.addAll(list);
			keysTimestamp=System.currentTimeMillis();
		}
		
		return keys.toArray(new String[0]);

	}
    @Override
<<<<<<< left_content.java
	public Session load(String id) throws ClassNotFoundException, IOException {

		ByteBuffer byteBuffer = DynamoUtils.loadItemBySessionId(dynamo, sessionTableName, id);
		if (byteBuffer == null) {
			keys.remove(id);
			return (null);
		}

		if (manager.getContainer().getLogger().isDebugEnabled()) {
			manager.getContainer().getLogger().debug(sm.getString(getStoreName() + ".loading", id, sessionTableName));
		}

		ByteArrayInputStream fis = null;
		BufferedInputStream bis = null;
		ObjectInputStream ois = null;
		Loader loader = null;
		ClassLoader classLoader = null;
		try {
			fis = new ByteArrayInputStream(byteBuffer.array());
			bis = new BufferedInputStream(fis);
			Container container = manager.getContainer();
			if (container != null) {
				loader = container.getLoader();
			}
			if (loader != null) {
				classLoader = loader.getClassLoader();
			}
			if (classLoader != null) {
				ois = new CustomObjectInputStream(bis, classLoader);
			} else {
				ois = new ObjectInputStream(bis);
			}
		} catch (Exception e) {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException f) {
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException f) {
				}
			}
			throw e;
		}

		try {
			StandardSession session = (StandardSession) manager.createEmptySession();
			session.readObjectData(ois);
			session.setManager(manager);
			keys.add(id);
			return (session);
		} finally {
			try {
				ois.close();
			} catch (IOException f) {
			}
		}
	}
=======
    public Session load(String id) throws ClassNotFoundException, IOException {
        Map<String, AttributeValue> item = DynamoUtils.loadItemBySessionId(dynamo, sessionTableName, id);
        if (item == null || !item.containsKey(SessionTableAttributes.SESSION_ID_KEY)
                || !item.containsKey(SessionTableAttributes.SESSION_DATA_ATTRIBUTE)) {
            logger.warn("Unable to load session attributes for session " + id);
            return null;
        }

        Session session = getManager().createSession(id);
        session.setCreationTime(Long.parseLong(item.get(SessionTableAttributes.CREATED_AT_ATTRIBUTE).getN()));

        ByteBuffer byteBuffer = item.get(SessionTableAttributes.SESSION_DATA_ATTRIBUTE).getB();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(copyAllBytesFrom(byteBuffer));

        Object readObject;
        ObjectInputStream objectInputStream = null;
        try {
            Context webapp = getAssociatedContext();
            objectInputStream = new CustomObjectInputStream(inputStream, webapp.getLoader().getClassLoader());

            readObject = objectInputStream.readObject();
        } finally {
            IOUtils.closeQuietly(objectInputStream, null);
        }

        if (readObject instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> sessionAttributeMap = (Map<String, Object>) readObject;

            for (String s : sessionAttributeMap.keySet()) {
                ((StandardSession) session).setAttribute(s, sessionAttributeMap.get(s));
            }
        } else {
            throw new RuntimeException("Error: Unable to unmarshall session attributes from DynamoDB store");
        }

        keys.add(id);
        manager.add(session);

        return session;
    }
>>>>>>> right_content.java


    /**
     * To be compatible with Tomcat7 we have to call the getContainer method rather than getContext.
     * The cast is safe as it only makes sense to use a session manager within the context of a
     * webapp, the Tomcat 8 version of getContainer just delegates to getContext. When Tomcat7 is no
     * longer supported this can be changed to getContext
     * 
     * @return The context this manager is associated with
     */
    @Override
    public void remove(String id) throws IOException {
        DynamoUtils.deleteSession(dynamo, sessionTableName, id);
        keys.remove(id);
    }
    private Context getAssociatedContext() {
        try {
            return (Context) getManager().getContainer();
        } catch (ClassCastException e) {
            logger.fatal("Unable to cast " + getManager().getClass().getName() + " to a Context."
                    + " DynamoDB SessionManager can only be used with a Context");
            throw new IllegalStateException(e);
        }
    }
    @Override
	public void save(Session session) throws IOException {

		String id = session.getIdInternal();

		if (manager.getContainer().getLogger().isDebugEnabled()) {
			manager.getContainer().getLogger()
					.debug(sm.getString(getStoreName() + ".saving", id, sessionTableName));
		}

		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new BufferedOutputStream(fos));
		} catch (IOException e) {
			try {
				fos.close();
			} catch (IOException f) {
			}
			throw e;
		}

		try {
			((StandardSession) session).writeObjectData(oos);
		} finally {
			oos.close();
		}
		DynamoUtils.storeSession(dynamo, sessionTableName, id, ByteBuffer.wrap(fos.toByteArray()));
		keys.add(id);
	}
    @Override
	public void remove(String id) {
		if (manager.getContainer().getLogger().isDebugEnabled()) {
			manager.getContainer().getLogger().debug(sm.getString(getStoreName() + ".removing", id, sessionTableName));
		}
		DynamoUtils.deleteSession(dynamo, sessionTableName, id);
		keys.remove(id);
	}

}