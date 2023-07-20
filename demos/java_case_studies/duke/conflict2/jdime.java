package no.priv.garshol.duke.databases;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import no.priv.garshol.duke.Comparator;
import org.apache.lucene.analysis.TokenStream;
import no.priv.garshol.duke.Configuration;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import no.priv.garshol.duke.Database;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import no.priv.garshol.duke.DukeConfigException;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import no.priv.garshol.duke.DukeException;
import org.apache.lucene.document.Document;
import no.priv.garshol.duke.Property;
import org.apache.lucene.document.Field;
import no.priv.garshol.duke.Record;
import org.apache.lucene.index.IndexReader;
import no.priv.garshol.duke.comparators.GeopositionComparator;
import org.apache.lucene.index.IndexWriter;
import no.priv.garshol.duke.utils.Utils;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

/**
 * Represents the Lucene index, and implements record linkage services
 * on top of it.
 */
public class LuceneDatabase implements Database {
  private Configuration config;

  private EstimateResultTracker maintracker;

  private IndexWriter iwriter;

  private Directory directory;

  private IndexReader reader;

  private IndexSearcher searcher;

  private Analyzer analyzer;

  private final static int SEARCH_EXPANSION_FACTOR = 1;

  private int max_search_hits;

  private float min_relevance;

  private boolean overwrite;

  private String path;

  private boolean fuzzy_search;

  public BoostMode boost_mode;

  private GeoProperty geoprop;

  public LuceneDatabase() {
    this.analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
    this.maintracker = new EstimateResultTracker();
    this.max_search_hits = 1000000;
    this.fuzzy_search = true;
    this.boost_mode = BoostMode.QUERY;
  }

  public void setConfiguration(Configuration config) {
    this.config = config;
  }

  public void setOverwrite(boolean overwrite) {
    this.overwrite = overwrite;
  }

  public void setMaxSearchHits(int max_search_hits) {
    this.max_search_hits = max_search_hits;
  }

  public void setMinRelevance(float min_relevance) {
    this.min_relevance = min_relevance;
  }

  /**
   * Controls whether to use fuzzy searches for properties that have
   * fuzzy comparators. True by default.
   */
  public void setFuzzySearch(boolean fuzzy_search) {
    this.fuzzy_search = fuzzy_search;
  }

  /**
   * Returns the path to the Lucene index directory. If null, it means
   * the Lucene index is kept in-memory.
   */
  public String getPath() {
    return path;
  }

  /**
   * The path to the Lucene index directory. If null or not set, it
   * means the Lucene index is kept in-memory.
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Tells the database to boost Lucene fields when searching for
   * candidate matches, depending on their probabilities. This can
   * help Lucene better pick the most interesting candidates.
   */
  public void setBoostMode(BoostMode boost_mode) {
    this.boost_mode = boost_mode;
  }

  /**
   * Returns true iff the Lucene index is held in memory rather than
   * on disk.
   */
  public boolean isInMemory() {
    return (directory instanceof RAMDirectory);
  }

  /**
   * Add the record to the index.
   */
  public void index(Record record) {
    if (directory == null) {
      init();
    }
    if (!overwrite && path != null) {
      delete(record);
    }
    Document doc = new Document();
    for (String propname : record.getProperties()) {
      Property prop = config.getPropertyByName(propname);
      if (prop == null) {
        throw new DukeConfigException("Record has property " + propname + " for which there is no configuration");
      }
      if (prop.getComparator() instanceof GeopositionComparator && geoprop != null) {
        String v = record.getValue(propname);
        if (v == null || v.equals("")) {
          continue;
        }
        for (IndexableField f : geoprop.createIndexableFields(v)) {
          doc.add(f);
        }
        doc.add(new Field(propname, v, Field.Store.YES, Field.Index.NOT_ANALYZED));
      } else {
        Field.Index ix;
        if (prop.isIdProperty()) {
          ix = Field.Index.NOT_ANALYZED;
        } else {
          ix = Field.Index.ANALYZED;
        }
        Float boost = getBoostFactor(prop.getHighProbability(), BoostMode.INDEX);
        for (String v : record.getValues(propname)) {
          if (v.equals("")) {
            continue;
          }
          Field field = new Field(propname, v, Field.Store.YES, ix);
          if (boost != null) {
            field.setBoost(boost);
          }
          doc.add(field);
        }
      }
    }
    try {
      iwriter.addDocument(doc);
    } catch (IOException e) {
      throw new DukeException(e);
    }
  }

