package com.zerobase.dividend.scraper;

import com.zerobase.dividend.exception.Impl.NotExistTicker;
import com.zerobase.dividend.model.Company;
import com.zerobase.dividend.model.Dividend;
import com.zerobase.dividend.model.ScrapedResult;
import com.zerobase.dividend.model.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper{
    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";
    private static final long START_TIME = 86400; // 60 * 60 * 24

    //회사의 배당금 정보 스크래핑
    @Override
    public ScrapedResult scrap(Company company) {
        var scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);
        try {
            long now = System.currentTimeMillis() / 1000; //ms에서 s단위로 바꿔서 쓰기 위해 /1000
            String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);

            Connection connection = Jsoup.connect(url);
            Document document = connection.get();

            Elements parsingDivs = document.getElementsByAttributeValue("data-test", "historical-prices");
            Element tableEle = parsingDivs.get(0);

            //thead = 0 tbody = 1 tfoot = 2
            Element tbody = tableEle.children().get(1);

            List<Dividend> dividends = new ArrayList<>();
            for (Element e : tbody.children()) {
                String txt = e.text();
                if (!txt.endsWith("Dividend")) {
                    continue;
                }
                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]); //객체 생성 안 하고 쓸 수 있는 건 메소드가 static 이어서
                int day = Integer.parseInt(splits[1].replace(",", ""));
                int year = Integer.parseInt(splits[2]);
                String dividend = splits[3];

                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value ->" + splits[0]);
                }
                dividends.add(new Dividend(
                        LocalDateTime.of(year, month, day, 0, 0),
                        dividend));
            }
            scrapResult.setDividends(dividends);

        } catch (IOException e) {
            //TODO
            e.printStackTrace();
        }
        return scrapResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker, ticker);

        try {
            Document document = Jsoup.connect(url).get();

            Element titleEle = null;
            try {
                titleEle = document.getElementsByTag("h1").get(0);
            } catch (IndexOutOfBoundsException e) {
                throw new NotExistTicker(ticker);
            }
            String title = titleEle.text();
            title = title.substring(0, title.length() - ticker.length() - 2).trim();
            return new Company(ticker, title);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
