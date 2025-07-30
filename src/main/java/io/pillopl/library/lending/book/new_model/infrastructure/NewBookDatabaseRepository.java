package io.pillopl.library.lending.book.new_model.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.aggregates.AggregateRootIsStale;
import io.pillopl.library.lending.book.new_model.*;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import static io.vavr.control.Option.none;
import static io.vavr.control.Option.of;

public class NewBookDatabaseRepository implements BookRepository {

    private final JdbcTemplate jdbcTemplate;

    public NewBookDatabaseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Option<Book> findBy(BookId bookId) {
        return findBookById(bookId)
                .map(NewBookDatabaseEntity::toDomainModel);
    }

    private Option<NewBookDatabaseEntity> findBookById(BookId bookId) {
        return Try
                .ofSupplier(() -> of(jdbcTemplate.queryForObject(
                    "SELECT b.* FROM book_database_entity b WHERE b.book_id = ?", 
                    new BeanPropertyRowMapper<>(NewBookDatabaseEntity.class), 
                    bookId.getBookId())))
                .getOrElse(none());
    }

    @Override
    public void save(Book book) {
        findBy(book.getBookId())
                .map(existingBook -> updateOptimistically(book))
                .onEmpty(() -> insertNew(book));
    }

    private int updateOptimistically(Book book) {
        NewBookDatabaseEntity entity = NewBookDatabaseEntity.fromDomainModel(book);
        
        int result = jdbcTemplate.update(
            "UPDATE book_database_entity SET book_state = ?, available_at_branch = ?, " +
            "on_hold_at_branch = ?, on_hold_by_patron = ?, on_hold_till = ?, " +
            "checked_out_at_branch = ?, checked_out_by_patron = ?, version = ? " +
            "WHERE book_id = ? AND version = ?",
            entity.book_state,
            entity.available_at_branch,
            entity.on_hold_at_branch, 
            entity.on_hold_by_patron,
            entity.on_hold_till,
            entity.checked_out_at_branch,
            entity.checked_out_by_patron,
            entity.version + 1,
            entity.book_id,
            entity.version);
            
        if (result == 0) {
            throw new AggregateRootIsStale("Someone has updated book in the meantime, book: " + book);
        }
        return result;
    }

    private void insertNew(Book book) {
        NewBookDatabaseEntity entity = NewBookDatabaseEntity.fromDomainModel(book);
        
        jdbcTemplate.update(
            "INSERT INTO book_database_entity " +
            "(id, book_id, book_type, book_state, available_at_branch, " +
            "on_hold_at_branch, on_hold_by_patron, on_hold_till, " +
            "checked_out_at_branch, checked_out_by_patron, version) VALUES " +
            "(book_database_entity_seq.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)",
            entity.book_id, entity.book_type.toString(), entity.book_state,
            entity.available_at_branch, entity.on_hold_at_branch, entity.on_hold_by_patron,
            entity.on_hold_till, entity.checked_out_at_branch, entity.checked_out_by_patron);
    }
}
