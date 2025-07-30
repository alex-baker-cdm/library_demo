package io.pillopl.library.lending.book.infrastructure;

import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.book.application.CreateAvailableBookOnInstanceAddedEventHandler;
import io.pillopl.library.lending.book.application.PatronEventsHandler;
import io.pillopl.library.lending.book.model.BookRepository;
import io.pillopl.library.lending.book.new_model.infrastructure.NewBookDatabaseRepository;
import io.pillopl.library.lending.book.new_model.infrastructure.BookRepositoryAdapter;
import io.pillopl.library.lending.book.new_model.infrastructure.FindAvailableBookAdapter;
import io.pillopl.library.lending.book.new_model.infrastructure.FindBookOnHoldAdapter;
import io.pillopl.library.lending.patron.application.hold.FindAvailableBook;
import io.pillopl.library.lending.patron.application.hold.FindBookOnHold;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class BookConfiguration {

    @Bean
    CreateAvailableBookOnInstanceAddedEventHandler createAvailableBookOnInstanceAddedEventHandler(BookRepository bookRepository) {
        return new CreateAvailableBookOnInstanceAddedEventHandler(bookRepository);
    }

    @Bean
    PatronEventsHandler bookEventsHandler(BookRepository bookRepository, DomainEvents domainEvents) {
        return new PatronEventsHandler(bookRepository, domainEvents);
    }

    @Bean
    BookDatabaseRepository bookDatabaseRepository(JdbcTemplate jdbcTemplate) {
        return new BookDatabaseRepository(jdbcTemplate);
    }

    @Bean
    NewBookDatabaseRepository newBookDatabaseRepository(JdbcTemplate jdbcTemplate) {
        return new NewBookDatabaseRepository(jdbcTemplate);
    }

    @Bean
    @Primary
    BookRepositoryAdapter bookRepositoryAdapter(NewBookDatabaseRepository newBookRepository) {
        return new BookRepositoryAdapter(newBookRepository);
    }

    @Bean
    @Primary
    FindAvailableBookAdapter findAvailableBookAdapter(NewBookDatabaseRepository newBookRepository) {
        return new FindAvailableBookAdapter(newBookRepository);
    }

    @Bean
    @Primary
    FindBookOnHoldAdapter findBookOnHoldAdapter(NewBookDatabaseRepository newBookRepository) {
        return new FindBookOnHoldAdapter(newBookRepository);
    }
}
