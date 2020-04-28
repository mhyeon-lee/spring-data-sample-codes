package spring.data.r2dbc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.Clob;
import io.r2dbc.spi.ConnectionFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.h2.Driver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.liquibase.DataSourceClosingSpringLiquibase;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.transaction.ReactiveTransactionManager;
import spring.data.r2dbc.comment.CommentContent;
import spring.data.r2dbc.issue.IssueAttachedLabel;
import spring.data.r2dbc.issue.IssueContent;
import spring.data.r2dbc.support.*;

import javax.sql.DataSource;
import javax.validation.Validator;
import java.time.Instant;
import java.util.List;

@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
public class R2dbcConfig extends AbstractR2dbcConfiguration {
    private static final String URL = "mem:testdb;DB_CLOSE_DELAY=-1;";
    private static final String USER_NAME = "sa";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    @Override
    public ConnectionFactory connectionFactory() {
        return new H2ConnectionFactory(
            H2ConnectionConfiguration.builder()
                .url(URL)
                .username(USER_NAME)
                .build());
    }

    @Bean
    @Override
    public R2dbcDialect getDialect(ConnectionFactory connectionFactory) {
        return DialectResolver.getDialect(connectionFactory);
    }

    @Bean
    R2dbcEntityOperations r2dbcEntityOperations(
        DatabaseClient databaseClient, ReactiveDataAccessStrategy dataAccessStrategy) {

        return new R2dbcEntityTemplate(databaseClient, dataAccessStrategy);
    }

    @Bean
    ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "spring.liquibase.enabled", havingValue = "true", matchIfMissing = true)
    SpringLiquibase liquibase(LiquibaseProperties properties) {
        SpringLiquibase liquibase = new DataSourceClosingSpringLiquibase();
        liquibase.setChangeLog(properties.getChangeLog());
        liquibase.setDataSource(this.liquibaseDataSource());
        return liquibase;
    }

    @Bean
    RepositoryValidationAop repositoryValidationAop(Validator validator) {
        return new RepositoryValidationAop(validator);
    }

    @Bean
    PersistableMarkNotNewAop persistableMarkNotNewAop() {
        return new PersistableMarkNotNewAop();
    }

    private DataSource liquibaseDataSource() {
        String url = "jdbc:h2:" + URL + "DB_CLOSE_ON_EXIT=false;";
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(USER_NAME);
        hikariConfig.setDriverClassName(Driver.class.getName());
        return new HikariDataSource(hikariConfig);
    }

    @Override
    protected List<Object> getCustomConverters() {
        Encryptor encryptor = new SimpleEncryptor();
        return List.of(
            new EncryptStringWritingConverter(encryptor),
            new EncryptStringReadingConverter(encryptor),
            new ClobReadingConverter<>() {
                @Override
                public String convert(Clob source) {
                    return convertToString(source)
                        .blockOptional()
                        .orElse("");
                }
            },
            new Converter<Instant, Instant>() {
                @Override
                public Instant convert(Instant source) {
                    return source;
                }
            },
            new ClobJsonReadingConverter<>(IssueContent.class, this.objectMapper) {},
            new JsonStringWritingConverter<>(IssueContent.class, this.objectMapper) {},
            new ClobJsonReadingConverter<>(CommentContent.class, this.objectMapper) {},
            new JsonStringWritingConverter<>(CommentContent.class, this.objectMapper) {}
        );
    }

    @WritingConverter
    static class EncryptStringWritingConverter implements Converter<EncryptString, byte[]> {
        private final Encryptor encryptor;

        public EncryptStringWritingConverter(Encryptor encryptor) {
            this.encryptor = encryptor;
        }

        @Override
        public byte[] convert(EncryptString source) {
            return this.encryptor.encrypt(source.getValue());
        }
    }

    @ReadingConverter
    static class EncryptStringReadingConverter implements Converter<byte[], EncryptString> {
        private final Encryptor encryptor;

        public EncryptStringReadingConverter(Encryptor encryptor) {
            this.encryptor = encryptor;
        }

        @Override
        public EncryptString convert(byte[] source) {
            String value = this.encryptor.decrypt(source);
            if (value == null) {
                return null;
            }

            return new EncryptString(value);
        }
    }
}
