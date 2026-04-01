package com.movierental.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@Profile("mysql")
public class MySqlConfig {

    @Bean
    public DataSource dataSource(@Value("${spring.datasource.url}") String url,
                                 @Value("${spring.datasource.username}") String username,
                                 @Value("${spring.datasource.password}") String password,
                                 @Value("${spring.datasource.driver-class-name}") String driverClassName) {
        org.springframework.boot.jdbc.DataSourceBuilder<?> builder = org.springframework.boot.jdbc.DataSourceBuilder.create();
        builder.url(url);
        builder.username(username);
        builder.password(password);
        builder.driverClassName(driverClassName);
        return builder.build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
