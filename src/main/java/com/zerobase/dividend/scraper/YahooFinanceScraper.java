package com.zerobase.dividend.scraper;

import com.zerobase.dividend.model.Company;
import com.zerobase.dividend.model.Dividend;
import com.zerobase.dividend.model.ScrapedResult;
import com.zerobase.dividend.model.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class YahooFinanceScraper implements Scraper{
    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";
    private static final long START_TIME = 86400; // 60 * 60 * 24

    @Override
    public ScrapedResult scrap(Company company) {
        var scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);
        try {
            long now = System.currentTimeMillis() / 1000;
            //현재 시간을 ms 단위로 받아서 초 단위로 바꿔주기위해 /1000

            String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
            //web사이트에요청을 보내서 html 받아와야함
            Connection connection = Jsoup.connect(url);//인자로 연결 요청할 URL
            Document document = connection.get();

            Elements parsingDivs =
                    document.getElementsByAttributeValue("data-test", "historical-prices");
            Element tableEle = parsingDivs.get(0); //table 전체

            //thead = 0 tbody = 1 tfoot = 2
            Element tbody = tableEle.children().get(1);

            List<Dividend> dividends = new ArrayList<>();
            for (Element e : tbody.children()) {
                String txt = e.text();

                if (!txt.endsWith("Dividend")) {
                    continue;
                }
                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                int day = Integer.valueOf(splits[1].replace(",", ""));
                int year = Integer.valueOf(splits[2]);
                String dividend = splits[3];

                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value ->" + splits[0]);
                }
                dividends.add(Dividend.builder()
                        .date(LocalDateTime.of(year, month, day, 0, 0))
                        .dividend(dividend)
                        .build());
            }
            scrapResult.setDividendEntities(dividends);

        } catch (IOException e) {
            //TODO
            throw new RuntimeException(e);
        }
        return scrapResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker, ticker);

        try {
            Document document = Jsoup.connect(url).get();
            Element titleEle = document.getElementsByTag("h1").get(0);

            String title = titleEle.text().split(" - ")[0].trim();
            //여기가 [1]인데 이걸로 하면 인덱스범위초과 오류가남.
            return Company.builder()
                    .ticker(ticker)
                    .name(title)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
