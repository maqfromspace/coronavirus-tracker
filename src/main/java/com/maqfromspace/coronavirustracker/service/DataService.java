package com.maqfromspace.coronavirustracker.service;

import com.maqfromspace.coronavirustracker.model.CovidData;
import com.maqfromspace.coronavirustracker.model.Point;
import com.maqfromspace.coronavirustracker.model.TypeOfData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
@Slf4j
@Service
@EnableScheduling
public class DataService {
    //Источники
    private static String CONFIRMED_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    private static String DEATHS_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv";
    private static String RECOVERED_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_recovered_global.csv";

    //Агрегированные данные
    List<CovidData> covidDataList = new ArrayList<>();

    //Заголовки
    List<String> headers = new ArrayList<>();

    //Мапа с англо-русскими названиями стран
    @Autowired
    @Qualifier("countryNames")
    HashMap<String, String> countryNames;


    /**
     * Получение данных с источника
     * @throws IOException
     */
    @PostConstruct
    @Scheduled(cron = "0 0 4 * * ?")
    public void fetchDataFromSource() throws IOException {

        String data = new RestTemplate()
                .getForEntity(CONFIRMED_URL, String.class)
                .getBody();
        StringReader headerReader = new StringReader(data);
        StringReader reader = new StringReader(data);

        headers = new CSVParser(headerReader, CSVFormat.DEFAULT.withHeader())
                .getHeaderNames();

        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

        //Создаем список стран с заполненными русскими и английскими названиями стран
        for (CSVRecord record : records) {
            String countryName = countryNames.getOrDefault(record.get("Country/Region"), record.get("Country/Region"));
            if (findByRussianName(countryName) == null) {
                CovidData covidData = new CovidData();
                covidData.setRussianName(countryNames.getOrDefault(record.get("Country/Region"), record.get("Country/Region")));
                covidData.setEnglishName(record.get("Country/Region"));
                covidDataList.add(covidData);
            }
        }

        updateDataByType(TypeOfData.CONFIRMED);
        updateDataByType(TypeOfData.DEATHS);
        updateDataByType(TypeOfData.RECOVERED);
        log.info("Данные обновлены.");
    }

    /**
     * Поиск данных для страны с названием country(ru)
     *
     * @param country - название страны
     * @return данные о заболевании для страны country
     */
    private CovidData findByRussianName(String country) {
        return covidDataList.stream()
                .filter(countryData -> country.equals(countryData.getRussianName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Поиск данных для страны с названием country(en)
     *
     * @param country - название страны
     * @return данные о заболевании для страны country
     */
    public CovidData findByEnglishName(String country) {
        return covidDataList.stream()
                .filter(countryData -> country.equals(countryData.getEnglishName()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "entity not found"
                ));
    }

    /**
     * Обновление данных определенного типа
     *
     * @param typeOfData
     * @throws IOException
     */
    private void updateDataByType(TypeOfData typeOfData) throws IOException {
        String URL = "";
        switch (typeOfData) {
            case CONFIRMED:
                URL = CONFIRMED_URL;
                break;
            case DEATHS:
                URL = DEATHS_URL;
                break;
            case RECOVERED:
                URL = RECOVERED_URL;
                break;
        }
        String csv = new RestTemplate()
                .getForEntity(URL, String.class)
                .getBody();
        StringReader dataReader = new StringReader(csv);

        Iterable<CSVRecord> dataRecords = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(dataReader);
        for (CSVRecord record : dataRecords) {
            String countryName = countryNames.getOrDefault(record.get("Country/Region"), record.get("Country/Region"));
            CovidData covidData = findByRussianName(countryName);
            List<Point> newPoints = new ArrayList<>();
            List<Point> oldPoints = covidData.getPointByType(typeOfData);
            int latestCases = Integer.parseInt(record.get(record.size() - 1));
            int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
            covidData.addTotalCasesByType(latestCases, typeOfData);
            covidData.addPrevDayCasesByType(latestCases - prevDayCases, typeOfData);
            int pointer = 0;
            if (oldPoints == null)
                for (int i = 4; i < record.size(); i++)
                    newPoints.add(new Point(headers.get(i), Integer.parseInt(record.get(i))));
            else
                for (int i = 4; i < record.size(); i++)
                    newPoints.add(new Point(headers.get(i), Integer.parseInt(record.get(i)) + oldPoints.get(pointer++).getY()));
            covidData.setPointsByType(newPoints, typeOfData);
        }
    }

    //Методы получения агрегированных данных
    public int getTotalConfirmed() {
        return covidDataList.stream().mapToInt(CovidData::getLatestTotalCases).sum();
    }

    public int getNewConfirmed() {
        return covidDataList.stream().mapToInt(CovidData::getDiffFromPrevDay).sum();
    }

    public int getTotalDeaths() {
        return covidDataList.stream().mapToInt(CovidData::getLatestTotalDeaths).sum();
    }

    public int getNewDeath() {
        return covidDataList.stream().mapToInt(CovidData::getDiffDeathsFromPrevDay).sum();
    }

    public int getTotalRecovered() {
        return covidDataList.stream().mapToInt(CovidData::getLatestTotalRecovered).sum();
    }

    public int getNewRecovered() {
        return covidDataList.stream().mapToInt(CovidData::getDiffFromPrevDayRecovered).sum();
    }
}