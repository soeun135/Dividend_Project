package com.zerobase.dividend.service;

import com.zerobase.dividend.model.Company;
import com.zerobase.dividend.model.Dividend;
import com.zerobase.dividend.model.ScrapedResult;
import com.zerobase.dividend.persist.CompanyRepository;
import com.zerobase.dividend.persist.DividendRepository;
import com.zerobase.dividend.persist.entity.CompanyEntity;
import com.zerobase.dividend.persist.entity.DividendEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.zerobase.dividend.model.constants.CacheKey.KEY_FINANCE;

@Service
@AllArgsConstructor
@Slf4j
public class FinanceService {
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    // 요청이 자주 들어오는가? (캐시 사용하기 적합) => O
    // 자주 변경되는 데이터인가 ? (캐시 사용하기 부적합) => X

    @Cacheable(key = "#companyName", value = KEY_FINANCE) //레디스 서버의 key value와 의미 다름
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search companu -> " + companyName);
        //1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회사명입니다."));
        //orElseThrow는 값이 없으면 예외 발생, 값이 있으면 Optional이 벗겨진 알맹이 리턴

        //2. 조회된 회사 ID로 배당금 정보 조회
        List<DividendEntity> dividendEntities =
                this.dividendRepository.findAllByCompanyId(company.getId());

        //3. 결과 조합후 반환
//        List<Dividend> dividends = new ArrayList<>();
//        for (var entity : dividendEntities) {
//            dividends.add(Dividend.builder()
//                    .date(entity.getDate())
//                    .dividend(entity.getDividend())
//                    .build());
//        }
        //stream으로 구현
        List<Dividend> dividends = dividendEntities.stream()
                .map(e -> new Dividend(e.getDate(), e.getDividend()))
                .collect(Collectors.toList());
        return new ScrapedResult(
                new Company(company.getTicker(), company.getName())
                , dividends);
    }
}
