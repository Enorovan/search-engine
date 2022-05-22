package com.mrec.bibliotheque.controller.response;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PositionResponse {
    String token;
    Integer line;
    Integer column;
}
