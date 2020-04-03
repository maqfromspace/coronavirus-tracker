package com.maqfromspace.coronavirustracker.controller;

import com.maqfromspace.coronavirustracker.model.CovidData;
import com.maqfromspace.coronavirustracker.service.DataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Slf4j
@Controller("/")
public class MainController {

    @Autowired
    DataService dataService;

    @GetMapping
    public String home(Model model) {
        List<CovidData> covidDataList = dataService.getCovidDataList();

        int totalConfirmed = dataService.getTotalConfirmed();
        int totalDeaths = dataService.getTotalDeaths();
        int totalRecovered = dataService.getTotalRecovered();

        int newConfirmed = dataService.getNewConfirmed();
        int newDeath = dataService.getNewDeath();
        int newRecovered = dataService.getNewRecovered();

        model.addAttribute("allData", covidDataList);
        model.addAttribute("totalReportedCases", totalConfirmed);
        model.addAttribute("totalNewCases", newConfirmed);
        model.addAttribute("totalDeathsReportedCases", totalDeaths);
        model.addAttribute("totalNewDeathsCases", newDeath);
        model.addAttribute("totalReportedCasesRecovered", totalRecovered);
        model.addAttribute("totalNewCasesRecovered", newRecovered);
        return "home";
    }

    @GetMapping("/graph/{countryName}")
    public String graph(@PathVariable String countryName, Model model) {
        CovidData countryData = dataService.findByEnglishName(countryName);
        model.addAttribute("countryData", countryData);
        return "graph";
    }
}