  private void delete(Record record) {
    Property idprop = config.getIdentityProperties().iterator().next();
    Query q = parseTokens(idprop.getName(), record.getValue(idprop.getName()));
    try {
      iwriter.deleteDocuments(q);
    } catch (IOException e) {
      throw new DukeException(e);
    }
  }

  /**
   * Flushes all changes to disk.
   */
  public void commit() {
    if (directory == null) {
      return;
    }
    try {
      if (reader != null) {
        reader.close();
      }
      iwriter.commit();
      openSearchers();
    } catch (IOException e) {
      throw new DukeException(e);
    }
  }

  /**
   * Look up record by identity.
   */
  public Record findRecordById(String id) {
    if (directory == null) {
      init();
    }
    Property idprop = config.getIdentityProperties().iterator().next();
    for (Record r : lookup(idprop, id)) {
      if (r.getValue(idprop.getName()).equals(id)) {
        return r;
      }
    }
    return null;
  }

  /**
   * Look up potentially matching records.
   */
  public Collection<Record> findCandidateMatches(Record record) {
    if (geoprop != null) {
      String value = record.getValue(geoprop.getName());
      if (value != null) {
        Filter filter = geoprop.geoSearch(value);
        return maintracker.doQuery(new MatchAllDocsQuery(), filter);
      }
    }
    BooleanQuery query = new BooleanQuery();
    for (Property prop : config.getLookupProperties()) {
      Collection<String> values = record.getValues(prop.getName());
      if (values == null) {
        continue;
      }
      for (String value : values) {
        parseTokens(query, prop.getName(), value, prop.getLookupBehaviour() == Property.Lookup.REQUIRED, prop.getHighProbability());
      }
    }
    return maintracker.doQuery(query);
  }

  /**
   * Stores state to disk and closes all open resources.
   */
  public void close() {
    if (directory == null) {
      return;
    }
    try {
      iwriter.close();
      directory.close();
      if (reader != null) {
        reader.close();
      }
    } catch (IOException e) {
      throw new DukeException(e);
    }
  }

  public String toString() {
    return "LuceneDatabase, max-search-hits: " + max_search_hits + ", min-relevance: " + min_relevance + ", fuzzy: " + fuzzy_search + ", boost-mode: " + boost_mode + "\n  " + directory;
  }

  private void init() {
    try {
      openIndexes(overwrite);
      openSearchers();
      initSpatial();
    } catch (IOException e) {
      throw new DukeException(e);
    }
  }

