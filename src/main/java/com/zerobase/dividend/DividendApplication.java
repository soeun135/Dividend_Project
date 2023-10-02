package com.zerobase.dividend;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class DividendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DividendApplication.class, args);

		try {
			//web사이트에요청을 보내서 html 받아와야함
			Connection connection = Jsoup.connect("https://finance.yahoo.com/quote/COKE/history?period1=99100800&period2=1695945600&interval=1mo&filter=history&frequency=1mo&includeAdjustedClose=true");//인자로 연결 요청할 URL
			Document document = connection.get();

			Elements eles =
					document.getElementsByAttributeValue("data-test", "historical-prices");
			Element ele = eles.get(0); //table 전체

			//thead = 0 tbody = 1 tfoot = 2
			Element tbody = ele.children().get(1);
			for (Element e : tbody.children()) {
				String txt = e.text();

				if(!txt.endsWith("Dividend")) {
					continue;
				}
				String[] splits = txt.split(" ");
				String month = splits[0];
				int day = Integer.valueOf(splits[1].replace(",",""));
				int year = Integer.valueOf(splits[2]);
				String dividend = splits[3];

				System.out.println(year + "/" + month + "/" + day + " -> " + dividend);
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
