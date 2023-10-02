package com.zerobase.dividend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.ArrayList;

@Data
@AllArgsConstructor
public class ScrapedResult {
    private Company company;
    private List<Dividend> dividendEntities;
    public ScrapedResult() {
        this.dividendEntities = new ArrayList<>();
    }
}
