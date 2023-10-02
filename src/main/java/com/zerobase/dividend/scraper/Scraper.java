package com.zerobase.dividend.scraper;

import com.zerobase.dividend.model.Company;
import com.zerobase.dividend.model.ScrapedResult;

public interface Scraper {
    ScrapedResult scrap(Company company);
    Company scrapCompanyByTicker(String ticker);
}
