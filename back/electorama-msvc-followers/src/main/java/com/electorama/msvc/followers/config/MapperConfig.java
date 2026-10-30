package com.electorama.msvc.followers.config;

import com.electorama.msvc.followers.model.mapper.PreregisterMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

  @Bean
  public PreregisterMapper preregisterMapper() {
    return Mappers.getMapper(PreregisterMapper.class);
  }
}
