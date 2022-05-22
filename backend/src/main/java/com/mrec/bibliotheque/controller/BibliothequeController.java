package com.mrec.bibliotheque.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrec.bibliotheque.controller.response.PositionResponse;
import com.mrec.bibliotheque.model.Book;
import com.mrec.bibliotheque.model.Neighbor;
import com.mrec.bibliotheque.model.Scoring;
import com.mrec.bibliotheque.model.TokenDto;
import com.mrec.bibliotheque.service.ApiGutenbergService;
import com.mrec.bibliotheque.service.BibliothequeService;
import com.mrec.bibliotheque.service.GraphService;
import com.mrec.bibliotheque.service.ScoringService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/bibliotheque")
public class BibliothequeController {
    private static final Logger LOG = LogManager.getLogger(BibliothequeController.class.getName());
    private final RestTemplate restTemplate;

    @Autowired
    private final BibliothequeService bibliothequeService;

    @Autowired
    private final ApiGutenbergService apiGutenbergService;

    @Autowired
    private final ScoringService scoringService;

    @Autowired
    private final GraphService graphService;


    public BibliothequeController(RestTemplateBuilder restTemplate, BibliothequeService bibliothequeService, ApiGutenbergService apiGutenbergService, ScoringService scoringService, GraphService graphService){
        this.restTemplate = restTemplate.build();
        this.bibliothequeService = bibliothequeService;
        this.apiGutenbergService = apiGutenbergService;
        this.scoringService = scoringService;
        this.graphService = graphService;
    }

    @GetMapping("/{id}/getContent")
    public String getContentBook(@PathVariable String id){
        return apiGutenbergService.getContent(id);
    }


    @GetMapping("/{id}/listToken")
    public Collection<TokenDto> getTokenByIdBook(@PathVariable String id){
        return apiGutenbergService.getTokens(id);
    }


    @GetMapping("/{id}/index/{request}")
    public ArrayList<PositionResponse> getTokenPositionIdBook(@PathVariable String id, @PathVariable String request){
        return apiGutenbergService.getTokensPosition(id, request);
    }

    @GetMapping
    public ArrayList<Book> getBook() throws Exception{
        ArrayList<Book> arrayList = new ArrayList<Book>();
        String[] languages = new String[]{"en", "fr", "de"};
        for (String lang : languages) {
            for (int i = 1; i < 2; i++) {
                String url = "https://gutendex.com/books/?languages=" + lang + "&page=" + i;
                String content = this.restTemplate.getForObject(url, String.class);

                ArrayList<Book> books = new ArrayList<Book>();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = null;
                node = mapper.readTree(content);
                arrayList.addAll(insertBooks(node.get("results")));
            }
        }
        return arrayList;
    }

    public ArrayList<Book> insertBooks(JsonNode results){
        ArrayList<Book> books = new ArrayList<Book>();
        for (int i = 0; i <results.size(); i++) {
            JsonNode element = results.get(i);
            String id = element.get("id").asText();
            String title = element.get("title").asText();
            String author = element.get("authors").get(0).get("name").asText();
            String urlImage = element.get("formats").get("image/jpeg").asText();
            JsonNode jsonLanguages = element.get("languages");
            String[] languages = new String[jsonLanguages.size()];
            if (jsonLanguages.isArray()) {
                for (int j = 0; j < languages.length; j++) {
                    languages[j] = jsonLanguages.get(j).asText();
                }
            }
            int nbClick = element.get("download_count").asInt(0);
            JsonNode jsonSubject = element.get("subjects");
            JsonNode jsonBookshelves = element.get("bookshelves");
            String[] keywords = new String[(jsonSubject.size() + jsonBookshelves.size())];
            if (jsonSubject.isArray()) {
                for (int j = 0; j < jsonSubject.size(); j++) {
                    keywords[j] = jsonSubject.get(j).asText();
                    if (j < jsonBookshelves.size()) {
                        keywords[(jsonSubject.size() + j)] = jsonBookshelves.get(j).asText();
                    }
                }
            }
            try {
                Collection<TokenDto> token = apiGutenbergService.getTokens(id);

                Book book = new Book(id, title, author, urlImage, keywords, languages, token, nbClick);
                books.add(book);
                bibliothequeService.createBookIndex(book);
            }catch (HttpClientErrorException e){

            }
        }
        return books;
    }

