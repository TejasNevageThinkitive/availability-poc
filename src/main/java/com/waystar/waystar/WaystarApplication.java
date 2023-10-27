package com.waystar.waystar;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WaystarApplication {

	public static void main(String[] args) {
		SpringApplication.run(WaystarApplication.class, args);
	}

	@Bean
	public ModelMapper createModelMapperBean(){
		return new ModelMapper();
	}

}
