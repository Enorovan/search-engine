package com.mrec.bibliotheque.service;

import com.mrec.bibliotheque.model.Book;
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
public class BibliothequeService {
    private static final String INDEX = "books";
    private final ElasticsearchOperations elasticsearchOperations;
    private final ApiGutenbergService apiGutenbergService;

    @Autowired
    public BibliothequeService(final ElasticsearchOperations elasticsearchOperations, ApiGutenbergService apiGutenbergService) {
        super();
        this.elasticsearchOperations = elasticsearchOperations;
        this.apiGutenbergService = apiGutenbergService;
    }

    public Book createBookIndex(Book book) {
        if(book.getId() == null || book.getId().isEmpty() || book.getId().isBlank())
            book.setId(UUID.randomUUID().toString());

        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(book.getId())
                .withObject(book)
                .build();

        elasticsearchOperations
                .index(indexQuery, IndexCoordinates.of(INDEX));

        return book;
    }

    public int Distance(String id1, String id2){
        Collection<TokenDto> token1 = apiGutenbergService.getTokens(id1);
        Collection<TokenDto> token2 = apiGutenbergService.getTokens(id2);
        int distance = 0;
        for (TokenDto tokenDto1 : token1){
            tokenDto1.getToken();
            tokenDto1.getOccurrences();
            for (TokenDto tokenDto2 : token2){
                if (tokenDto1.getToken() == tokenDto2.getToken()){
                    int sommeOccurrence = tokenDto1.getOccurrences() + tokenDto2.getOccurrences();
                }
            }
        }
        return distance;
    }


    public Optional<Book> getBookFromDatabase(Book book) {
        return Optional.ofNullable(elasticsearchOperations.get(book.getId(),Book.class));
    }

    public Book getBookFromId(String id) {
        return elasticsearchOperations.get(id, Book.class);
    }

    public List<Book> getSuggestion(List<String> ids){
        List<Book> result = new ArrayList<>();
        for (String id: ids){
            Optional<Book> bookOpt = Optional.ofNullable(getBookFromId(id));
            if(bookOpt.isPresent()){
                result.add(bookOpt.get());
            }
        }
        return result;
    }

    public List<Book> getAll() {
        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();

        Query searchQuery = new NativeSearchQueryBuilder()
                .withFilter(queryBuilder)
                .withSorts((SortBuilders.fieldSort("token.occurrences").order( SortOrder.DESC )))
                .build();

        SearchHits<Book> bookHits =
                elasticsearchOperations
                        .search(searchQuery, Book.class,
                                IndexCoordinates.of(INDEX));
        List<Book> bookMatches = new ArrayList<Book>();
        for (SearchHit<Book> searchHit : bookHits) {
            bookMatches.add(searchHit.getContent());
        }
        return bookMatches;
    }

    public List<Book> searchWord(String query) {
        QueryBuilder queryBuilder =
                QueryBuilders
                        .matchQuery("token.token", query)
                        .operator(Operator.AND);
        Query searchQuery = new NativeSearchQueryBuilder()
                .withFilter(queryBuilder)
                .withSorts((SortBuilders.fieldSort("token.occurrences").order( SortOrder.DESC )))
                .build();
        SearchHits<Book> bookHits =
                elasticsearchOperations
                        .search(searchQuery, Book.class,
                                IndexCoordinates.of(INDEX));
        List<Book> bookMatches = new ArrayList<Book>();
        for (SearchHit<Book> searchHit : bookHits) {
            bookMatches.add(searchHit.getContent());
        }
        return bookMatches;
    }

    public List<Book> searchLang(String query) {
        QueryBuilder queryBuilder =
                QueryBuilders
                        .matchQuery("languages", query)
                        .operator(Operator.AND);
        Query searchQuery = new NativeSearchQueryBuilder()
                .withFilter(queryBuilder)
                .withSorts((SortBuilders.fieldSort("nbClick").order( SortOrder.DESC )))
                .build();
        SearchHits<Book> bookHits =
                elasticsearchOperations
                        .search(searchQuery, Book.class,
                                IndexCoordinates.of(INDEX));
        List<Book> bookMatches = new ArrayList<Book>();
        for (SearchHit<Book> searchHit : bookHits) {
            bookMatches.add(searchHit.getContent());
        }
        return bookMatches;
    }

    public List<Book> advancedSearch(final String regexp) {
        Query searchQuery = new StringQuery(
                "{\"regexp\":{\"token.token\":\""+ regexp + "\"}}\"");

        SearchHits<Book> bookHits =
                elasticsearchOperations
                        .search(searchQuery, Book.class,
                                IndexCoordinates.of(INDEX));
        List<Book> bookMatches = new ArrayList<Book>();
        for (SearchHit<Book> searchHit : bookHits) {
            bookMatches.add(searchHit.getContent());
        }
        return bookMatches;
    }

    public static double distanceJ(Book b1, Book b2){
        List<String> tokenProcess = new ArrayList<>();
        List<String> intersection = new ArrayList<>();
        for(TokenDto token1 : b1.getToken()){
            String current = token1.getToken();
            if(!tokenProcess.contains(current)) tokenProcess.add(current);
            for(TokenDto token2 : b2.getToken()){
                if(current.equals(token2.getToken()) && !intersection.contains(current)) {
                    intersection.add(current);
                }
            }
        }

        for(TokenDto token2 : b2.getToken()){
            String current = token2.getToken();
            if(!tokenProcess.contains(current)) tokenProcess.add(current);
        }
        return 1.0 - ((double)intersection.size()/(double)tokenProcess.size());
    }
}
