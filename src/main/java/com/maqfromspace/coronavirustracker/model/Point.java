package com.maqfromspace.coronavirustracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Сущность точки
 */
@AllArgsConstructor
@Data
public class Point {
    String x;
    int y;
}
