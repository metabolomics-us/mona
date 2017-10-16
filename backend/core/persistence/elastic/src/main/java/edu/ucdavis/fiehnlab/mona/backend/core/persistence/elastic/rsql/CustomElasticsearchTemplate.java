package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.rsql;

import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.facet.FacetBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.facet.FacetRequest;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by sajjan on 7/25/17.
 * Extremely hacky solution to add a node preference to fix duplicate results issue when paging
 * TODO Must be updated upon any change of spring-data-elasticsearch module
 */
public class CustomElasticsearchTemplate extends ElasticsearchTemplate {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchTemplate.class);

    private Client client;


    public CustomElasticsearchTemplate(Client client) {
        super(client);
        this.client = client;
    }

    public CustomElasticsearchTemplate(Client client, EntityMapper entityMapper) {
        super(client, entityMapper);
        this.client = client;
    }

    public CustomElasticsearchTemplate(Client client, ResultsMapper resultsMapper) {
        super(client, resultsMapper);
        this.client = client;
    }

    public CustomElasticsearchTemplate(Client client, ElasticsearchConverter elasticsearchConverter) {
        super(client, elasticsearchConverter);
        this.client = client;
    }

    public CustomElasticsearchTemplate(Client client, ElasticsearchConverter elasticsearchConverter, ResultsMapper resultsMapper) {
        super(client, elasticsearchConverter, resultsMapper);
        this.client = client;
    }

    private SearchResponse doSearch(SearchRequestBuilder searchRequest, SearchQuery searchQuery) {
        if (searchQuery.getFilter() != null) {
            searchRequest.setPostFilter(searchQuery.getFilter());
        }

        if (CollectionUtils.isNotEmpty(searchQuery.getElasticsearchSorts())) {
            for (SortBuilder sort : searchQuery.getElasticsearchSorts()) {
                searchRequest.addSort(sort);
            }
        }

        if (CollectionUtils.isNotEmpty(searchQuery.getFacets())) {
            for (FacetRequest facetRequest : searchQuery.getFacets()) {
                FacetBuilder facet = facetRequest.getFacet();
                if (facetRequest.applyQueryFilter() && searchQuery.getFilter() != null) {
                    facet.facetFilter(searchQuery.getFilter());
                }
                searchRequest.addFacet(facet);
            }
        }

        if (searchQuery.getHighlightFields() != null) {
            for (HighlightBuilder.Field highlightField : searchQuery.getHighlightFields()) {
                searchRequest.addHighlightedField(highlightField);
            }
        }

        if (CollectionUtils.isNotEmpty(searchQuery.getAggregations())) {
            for (AbstractAggregationBuilder aggregationBuilder : searchQuery.getAggregations()) {
                searchRequest.addAggregation(aggregationBuilder);
            }
        }
        return searchRequest.setQuery(searchQuery.getQuery()).execute().actionGet();
    }

    private ElasticsearchPersistentEntity getPersistentEntityFor(Class clazz) {
        Assert.isTrue(clazz.isAnnotationPresent(Document.class), "Unable to identify index name. " + clazz.getSimpleName()
                + " is not a Document. Make sure the document class is annotated with @Document(indexName=\"foo\")");
        return getElasticsearchConverter().getMappingContext().getPersistentEntity(clazz);
    }

    private String[] retrieveIndexNameFromPersistentEntity(Class clazz) {
        if (clazz != null) {
            return new String[]{getPersistentEntityFor(clazz).getIndexName()};
        }
        return null;
    }

    private String[] retrieveTypeFromPersistentEntity(Class clazz) {
        if (clazz != null) {
            return new String[]{getPersistentEntityFor(clazz).getIndexType()};
        }
        return null;
    }

    private void setPersistentEntityIndexAndType(Query query, Class clazz) {
        if (query.getIndices().isEmpty()) {
            query.addIndices(retrieveIndexNameFromPersistentEntity(clazz));
        }
        if (query.getTypes().isEmpty()) {
            query.addTypes(retrieveTypeFromPersistentEntity(clazz));
        }
    }

    private <T> SearchRequestBuilder prepareSearch(Query query, Class<T> clazz) {
        setPersistentEntityIndexAndType(query, clazz);
        return prepareSearch(query);
    }

    private static String[] toArray(List<String> values) {
        String[] valuesAsArray = new String[values.size()];
        return values.toArray(valuesAsArray);
    }


    private SearchRequestBuilder prepareSearch(Query query) {
        Assert.notNull(query.getIndices(), "No index defined for Query");
        Assert.notNull(query.getTypes(), "No type defined for Query");

        // Add a custom preference key to force queries to execute on the same shards, preventing
        // duplication issues during pagination
        // This is the ONLY reason for this terrible hack of a file
        int startRecord = 0;
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(toArray(query.getIndices()))
                .setSearchType(query.getSearchType()).setTypes(toArray(query.getTypes()))
                .setPreference("paging");

        if (query.getPageable() != null) {
            startRecord = query.getPageable().getPageNumber() * query.getPageable().getPageSize();
            searchRequestBuilder.setSize(query.getPageable().getPageSize());
        }
        searchRequestBuilder.setFrom(startRecord);

        if (!query.getFields().isEmpty()) {
            searchRequestBuilder.addFields(toArray(query.getFields()));
        }

        if (query.getSort() != null) {
            for (Sort.Order order : query.getSort()) {
                searchRequestBuilder.addSort(order.getProperty(), order.getDirection() == Sort.Direction.DESC ? SortOrder.DESC
                        : SortOrder.ASC);
            }
        }

        if (query.getMinScore() > 0) {
            searchRequestBuilder.setMinScore(query.getMinScore());
        }
        return searchRequestBuilder;
    }

    @Override
    public <T> FacetedPage<T> queryForPage(SearchQuery query, Class<T> clazz, SearchResultMapper mapper) {
        SearchResponse response = doSearch(prepareSearch(query, clazz), query);
        return mapper.mapResults(response, clazz, query.getPageable());
    }
}
