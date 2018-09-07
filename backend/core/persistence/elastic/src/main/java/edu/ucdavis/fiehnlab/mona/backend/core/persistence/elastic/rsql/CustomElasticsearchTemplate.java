package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.rsql;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.EntityMapper;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.facet.FacetRequest;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.util.Assert;

import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Created by sajjan on 7/25/17.
 * Extremely hacky solution to add a node preference to fix duplicate results issue when paging
 * TODO Must be updated upon any change of spring-data-elasticsearch module
 */
public class CustomElasticsearchTemplate extends ElasticsearchTemplate {

    private static final String FIELD_SCORE = "_score";

    public CustomElasticsearchTemplate(Client client, EntityMapper entityMapper) {
        super(client, entityMapper);
    }

    private SearchResponse doSearch(SearchRequestBuilder searchRequest, SearchQuery searchQuery) {
        if (searchQuery.getFilter() != null) {
            searchRequest.setPostFilter(searchQuery.getFilter());
        }

        if (!isEmpty(searchQuery.getElasticsearchSorts())) {
            for (SortBuilder sort : searchQuery.getElasticsearchSorts()) {
                searchRequest.addSort(sort);
            }
        }

        if (!searchQuery.getScriptFields().isEmpty()) {
            //_source should be return all the time
            //searchRequest.addStoredField("_source");
            for (ScriptField scriptedField : searchQuery.getScriptFields()) {
                searchRequest.addScriptField(scriptedField.fieldName(), scriptedField.script());
            }
        }

        if (searchQuery.getHighlightFields() != null) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            for (HighlightBuilder.Field highlightField : searchQuery.getHighlightFields()) {
                highlightBuilder.field(highlightField);
            }
            searchRequest.highlighter(highlightBuilder);
        }

        if (!isEmpty(searchQuery.getIndicesBoost())) {
            for (IndexBoost indexBoost : searchQuery.getIndicesBoost()) {
                searchRequest.addIndexBoost(indexBoost.getIndexName(), indexBoost.getBoost());
            }
        }

        if (!isEmpty(searchQuery.getAggregations())) {
            for (AbstractAggregationBuilder aggregationBuilder : searchQuery.getAggregations()) {
                searchRequest.addAggregation(aggregationBuilder);
            }
        }

        if (!isEmpty(searchQuery.getFacets())) {
            for (FacetRequest aggregatedFacet : searchQuery.getFacets()) {
                searchRequest.addAggregation(aggregatedFacet.getFacet());
            }
        }
        return searchRequest.setQuery(searchQuery.getQuery()).execute().actionGet();
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

        int startRecord = 0;
        SearchRequestBuilder searchRequestBuilder = getClient().prepareSearch(toArray(query.getIndices()))
                .setSearchType(query.getSearchType())
                .setTypes(toArray(query.getTypes()))
                .setVersion(true);

        if (query.getSourceFilter() != null) {
            SourceFilter sourceFilter = query.getSourceFilter();
            searchRequestBuilder.setFetchSource(sourceFilter.getIncludes(), sourceFilter.getExcludes());
        }

        if (query.getPageable().isPaged()) {
            startRecord = query.getPageable().getPageNumber() * query.getPageable().getPageSize();
            searchRequestBuilder.setSize(query.getPageable().getPageSize());
        }
        searchRequestBuilder.setFrom(startRecord);

        if (!query.getFields().isEmpty()) {
            searchRequestBuilder.setFetchSource(toArray(query.getFields()),null);
        }

        if (query.getSort() != null) {
            for (Sort.Order order : query.getSort()) {
                SortOrder sortOrder = order.getDirection().isDescending() ? SortOrder.DESC : SortOrder.ASC;

                if (FIELD_SCORE.equals(order.getProperty())) {
                    ScoreSortBuilder sort = SortBuilders //
                            .scoreSort() //
                            .order(sortOrder);

                    searchRequestBuilder.addSort(sort);
                } else {
                    FieldSortBuilder sort = SortBuilders //
                            .fieldSort(order.getProperty()) //
                            .order(sortOrder);

                    if (order.getNullHandling() == Sort.NullHandling.NULLS_FIRST) {
                        sort.missing("_first");
                    } else if (order.getNullHandling() == Sort.NullHandling.NULLS_LAST) {
                        sort.missing("_last");
                    }

                    searchRequestBuilder.addSort(sort);
                }
            }
        }

        if (query.getMinScore() > 0) {
            searchRequestBuilder.setMinScore(query.getMinScore());
        }
        return searchRequestBuilder;
    }

    @Override
    public <T> AggregatedPage<T> queryForPage(SearchQuery query, Class<T> clazz, SearchResultMapper mapper) {
        SearchResponse response = doSearch(prepareSearch(query, clazz), query);
        return mapper.mapResults(response, clazz, query.getPageable());
    }
}
