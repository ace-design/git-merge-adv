package io.searchbox.indices.aliases;

<<<<<<< HEAD
import io.searchbox.client.config.ElasticsearchVersion;
=======
import java.util.Arrays;
>>>>>>> f0c0e77f
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class GetAliasesTest {

    @Test
    public void testBasicUriGeneration() {
        GetAliases getAliases = new GetAliases.Builder().addIndex("twitter").build();

        assertEquals("GET", getAliases.getRestMethodName());
<<<<<<< HEAD
        assertEquals("twitter/_aliases", getAliases.getURI(ElasticsearchVersion.UNKNOWN));
=======
        assertEquals("twitter/_alias", getAliases.getURI());
    }

    @Test
    public void testBasicUriGenerationWithAliases() {
        GetAliases getAliases = new GetAliases.Builder().addIndex("twitter").addAlias("alias").build();

        assertEquals("GET", getAliases.getRestMethodName());
        assertEquals("twitter/_alias/alias", getAliases.getURI());
    }

    @Test
    public void testBasicUriGenerationWithMultipleAliases() {
        GetAliases getAliases = new GetAliases.Builder().addIndex("twitter").addAliases(Arrays.asList(new String[]{"alias1", "alias2"})).build();

        assertEquals("GET", getAliases.getRestMethodName());
        assertEquals("twitter/_alias/alias1,alias2", getAliases.getURI());
>>>>>>> f0c0e77f
    }

    @Test
    public void equalsReturnsTrueForSameIndex() {
        GetAliases getAliases1 = new GetAliases.Builder().addIndex("twitter").build();
        GetAliases getAliases1Duplicate = new GetAliases.Builder().addIndex("twitter").build();

        assertEquals(getAliases1, getAliases1Duplicate);
    }

    @Test
    public void equalsReturnsFalseForDifferentIndex() {
        GetAliases getAliases1 = new GetAliases.Builder().addIndex("twitter").build();
        GetAliases getAliases2 = new GetAliases.Builder().addIndex("myspace").build();

        assertNotEquals(getAliases1, getAliases2);
    }

}