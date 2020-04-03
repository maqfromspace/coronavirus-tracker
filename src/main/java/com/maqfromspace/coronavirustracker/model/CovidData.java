package com.maqfromspace.coronavirustracker.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Сущность данных о короне
 */
@Data
public class CovidData {

    private String russianName;
    private String englishName;
    private int latestTotalCases;
    private int diffFromPrevDay;
    private int latestTotalDeaths;
    private int diffDeathsFromPrevDay;
    private int latestTotalRecovered;
    private int diffFromPrevDayRecovered;
    private List<Point> confirmed;
    private List<Point> deaths;
    private List<Point> recovered;
    private List<Point> confirmedByDay;
    private List<Point> deathsByDay;
    private List<Point> recoveredByDay;

    /**
     * Геттер для точек определенного типа
     *
     * @param typeOfData - тип данных
     * @return точки
     */
    public List<Point> getPointByType(TypeOfData typeOfData) {
        List<Point> points = null;
        switch (typeOfData) {
            case CONFIRMED:
                points = getConfirmed();
                break;
            case DEATHS:
                points = getDeaths();
                break;
            case RECOVERED:
                points = getRecovered();
        }
        return points;
    }

    /**
     * Сеттер для точек определенного типа
     *
     * @param points     - точки
     * @param typeOfData тип задаваемых точек
     */
    public void setPointsByType(List<Point> points, TypeOfData typeOfData) {
        switch (typeOfData) {
            case CONFIRMED:
                confirmed = points;
                confirmedByDay = calculateCasesByDay(points);
                break;
            case DEATHS:
                deaths = points;
                deathsByDay = calculateCasesByDay(points);
                break;
            case RECOVERED:
                recovered = points;
                recoveredByDay = calculateCasesByDay(points);
                break;
        }
    }

    /**
     * Метод увеличения числа случаев определенного типа за всё время
     * @param latestCases добавляемые случаи
     * @param typeOfData тип случая
     */
    public void addTotalCasesByType(int latestCases, TypeOfData typeOfData) {
        switch (typeOfData) {
            case CONFIRMED:
                latestTotalCases += latestCases;
                break;
            case DEATHS:
                latestTotalDeaths += latestCases;
                break;
            case RECOVERED:
                latestTotalRecovered += latestCases;
                break;
        }
    }

    /**
     * Метод увеличения числа случаев определенного типа за день
     * @param latestCases добавляемые случаи
     * @param typeOfData тип случая
     */
    public void addPrevDayCasesByType(int latestCases, TypeOfData typeOfData) {
        switch (typeOfData) {
            case CONFIRMED:
                diffFromPrevDay += latestCases;
                break;
            case DEATHS:
                diffDeathsFromPrevDay += latestCases;
                break;
            case RECOVERED:
                diffFromPrevDayRecovered += latestCases;
                break;
        }
    }

    /**
     * Метод получения точек посуточного изменения данных, основанный на данных за всё время
     * @param allCases данные за всё время
     * @return список точек, отражающих посуточное изменение
     */
    private List<Point> calculateCasesByDay(List<Point> allCases) {
        List<Point> casesByDay = new ArrayList<>(allCases);
        for (int i = 1; i < casesByDay.size(); i++) {
            String x = casesByDay.get(i).getX();
            Integer currentDay = allCases.get(i).getY();
            Integer previousDay = allCases.get(i - 1).getY();
            casesByDay.set(i, new Point(x, currentDay - previousDay));
        }
        return casesByDay;
    }
}