package io.searchbox.indices.aliases;
import io.searchbox.action.AbstractMultiIndexActionBuilder;
import com.google.common.base.Joiner;
import io.searchbox.action.GenericResultAbstractAction;
import io.searchbox.client.config.ElasticsearchVersion;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class GetAliases extends GenericResultAbstractAction{

    protected String aliasName;,

    protected GetAliases(Builder builder) {
        super(builder);
<<<<<<< left_content.java
        aliasName = builder.getJoinedAliases();
        setURI(buildURI());
=======
>>>>>>> right_content.java
    }


    @Override
    public String getRestMethodName() {
        return "GET";
    }

    @Override
    protected String buildURI(ElasticsearchVersion elasticsearchVersion) {
        return super.buildURI(elasticsearchVersion) + "/_aliases";
    }

    @Override
    protected String buildURI() {
        if (aliasName == null){
            return super.buildURI() + "/_alias";
        } else {
            return super.buildURI() + "/_alias/" + aliasName;
        }
    }

    public static class Builder extends AbstractMultiIndexActionBuilder<GetAliases, Builder>{


        @Override
        public GetAliases build() {
            return new GetAliases(this);
        }
        protected Set<String> aliasNames = new LinkedHashSet<String>();,

        public String getJoinedAliases() {
            if (aliasNames.size() > 0) {
                return Joiner.on(',').join(aliasNames);
            } else {
                return null;
            }
        }

        public Builder addAlias(String aliasName) {
            this.aliasNames.add(aliasName);
            return this;
        }

        public Builder addAliases(Collection<? extends String> aliasNames) {
            this.aliasNames.addAll(aliasNames);
            return this;
        }

    }
}