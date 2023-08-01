package io.searchbox.client;

import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.client.http.JestHttpClient;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.Test;
<<<<<<< HEAD
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
=======
>>>>>>> b1e434e4

import java.util.concurrent.TimeUnit;

/**
 * @author cihat keser
 */
@ElasticsearchIntegrationTest.ClusterScope(scope = ElasticsearchIntegrationTest.Scope.TEST, numNodes = 0)
public class JestClientFactoryIntegrationTest extends ElasticsearchIntegrationTest {

<<<<<<< HEAD
    final static Logger log = LoggerFactory.getLogger(JestClientFactoryIntegrationTest.class);

    @ElasticsearchNode
    Node first;

    @ElasticsearchNode(name = "2nd")
    Node node;
    @ElasticsearchAdminClient
    AdminClient adminClient;
=======
>>>>>>> b1e434e4
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

        assertEquals(
                "All 4 nodes should be discovered and be in the client's server list",
                4,
                jestClient.getServers().size()
        );

<<<<<<< HEAD
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
        cluster().ensureAtMostNumNodes(3);

        int numServers = 0;
        int retries = 0;
        while(numServers != 3 && retries < 30) {
            numServers = jestClient.getServers().size();
            retries++;
            Thread.sleep(1000);
        }
>>>>>>> b1e434e4

        assertEquals(
                "Only 3 nodes should be in Jest's list",
                3,
                jestClient.getServers().size()
        );
    }
}
