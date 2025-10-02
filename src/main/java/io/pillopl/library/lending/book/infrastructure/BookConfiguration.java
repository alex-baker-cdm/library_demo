package io.pillopl.library.lending.book.infrastructure;

import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.book.application.CreateAvailableBookOnInstanceAddedEventHandler;
import io.pillopl.library.lending.book.application.PatronEventsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class BookConfiguration {

    @Bean
    CreateAvailableBookOnInstanceAddedEventHandler createAvailableBookOnInstanceAddedEventHandler(
            io.pillopl.library.lending.book.new_model.BookRepository newBookRepository) {
        return new CreateAvailableBookOnInstanceAddedEventHandler(newBookRepository);
    }

    @Bean
    PatronEventsHandler bookEventsHandler(
            io.pillopl.library.lending.book.new_model.BookRepository newBookRepository,
            DomainEvents domainEvents) {
        return new PatronEventsHandler(newBookRepository, domainEvents);
    }

    @Bean
    io.pillopl.library.lending.book.new_model.BookRepository newBookRepository(JdbcTemplate jdbcTemplate) {
        return new NewBookDatabaseRepository(jdbcTemplate);
    }

    @Bean
    io.pillopl.library.lending.book.model.BookRepository bookDatabaseRepository(JdbcTemplate jdbcTemplate) {
        return new BookDatabaseRepository(jdbcTemplate);
    }
}
