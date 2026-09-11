package com.nnp.common.config;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = { "com.nnp.common.config" })
@EnableJpaRepositories(basePackages = { "com.nnp.common.config.repo" })
@EntityScan(basePackages = { "com.nnp.common.config.entity" })
@EnableJpaAuditing
@EnableConfigServer
public class NNPConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(NNPConfigServerApplication.class, args);
	}

	@Bean(name = "modelMapper")
	public ModelMapper getModelMapper() {
		return new ModelMapper();
	}
}
