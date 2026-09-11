package com.nnp.common.config.service.impl.jdbc;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceSpringConfig {

	private final String user;
	private final String password;
	private final String url;
	private final int maxPoolSize;
	private final int minIdle;

	public DataSourceSpringConfig(
			@Value("${db.user}") String user,
			@Value("${db.password}") String password,
			@Value("${db.url}") String url,
			@Value("${spring.datasource.hikari.maximum-pool-size:20}") int maxPoolSize,
			@Value("${spring.datasource.hikari.minimum-idle:10}") int minIdle) {
		this.user = user;
		this.password = password;
		this.url = url;
		this.maxPoolSize = maxPoolSize;
		this.minIdle = minIdle;
	}

	@Bean("nnpDataSource")
	@Primary
	public DataSource getNnpDataSource() {
		DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
		dataSourceBuilder.driverClassName("org.postgresql.Driver");
		dataSourceBuilder.password(password);
		dataSourceBuilder.username(user);
		dataSourceBuilder.url(url);

		HikariDataSource dataSource = (HikariDataSource) dataSourceBuilder.build();
		dataSource.setMaximumPoolSize(maxPoolSize);
		dataSource.setMinimumIdle(minIdle);

		return dataSource;
	}
}
