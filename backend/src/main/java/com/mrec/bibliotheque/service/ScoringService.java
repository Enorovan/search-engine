package com.mrec.bibliotheque.service;

import com.mrec.bibliotheque.model.Book;
import com.mrec.bibliotheque.model.Neighbor;
import com.mrec.bibliotheque.model.Scoring;
import com.mrec.bibliotheque.model.TokenDto;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ScoringService {
    private static final String INDEX = "scoring";
    private final ElasticsearchOperations elasticsearchOperations;
    private final ApiGutenbergService apiGutenbergService;

    @Autowired
    public ScoringService(final ElasticsearchOperations elasticsearchOperations, ApiGutenbergService apiGutenbergService) {
        super();
        this.elasticsearchOperations = elasticsearchOperations;
        this.apiGutenbergService = apiGutenbergService;
    }

    public List<Scoring> createScoreIndex(HashMap<String, Double> hashMap) {
        ArrayList<Scoring> arrayList = new ArrayList<>();
        Set<String> booksIds = hashMap.keySet();
        for (String bookId : booksIds) {
            Double score = hashMap.get(bookId);

            Scoring scoring = new Scoring(bookId, score);

            arrayList.add(scoring);

            IndexQuery indexQuery = new IndexQueryBuilder()
                    .withId(scoring.getId())
                    .withObject(scoring)
                    .build();

            elasticsearchOperations
                    .index(indexQuery, IndexCoordinates.of(INDEX));
        }

        return arrayList;
    }

    public List<Scoring> getAll() {
        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();

        Query searchQuery = new NativeSearchQueryBuilder()
                .withFilter(queryBuilder)
                .build();

        SearchHits<Scoring> scoreHits =
                elasticsearchOperations
                        .search(searchQuery, Scoring.class,
                                IndexCoordinates.of(INDEX));
        List<Scoring> scoreMatches = new ArrayList<Scoring>();
        for (SearchHit<Scoring> searchHit : scoreHits) {
            scoreMatches.add(searchHit.getContent());
        }
        return scoreMatches;
    }

    public List<Neighbor> createNeighborIndex(GraphService graphService, List<Book> bookList) {
        ArrayList<Neighbor> arrayList = new ArrayList<>();
        Map<String, List<String>> hashMap = graphService.getNeighborAll(bookList);
        Set<String> booksIds = hashMap.keySet();
        for (String bookId : booksIds) {
            List<String> neighborId = hashMap.get(bookId);

            Neighbor neighbor = new Neighbor(bookId, neighborId);

            arrayList.add(neighbor);

            IndexQuery indexQuery = new IndexQueryBuilder()
                    .withId(neighbor.getId())
                    .withObject(neighbor)
                    .build();

            String INDEXNeighbor = "neighbor";
            elasticsearchOperations
                    .index(indexQuery, IndexCoordinates.of(INDEXNeighbor));
        }

        return arrayList;
    }

    public Neighbor getNeighborFromId(String id) {
        return elasticsearchOperations.get(id, Neighbor.class);
    }

    public List<String> getSuggestion(String bookId){
        List<String> suggest = getNeighborFromId(bookId).getIdNeighbor();
        List<String> result = new ArrayList<>();
        if(suggest.size()>3){
            for(int i = 0; i<3; i++){
                result.add(suggest.get(i));
            }
        }else{
            return  suggest;
        }

        return result;
    }
}
