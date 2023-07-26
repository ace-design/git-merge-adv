package org.mongojack;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DB;
import java.util.HashSet;
import com.mongodb.DBCollection;
import java.util.Map;
import com.mongodb.Mongo;
import java.util.Random;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * Base class for unit tests that run against MongoDB. Assumes there is a MongoDB instance listening on the default
 * port on localhost, and that we can do whatever we want to a database called "unittest".
 */
@RunWith(value = MongoDBTestCaseRunner.class) public abstract class MongoDBTestBase {
  private static final Random rand = new Random();

  private static final String dbHostKey = "MONGOJACK_TESTDB_HOST";

  private static final Map<String, String> environment = System.getenv();

  private boolean useStreamParser = true;

  private boolean useStreamSerialiser = false;

  protected Mongo mongo;

  protected DB db;

  private Set<String> collections;

  @Before public void connectToDb() throws Exception {
    String testDbHost = "localhost";
    if (environment.containsKey(dbHostKey)) {
      testDbHost = environment.get(dbHostKey);
    }
    mongo = new Mongo(testDbHost);
    db = mongo.getDB("unittest");
    collections = new HashSet<String>();
  }

  @After public void disconnectFromDb() throws Exception {
    for (String collection : collections) {
      db.getCollection(collection).drop();
    }
    mongo.close();
  }

  /**
     * Get a collection with the given name, and store it, so that it will be dropped in clean up
     *
     * @param name The name of the collection
     * @return The collection
     */
  protected DBCollection getCollection(String name) {
    collections.add(name);
    return db.getCollection(name);
  }

  /**
     * Get a collection with a random name. Should grant some degree of isolation from tests running in parallel.
     *
     * @return The collection
     */
  protected DBCollection getCollection() {
    StringBuilder name = new StringBuilder();
    while (name.length() < 8) {
      char letter = (char) rand.nextInt(26);
      if (rand.nextBoolean()) {
        letter += 'a';
      } else {
        letter += 'A';
      }
      name.append(letter);
    }
    return getCollection(name.toString());
  }

  protected <T extends java.lang.Object, K extends java.lang.Object> JacksonDBCollection<T, K> configure(JacksonDBCollection<T, K> collection) {
    if (useStreamParser) {
      collection.enable(JacksonDBCollection.Feature.USE_STREAM_DESERIALIZATION);
    } else {
      collection.disable(JacksonDBCollection.Feature.USE_STREAM_DESERIALIZATION);
    }
    if (useStreamSerialiser) {
      collection.enable(JacksonDBCollection.Feature.USE_STREAM_SERIALIZATION);
    } else {
      collection.disable(JacksonDBCollection.Feature.USE_STREAM_SERIALIZATION);
    }
    return collection;
  }

  protected <T extends java.lang.Object, K extends java.lang.Object> JacksonDBCollection<T, K> getCollection(Class<T> type, Class<K> keyType) {
    return configure(JacksonDBCollection.wrap(getCollection(), type, keyType));
  }

  protected <T extends java.lang.Object, K extends java.lang.Object> JacksonDBCollection<T, K> getCollection(Class<T> type, Class<K> keyType, Class<?> view) {
    return configure(JacksonDBCollection.wrap(getCollection(), type, keyType, view));
  }

  protected <T extends java.lang.Object, K extends java.lang.Object> JacksonDBCollection<T, K> getCollection(Class<T> type, Class<K> keyType, String collectionName) {
    return configure(JacksonDBCollection.wrap(getCollection(collectionName), type, keyType));
  }

  protected <T extends java.lang.Object, K extends java.lang.Object> JacksonDBCollection<T, K> getCollection(Class<T> type, Class<K> keyType, ObjectMapper mapper) {
    return configure(JacksonDBCollection.wrap(getCollection(), type, keyType, mapper));
  }

  public void setUseStreamParser(boolean useStreamParser) {
    this.useStreamParser = useStreamParser;
  }

  public void setUseStreamSerialiser(boolean useStreamSerialiser) {
    this.useStreamSerialiser = useStreamSerialiser;
  }
}