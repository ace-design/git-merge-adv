package io.searchbox.client;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.client.http.JestHttpClient;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.elasticsearch.test.ElasticsearchIntegrationTest;



/**
 * @author cihat keser
 */
<<<<<<< left_content.java
@RunWith(ElasticsearchRunner.class)
public class JestClientFactoryIntegrationTest {

    final static Logger log = LoggerFactory.getLogger(JestClientFactoryIntegrationTest.class);

    @ElasticsearchNode
    Node first;
=======
@ElasticsearchIntegrationTest.ClusterScope(scope = ElasticsearchIntegrationTest.Scope.TEST, numNodes = 0)
public class JestClientFactoryIntegrationTest extends ElasticsearchIntegrationTest {
>>>>>>> right_content.java

    JestClientFactory factory = new JestClientFactory();

    @Test
    public void testDiscovery() throws InterruptedException {
        // wait for 3 active nodes
        cluster().ensureAtLeastNumNodes(4);

        factory.setHttpClientConfig(new HttpClientConfig
                .Builder("http://localhost:9200")
                .discoveryEnabled(true)
                .discoveryFrequency(500l, TimeUnit.MILLISECONDS)
                .build());
        JestHttpClient jestClient = (JestHttpClient) factory.getObject();
        assertNotNull(jestClient);

        // wait for NodeChecker to do the discovery
        Thread.sleep(3000);

<<<<<<< left_content.java
        assertEquals("All 2 nodes should be discovered and be in the client's server list", 2, jestClient.getServers().size());

        // now close first client
        first.close();

        // wait for second node to stop
        while(!first.isClosed()) {
            Thread.sleep(500);
            log.info("Waiting for second node to stop...");
        }

        // wait for NodeChecker to do the discovery
        Thread.sleep(1500);

        assertEquals("Only 1 node should be in Jest's list", 1, jestClient.getServers().size());

=======
        assertEquals(
                "All 4 nodes should be discovered and be in the client's server list",
                4,
                jestClient.getServers().size()
        );

        cluster().ensureAtMostNumNodes(3);

        int numServers = 0;
        int retries = 0;
        while(numServers != 3 && retries < 30) {
            numServers = jestClient.getServers().size();
            retries++;
            Thread.sleep(1000);
        }

        assertEquals(
                "Only 3 nodes should be in Jest's list",
                3,
                jestClient.getServers().size()
        );
>>>>>>> right_content.java
    }
}

