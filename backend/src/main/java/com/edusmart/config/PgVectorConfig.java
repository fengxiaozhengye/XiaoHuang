package com.edusmart.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Lazy
@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "pgvector.enabled", havingValue = "true")
public class PgVectorConfig {

    @Value("${pgvector.url:}")
    private String pgUrl;

    @Value("${pgvector.username:postgres}")
    private String pgUsername;

    @Value("${pgvector.password:postgres}")
    private String pgPassword;

    @Bean
    @Lazy
    public HikariDataSource pgVectorDataSource() {
        if (pgUrl == null || pgUrl.isBlank()) {
            log.warn("pgvector.url未配置，跳过向量数据库初始化");
            return null;
        }
        try {
            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(pgUrl);
            ds.setUsername(pgUsername);
            ds.setPassword(pgPassword);
            ds.setMaximumPoolSize(10);
            ds.setDriverClassName("org.postgresql.Driver");
            ds.setInitializationFailTimeout(0);
            return ds;
        } catch (Exception e) {
            log.warn("PostgreSQL连接初始化失败（向量库不可用）: {}", e.getMessage());
            return null;
        }
    }

    @Bean
    @Lazy
    public JdbcTemplate pgVectorJdbcTemplate(HikariDataSource pgVectorDataSource) {
        if (pgVectorDataSource == null) {
            return null;
        }
        return new JdbcTemplate(pgVectorDataSource);
    }
}
