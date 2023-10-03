package com.zerobase.dividend.service;

import com.zerobase.dividend.model.Company;
import com.zerobase.dividend.model.ScrapedResult;
import com.zerobase.dividend.persist.CompanyRepository;
import com.zerobase.dividend.persist.DividendRepository;
import com.zerobase.dividend.persist.entity.CompanyEntity;
import com.zerobase.dividend.persist.entity.DividendEntity;
import com.zerobase.dividend.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Service
@AllArgsConstructor
public class CompanyService {
    private final Scraper yahooFinanceScraper;
    private final DividendRepository dividendRepository;
    private final CompanyRepository companyRepository;

    public Company save(String ticker) {
        //아래 메소드가 private으로 만들어놓음
        // 이미 저장된 회사인지 판별하는 작업 한 단계 추가
        //우리가 DB에 저장하지 않은 회사인 경우에만 아래ㅐ 메소드 호출
        //CompanyRepository에 existsByTicker(String ticker); 메소드 추가
        boolean exists = this.companyRepository.existsByTicker(ticker);
        if (exists) {
            throw new RuntimeException("already exists ticker -> " + ticker);
        }
        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return this.companyRepository.findAll(pageable);
    }
    private Company storeCompanyAndDividend(String ticker) {
        // ticker를 받아서 저장한 회사 정보 Company 반환
        
        //ticker를 기준으로 회사를 스크래핑
        Company company =
                this.yahooFinanceScraper.scrapCompanyByTicker(ticker);
         if (ObjectUtils.isEmpty(company)) {
             throw new RuntimeException("failed to scrap ticker ->" + ticker);
         }

        //해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

        //스크래핑 결과 저장해서 저장한 company 정보 반환
        //스크래핑 결과 저장할 때 배당금 entity에 DividendEntity에 company_id까지 저장해야함.
        //그래서 회사정보 먼저 저장하고 결과로 받은 entity에서 company_id를 가져와서
        //dividend모델 클래스를 entity로 매핑시키면서 company_id를 같이 가져와서 저장.
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntities =
                scrapedResult.getDividends().stream()
                .map(e -> new DividendEntity(companyEntity.getId(), e))
                .collect(Collectors.toList());
        this.dividendRepository.saveAll(dividendEntities);
        return company;
    }
}
