package io.pillopl.library.lending.book.infrastructure;

import io.pillopl.library.lending.book.new_model.BookRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class NewBookConfiguration {

    @Bean
    BookRepository newBookRepository(JdbcTemplate jdbcTemplate) {
        return new NewBookDatabaseRepository(jdbcTemplate);
    }
}
