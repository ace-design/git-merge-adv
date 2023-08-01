package io.searchbox.indices.aliases;

import com.google.common.base.Joiner;
import io.searchbox.action.AbstractMultiIndexActionBuilder;
import io.searchbox.action.GenericResultAbstractAction;
<<<<<<< HEAD
import io.searchbox.client.config.ElasticsearchVersion;
=======
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
>>>>>>> f0c0e77f

/**
 * @author cihat keser
 */
public class GetAliases extends GenericResultAbstractAction {
    protected String aliasName;

    protected GetAliases(Builder builder) {
        super(builder);
<<<<<<< HEAD
=======
        aliasName = builder.getJoinedAliases();
        setURI(buildURI());
>>>>>>> f0c0e77f
    }

    @Override
    public String getRestMethodName() {
        return "GET";
    }

    @Override
<<<<<<< HEAD
    protected String buildURI(ElasticsearchVersion elasticsearchVersion) {
        return super.buildURI(elasticsearchVersion) + "/_aliases";
=======
    protected String buildURI() {
        if (aliasName == null){
            return super.buildURI() + "/_alias";
        } else {
            return super.buildURI() + "/_alias/" + aliasName;
        }
>>>>>>> f0c0e77f
    }

    public static class Builder extends AbstractMultiIndexActionBuilder<GetAliases, Builder> {

        protected Set<String> aliasNames = new LinkedHashSet<String>();

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


        @Override
        public GetAliases build() {
            return new GetAliases(this);
        }
    }
}
