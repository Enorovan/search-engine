package com.mrec.bibliotheque.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Collection;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;
import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "books")
public class Book {
    @Id
    private String id;

    @Field(type = Text, name = "title")
    private String title;

    @Field(type = Text, name = "author")
    private String author;

    @Field(type = Text, name = "urlImage")
    private String urlImage;

    @Field(type = Keyword, name = "keywords")
    private String[] keywords;

    @Field(type = Text, name = "languages")
    private String[] languages;

    @Field(type = FieldType.Object, name = "token")
    private Collection<TokenDto> token;

    @Field(type = FieldType.Integer, name = "nbClick")
    private int nbClick = 0;
}