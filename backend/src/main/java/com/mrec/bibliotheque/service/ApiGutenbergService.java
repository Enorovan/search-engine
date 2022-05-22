package com.mrec.bibliotheque.service;


import com.mrec.bibliotheque.controller.response.PositionResponse;
import com.mrec.bibliotheque.model.TokenDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApiGutenbergService {
    private final RestTemplate restTemplate ;

    public ApiGutenbergService(RestTemplateBuilder restTemplate) {
        this.restTemplate = restTemplate.build();
    }


    public String getContent(String id){
        String url = "https://www.gutenberg.org/files/" + id + "/" + id + "-0.txt";
        String content = this.restTemplate.getForObject(url, String.class)
                .replaceAll("[^a-zA-Z]", " ")
                .replaceAll("  ", " ")
                .toLowerCase();

        return content;
    }

    public Collection<TokenDto> getTokens(String id) throws HttpClientErrorException {
        String url = "https://www.gutenberg.org/files/" + id + "/" + id + "-0.txt";
        System.out.println(url);
        String content = this.restTemplate.getForObject(url, String.class);

        List<String> arrayToken = Arrays.stream(content
                        .replaceAll("[^a-zA-Z]", " ")
                        .toLowerCase()
                        .split(" "))
                .filter(word->word!="the" && word!="a" && word!="an" && word!="" && word.length()>2 ).collect(Collectors.toList());
        int nbToken = arrayToken.size();

        Map<String, TokenDto> tokenHashMap = new HashMap<String, TokenDto>();
        for (int i=0; i<arrayToken.size(); i++){
            if (tokenHashMap.containsKey(arrayToken.get(i))){
                TokenDto tokenDto = tokenHashMap.get(arrayToken.get(i));
                tokenDto.setOccurrences(tokenDto.getOccurrences() + 1);
                tokenDto.setFrequencies((float)tokenDto.getOccurrences()/nbToken);
                tokenHashMap.replace(arrayToken.get(i) , tokenDto);
            } else {
                TokenDto tokenDTO = new TokenDto(
                        arrayToken.get(i),
                        1,
                        (float) 1/nbToken
                );
                tokenHashMap.put(arrayToken.get(i), tokenDTO);
            }
        }
        return tokenHashMap.values();
    }

    public ArrayList<PositionResponse> getTokensPosition(@PathVariable String id, @PathVariable String request){
        String url = "https://www.gutenberg.org/files/" + id + "/" + id + "-0.txt";
        String content = this.restTemplate.getForObject(url, String.class);
        ArrayList<ArrayList<PositionResponse>> tokenHashMap = new ArrayList<ArrayList<PositionResponse>>();
        ArrayList<Integer> arrayList = new ArrayList<Integer>();

        String[] listWord = request.split(",");

        String[] tab = content.split("\n");
        ArrayList<PositionResponse> list = new ArrayList<>();
        for (String word : listWord){
            for(int i = 0; i < tab.length; i++) {
                var index = tab[i].indexOf(word);
                if(index != -1)
                    list.add(new PositionResponse(word, i,index));
            }
        }
        return list;
    }

}
