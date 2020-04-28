package com.navercorp.spring.sql.groovy.config;

import com.navercorp.spring.data.jdbc.plus.sql.config.JdbcPlusSqlConfiguration;
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.EntityConvertibleSqlParameterSourceFactory;
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.SqlParameterSourceFactory;
import com.navercorp.spring.jdbc.plus.support.parametersource.ConvertibleParameterSourceFactory;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.DefaultJdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.JdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.Unwrapper;
import com.navercorp.spring.sql.groovy.label.Label.LabelAfterSaveEventListener;
import com.navercorp.spring.sql.groovy.repo.Repo.RepoBeforeSaveCallback;
import com.navercorp.spring.sql.groovy.support.EncryptString;
import com.navercorp.spring.sql.groovy.support.Encryptor;
import com.navercorp.spring.sql.groovy.support.SimpleEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.DialectResolver;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.dialect.H2Dialect;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.lang.Nullable;

import java.sql.Clob;
import java.sql.SQLException;
import java.util.List;

@Configuration
public class JdbcConfig extends AbstractJdbcConfiguration {
    @Bean
    public Dialect jdbcDialect(NamedParameterJdbcOperations operations) {
        return new H2Dialect() {
            @Override
            public IdentifierProcessing getIdentifierProcessing() {
                // SQL 작성시 컬럼 대상을 Quoting 해야 할 경우 불편하기 때문에 NONE 으로 설정한다.
                // Quoting 여부에 따른 호환은 DBMS 구현에 따른다.
                return IdentifierProcessing.create(
                    IdentifierProcessing.Quoting.NONE, IdentifierProcessing.LetterCasing.UPPER_CASE);
            }
        };
    }

    @Bean
    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        Encryptor encryptor = new SimpleEncryptor();
        return new JdbcCustomConversions(List.of(
            new EncryptStringWritingConverter(encryptor),
            new EncryptStringReadingConverter(encryptor),
            new Converter<Clob, String>() {
                @Nullable
                @Override
                public String convert(Clob clob) {
                    try {
                        return Math.toIntExact(clob.length()) == 0
                            ? "" : clob.getSubString(1, Math.toIntExact(clob.length()));

                    } catch (SQLException e) {
                        throw new IllegalStateException("Failed to convert CLOB to String.", e);
                    }
                }
            }));
    }

    @Bean
    LabelAfterSaveEventListener labelAfterSaveEventListener() {
        return new LabelAfterSaveEventListener();
    }

    @Bean
    RepoBeforeSaveCallback repoBeforeSaveCallback() {
        return new RepoBeforeSaveCallback();
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

    @Configuration
    static class JdbcSqlConfig extends JdbcPlusSqlConfiguration {
        @Bean
        @Override
        public SqlParameterSourceFactory sqlParameterSourceFactory(
            JdbcMappingContext jdbcMappingContext, JdbcConverter jdbcConverter, Dialect dialect) {

            return new EntityConvertibleSqlParameterSourceFactory(
                this.parameterSourceConverter(),
                jdbcMappingContext,
                jdbcConverter,
                dialect.getIdentifierProcessing());
        }

        private ConvertibleParameterSourceFactory parameterSourceConverter() {
            JdbcParameterSourceConverter converter = new DefaultJdbcParameterSourceConverter(
                List.of(), List.of(new IdOnlyAggregateReferenceUnwrapper())
            );
            ConvertibleParameterSourceFactory parameterSourceFactory = new ConvertibleParameterSourceFactory(converter, null);
            parameterSourceFactory.setPaddingIterableParam(true);
            return parameterSourceFactory;
        }
    }

    static class IdOnlyAggregateReferenceUnwrapper implements Unwrapper<AggregateReference.IdOnlyAggregateReference> {
        @Nullable
        @Override
        public Object unwrap(AggregateReference.IdOnlyAggregateReference source) {
            return source.getId();
        }
    }
}
