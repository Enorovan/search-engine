package com.mrec.bibliotheque.model;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class TokenDto{
    String token;
    int occurrences;
    float frequencies;
}