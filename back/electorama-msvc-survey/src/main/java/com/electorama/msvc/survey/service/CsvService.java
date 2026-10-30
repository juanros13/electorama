package com.electorama.msvc.survey.service;

import com.electorama.msvc.survey.model.dto.CsvDto;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CsvService {

  public List<CsvDto> readCsv(String filePath) {
    List<CsvDto> users = new ArrayList<>();

    try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
      String[] line;
      reader.readNext(); // Skip header line
      while ((line = reader.readNext()) != null) {
        CsvDto user =
            new CsvDto(
                line[0], line[1], line[2], line[3], line[4], line[5], Integer.parseInt(line[6]));
        users.add(user);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return users;
  }
}
