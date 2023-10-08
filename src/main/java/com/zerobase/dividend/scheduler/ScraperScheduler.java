package com.zerobase.dividend.scheduler;

import com.zerobase.dividend.model.Company;
import com.zerobase.dividend.model.ScrapedResult;
import com.zerobase.dividend.persist.CompanyRepository;
import com.zerobase.dividend.persist.DividendRepository;
import com.zerobase.dividend.persist.entity.CompanyEntity;
import com.zerobase.dividend.persist.entity.DividendEntity;
import com.zerobase.dividend.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.zerobase.dividend.model.constants.CacheKey.KEY_FINANCE;

@Slf4j
@Component
@AllArgsConstructor
@EnableCaching
public class ScraperScheduler {
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    private final Scraper yahooFinanceScraper;

    @CacheEvict(value = KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {
        log.info("scraping scheduler is started");
        List<CompanyEntity> companyEntities = this.companyRepository.findAll();

        for (var companyEntity : companyEntities) {
            ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(
                    new Company(companyEntity.getTicker(), companyEntity.getName()));

            scrapedResult.getDividends().stream()
                    .map(e -> new DividendEntity(companyEntity.getId(), e))
                    .forEach(e -> {
                        boolean exists =
                                this.dividendRepository.existsByCompanyIdAndDate
                                        (e.getCompanyId(), e.getDate());
                        if (!exists) {
                            this.dividendRepository.save(e);
                            log.info("insert new dividend -> " + e.toString());
                        }
                    });
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}