  private void openIndexes(boolean overwrite) {
    if (directory == null) {
      try {
        if (path == null) {
          directory = new RAMDirectory();
        } else {
          if (Utils.isWindowsOS()) {
            directory = FSDirectory.open(new File(path));
          } else {
            directory = NIOFSDirectory.open(new File(path));
          }
        }
        IndexWriterConfig cfg = new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer);
        cfg.setOpenMode(overwrite ? IndexWriterConfig.OpenMode.CREATE : IndexWriterConfig.OpenMode.APPEND);
        iwriter = new IndexWriter(directory, cfg);
        iwriter.commit();
      } catch (IndexNotFoundException e) {
        if (!overwrite) {
          directory = null;
          openIndexes(true);
        } else {
          throw new DukeException(e);
        }
      } catch (IOException e) {
        throw new DukeException(e);
      }
    }
  }

  public void openSearchers() throws IOException {
    reader = DirectoryReader.open(directory);
    searcher = new IndexSearcher(reader);
  }

  /** 
   * Parses the query. Using this instead of a QueryParser in order
   * to avoid thread-safety issues with Lucene's query parser.
   * 
   * @param fieldName the name of the field
   * @param value the value of the field
   * @return the parsed query
   */
  private Query parseTokens(String fieldName, String value) {
    BooleanQuery searchQuery = new BooleanQuery();
    if (value != null) {
      Analyzer analyzer = new KeywordAnalyzer();
      try {
        TokenStream tokenStream = analyzer.tokenStream(fieldName, new StringReader(value));
        tokenStream.reset();
        CharTermAttribute attr = tokenStream.getAttribute(CharTermAttribute.class);
        while (tokenStream.incrementToken()) {
          String term = attr.toString();
          Query termQuery = new TermQuery(new Term(fieldName, term));
          searchQuery.add(termQuery, Occur.SHOULD);
        }
      } catch (IOException e) {
        throw new DukeException("Error parsing input string \'" + value + "\' " + "in field " + fieldName);
      }
    }
    return searchQuery;
  }

  /**
   * Parses Lucene query.
   * @param required Iff true, return only records matching this value.
   */
  private void parseTokens(BooleanQuery parent, String fieldName, String value, boolean required, double probability) {
    value = escapeLucene(value);
    if (value.length() == 0) {
      return;
    }
    try {
      TokenStream tokenStream = analyzer.tokenStream(fieldName, new StringReader(value));
      tokenStream.reset();
      CharTermAttribute attr = tokenStream.getAttribute(CharTermAttribute.class);
      Float boost = getBoostFactor(probability, BoostMode.QUERY);
      while (tokenStream.incrementToken()) {
        String term = attr.toString();
        Query termQuery;
        if (fuzzy_search && isFuzzy(fieldName)) {
          termQuery = new FuzzyQuery(new Term(fieldName, term));
        } else {
          termQuery = new TermQuery(new Term(fieldName, term));
        }
        if (boost != null) {
          termQuery.setBoost(boost);
        }
        parent.add(termQuery, required ? Occur.MUST : Occur.SHOULD);
      }
    } catch (IOException e) {
      throw new DukeException("Error parsing input string \'" + value + "\' " + "in field " + fieldName);
    }
  }

  private boolean isFuzzy(String fieldName) {
    Comparator c = config.getPropertyByName(fieldName).getComparator();
    return c != null && c.isTokenized();
  }

  private String escapeLucene(String query) {
    char[] tmp = new char[query.length() * 2];
    int count = 0;
    for (int ix = 0; ix < query.length(); ix++) {
      char ch = query.charAt(ix);
      if (ch == '*' || ch == '?' || ch == '!' || ch == '&' || ch == '(' || ch == ')' || ch == '-' || ch == '+' || ch == ':' || ch == '\"' || ch == '[' || ch == ']' || ch == '~' || ch == '{' || ch == '}' || ch == '^' || ch == '|') {
        tmp[count++] = '\\';
      }
      tmp[count++] = ch;
    }
    return new String(tmp, 0, count).trim();
  }

  public Collection<Record> lookup(Property property, String value) {
    Query query = parseTokens(property.getName(), value);
    return maintracker.doQuery(query);
  }

  class EstimateResultTracker {
    private int limit;

    /**
     * Ring buffer containing n last search result sizes, except for
     * searches which found nothing.
     */
    private int[] prevsizes;

    private int sizeix;

    public EstimateResultTracker() {
      this.limit = 100;
      this.prevsizes = new int[10];
    }

    public Collection<Record> doQuery(Query query) {
      return doQuery(query, null);
    }

    public Collection<Record> doQuery(Query query, Filter filter) {
      List<Record> matches;
      try {
        ScoreDoc[] hits;
        int thislimit = Math.min(limit, max_search_hits);
        while (true) {
          hits = searcher.search(query, filter, thislimit).scoreDocs;
          if (hits.length < thislimit || thislimit == max_search_hits) {
            break;
          }
          thislimit = thislimit * 5;
        }
        matches = new ArrayList(Math.min(hits.length, max_search_hits));
        for (int ix = 0; ix < hits.length && hits[ix].score >= min_relevance; ix++) {
          matches.add(new DocumentRecord(hits[ix].doc, searcher.doc(hits[ix].doc)));
        }
        if (hits.length > 0) {
          synchronized (this) {
            prevsizes[sizeix++] = matches.size();
            if (sizeix == prevsizes.length) {
              sizeix = 0;
              limit = Math.max((int) (average() * SEARCH_EXPANSION_FACTOR), limit);
            }
          }
        }
      } catch (IOException e) {
        throw new DukeException(e);
      }
      return matches;
    }

    private double average() {
      int sum = 0;
      int ix = 0;
      for ( ; ix < prevsizes.length && prevsizes[ix] != 0; ix++) {
        sum += prevsizes[ix];
      }
      return sum / (double) ix;
    }
  }

  /**
   * Checks to see if we need the spatial support, and if so creates
   * the necessary context objects.
   */
  private void initSpatial() {
    if (config.getLookupProperties().size() != 1) {
      return;
    }
    Property prop = config.getLookupProperties().iterator().next();
    if (!(prop.getComparator() instanceof GeopositionComparator)) {
      return;
    }
    geoprop = new GeoProperty(prop);
  }

  public enum BoostMode {
    QUERY,
    INDEX,
    NONE
  }

  private Float getBoostFactor(double probability, BoostMode phase) {
    Float boost = null;
    if (phase == boost_mode) {
      boost = (float) Math.sqrt(1.0 / ((1.0 - probability) * 2.0));
    }
    return boost;
  }
}