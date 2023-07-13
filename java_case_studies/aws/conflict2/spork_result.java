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

import com.amazonaws.services.dynamodb.sessionmanager.util.DynamoUtils;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.util.IOUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.catalina.Context;
import org.apache.catalina.Loader;
import org.apache.catalina.Session;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.session.StoreBase;
import org.apache.catalina.util.CustomObjectInputStream;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import static com.amazonaws.util.BinaryUtils.copyAllBytesFrom;


/**
 * Session store implementation that loads and stores HTTP sessions from Amazon DynamoDB.
 */
public class DynamoDBSessionStore extends StoreBase {
    private static final String name = "AmazonDynamoDBSessionStore";

    private static final String info = name + "/1.0";

    private AmazonDynamoDBClient dynamo;

    private String sessionTableName;

    private final Set<String> keys = Collections.synchronizedSet(new HashSet<String>());

    private static final Log logger = LogFactory.getLog(DynamoDBSessionStore.class);

private long keysTimestamp=0;

    public String getInfo() {
        return info;
    }

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
    public int getSize() throws IOException {
        TableDescription table = dynamo.describeTable(new DescribeTableRequest().withTableName(sessionTableName)).getTable();
        long itemCount = table.getItemCount();
        return ((int) (itemCount));
    }

    @Override
    public String[] keys() throws IOException {
// refresh the keys stored in memory in every hour.
        if (keysTimestamp < (System.currentTimeMillis() - ((1000L * 60) * 60))) {
// Other instances can also add or remove sessions, so we have to synchronise the keys set with the DB sometimes 
            List<String> list = DynamoUtils.loadKeys(dynamo, sessionTableName);
            keys.clear();
            keys.addAll(list);
            keysTimestamp = System.currentTimeMillis();
        }
        return keys.toArray(new String[0]);
    }

    @Override
    public Session load(String id) throws ClassNotFoundException, IOException {
        ByteBuffer byteBuffer = DynamoUtils.loadItemBySessionId(dynamo, sessionTableName, id);
        if (byteBuffer == null) {
<<<<<<< LEFT
            logger
=======
			keys
>>>>>>> RIGHT
            .remove(id);
            return null;
        }
        if (manager.getContainer().getLogger().isDebugEnabled()) {
            manager.getContainer().getLogger().debug(sm.getString(getStoreName() + ".loading", id, sessionTableName));
        }
        ByteArrayInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream ois = null;
<<<<<<< LEFT
ObjectInputStream
=======
Loader
>>>>>>> RIGHT
         loader = null;
        ClassLoader classLoader = null;
        try {
            fis = new ByteArrayInputStream(copyAllBytesFrom(byteBuffer));
            bis = new BufferedInputStream(fis);
            Context container = getAssociatedContext();
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
        } catch (java.lang.Exception e) {
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
            StandardSession session = ((StandardSession) (manager.createEmptySession()));
            session.readObjectData(ois);
            session.setManager(manager);
            keys.add(id);
            return session;
        } finally {
            try {
                ois.close();
            } catch (IOException f) {
            }
        }
    }

    /**
     * To be compatible with Tomcat7 we have to call the getContainer method rather than getContext.
     * The cast is safe as it only makes sense to use a session manager within the context of a
     * webapp, the Tomcat 8 version of getContainer just delegates to getContext. When Tomcat7 is no
     * longer supported this can be changed to getContext
     * 
     * @return The context this manager is associated with
     */
    // TODO Inline this method with getManager().getContext() when Tomcat7 is no longer supported
    private Context getAssociatedContext() {
        try {
            return 
<<<<<<< LEFT
            (Context) getManager().getContainer()
=======
            manager.getContainer()
>>>>>>> RIGHT;
        } catch (java.lang.ClassCastException e) {
            logger.fatal((("Unable to cast " + getManager().getClass().getName()) + " to a Context.") + " DynamoDB SessionManager can only be used with a Context");
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Session session) throws IOException {
        String id = session.getIdInternal();
        if (manager.getContainer().getLogger().isDebugEnabled()) {
            manager.getContainer().getLogger().debug(sm.getString(getStoreName() + ".saving", id, sessionTableName));
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
            ((StandardSession) (session)).writeObjectData(oos);
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
