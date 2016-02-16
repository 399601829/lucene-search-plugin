package org.protege.editor.search.lucene;

import org.protege.editor.owl.model.search.CompoundKeyword;
import org.protege.editor.owl.model.search.SearchInputHandlerBase;
import org.protege.editor.owl.model.search.SearchKeyword;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 04/11/2015
 */
public class QueryBasedInputHandler extends SearchInputHandlerBase<SearchQueries> {

    private List<SearchQueryBuilder> builders = new ArrayList<>();

    public QueryBasedInputHandler(LuceneSearcher searcher) {
        builders.add(new QueryForEntityIriBuilder(searcher));
        builders.add(new QueryForDisplayNameBuilder(searcher));
        builders.add(new QueryForAnnotationValueBuilder(searcher));
        builders.add(new QueryForFilteredAnnotationBuilder(searcher));
    }

    @Override
    public SearchQueries getQueryObject() {
        SearchQueries batchQuery = new SearchQueries();
        for (SearchQueryBuilder builder : builders) {
            SearchQuery searchQuery = builder.build();
            batchQuery.add(searchQuery);
        }
        return batchQuery;
    }

    @Override
    public void handle(SearchKeyword searchKeyword) {
        for (SearchQueryBuilder builder : builders) {
            if (builder.isBuilderFor(searchKeyword)) {
                builder.add(searchKeyword);
            }
        }
    }

    @Override
    public void handle(CompoundKeyword compoundKeyword) {
        for (SearchKeyword keyword : compoundKeyword) {
            handle(keyword);
        }
    }
}