    public Book insertBook(Book book) {
        long startTime = System.currentTimeMillis();
        book.setToken(apiGutenbergService.getTokens(book.getId()));
        Book uploadBook = bibliothequeService.createBookIndex(book);
        long endTime = System.currentTimeMillis();
        System.out.println("Total elapsed time in execution of method callMethod() is :"+ ((endTime-startTime)));
        return uploadBook;
    }

    @PostMapping("/click")
    public Boolean addClick(@RequestBody Book book) {
        long startTime = System.currentTimeMillis();
        var optionalBook = bibliothequeService.getBookFromDatabase(book);
        optionalBook.ifPresent(value -> value.setNbClick(value.getNbClick() + 1));
        bibliothequeService.createBookIndex(optionalBook.get());
        long endTime = System.currentTimeMillis();
        LOG.log(Level.INFO, "book " + optionalBook.get().getId() + " click increment " + optionalBook.get().getNbClick());
        System.out.println("Total elapsed time in execution of method callMethod() is :"+ ((endTime-startTime)));
        return true;
    }

    @GetMapping("/searchWord")
    public List<Book> getBookByWord(@RequestParam(value = "q", required = false) String words){
        LOG.log(Level.INFO, "Get books with words " + words);
        long startTime = System.currentTimeMillis();
        List<Book> listBooks = bibliothequeService.searchWord(words) ;
        long endTime = System.currentTimeMillis();
        System.out.println("Total elapsed time in execution of method callMethod() is :"+ ((endTime-startTime)));
        return listBooks;
    }

    @GetMapping("/searchLang")
    public List<Book> getBookByLang(@RequestParam(value = "q", required = false) String lang){
        LOG.log(Level.INFO, "Get books with lang " + lang);
        long startTime = System.currentTimeMillis();
        List<Book> listBooks = bibliothequeService.searchLang(lang) ;
        long endTime = System.currentTimeMillis();
        System.out.println("Total elapsed time in execution of method callMethod() is :"+ ((endTime-startTime)));
        return listBooks;
    }
    
    @PostMapping("/advancedSearch")
    public List<Book> getBookByRegexp(@RequestBody String regexp){
        LOG.log(Level.INFO, "Get books with regexp " + regexp);
        return bibliothequeService.advancedSearch(regexp) ;
    }


    @GetMapping("/getAll")
    public List<Book> getAll(){
        LOG.log(Level.INFO, "Get all books");
        List<Book> books = bibliothequeService.getAll();
        return books;
    }


    @GetMapping("/getGraph")
    public List<Scoring> getGraph(){
        GraphService graphService = new GraphService();
        List<Scoring> scoring = null;
        try {
            graphService.createGraphByBookList(getAll());
            HashMap<String, Double> result = graphService.mapOfScores(getAll());
            scoringService.createNeighborIndex(graphService, getAll());
            scoring = scoringService.createScoreIndex(result);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return scoring;
    }

    @GetMapping("/getAllScoring")
    public List<Scoring> getAllScoring(){
        LOG.log(Level.INFO, "Get all books");
        return scoringService.getAll();
    }

    @GetMapping("/fromId/{id}")
    public Book getFromId(@PathVariable String id) {
        return bibliothequeService.getBookFromId(id);
    }

    @GetMapping("/suggestion/{id}")
    public List<Book> getSuggestion(@PathVariable String id){
        try {
            List<String> suggestIds = scoringService.getSuggestion(id);
            return bibliothequeService.getSuggestion(suggestIds);
        }
        catch (Exception e){
            System.out.println(e);
        }
        return new ArrayList<Book>();
    }

    @GetMapping("/neighborFromId/{id}")
    public Neighbor getNeighborFromId(@PathVariable String id) {
        return scoringService.getNeighborFromId(id);
    }

}
