package com.maqfromspace.coronavirustracker.configuration;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maqfromspace.coronavirustracker.model.CountryName;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

@Configuration
public class BeanConfiguration {

    /**
     * Бин, содержащий мапу с англо-русскими названиями стран.
     * Билдится из файла
     *
     * @return мапа с англо-русскими названиями стран
     * @throws IOException
     */
    @Bean("countryNames")
    public HashMap<String, String> name() throws IOException {
        File file = ResourceUtils.getFile("countryNames.json");
        String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        HashMap<String, String> result = new HashMap<>();
        List<CountryName> countriesList = new Gson()
                .fromJson(content, new TypeToken<List<CountryName>>() {
                }.getType());
        countriesList.forEach(countryName -> result.put(countryName.getEN(), countryName.getRU()));
        return result;
    }
}
