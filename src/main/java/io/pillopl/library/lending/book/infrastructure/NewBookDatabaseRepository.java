package io.pillopl.library.lending.book.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.aggregates.AggregateRootIsStale;
import io.pillopl.library.lending.book.new_model.*;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import static io.pillopl.library.lending.book.infrastructure.BookDatabaseEntity.BookState.*;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.of;

class NewBookDatabaseRepository implements io.pillopl.library.lending.book.new_model.BookRepository {

    private final JdbcTemplate jdbcTemplate;

    NewBookDatabaseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Option<Book> findBy(BookId bookId) {
        return findBookById(bookId)
                .map(BookDatabaseEntity::toNewBook);
    }

    private Option<BookDatabaseEntity> findBookById(BookId bookId) {
        return Try
                .ofSupplier(() -> of(jdbcTemplate.queryForObject(
                    "SELECT b.* FROM book_database_entity b WHERE b.book_id = ?",
                    new BeanPropertyRowMapper<>(BookDatabaseEntity.class),
                    bookId.getBookId())))
                .getOrElse(none());
    }

    @Override
    public void save(Book book) {
        findBy(book.getBookId())
                .map(entity -> updateOptimistically(book))
                .onEmpty(() -> insertNew(book));
    }

    private int updateOptimistically(Book book) {
        BookState state = book.getState();
        int result;
        
        if (state instanceof AvailableState) {
            AvailableState availableState = (AvailableState) state;
            result = jdbcTemplate.update(
                "UPDATE book_database_entity b SET b.book_state = ?, b.available_at_branch = ?, b.version = ? WHERE book_id = ? AND version = ?",
                Available.toString(),
                availableState.getCurrentBranch().getLibraryBranchId(),
                book.getVersion().getVersion() + 1,
                book.getBookId().getBookId(),
                book.getVersion().getVersion());
        } else if (state instanceof OnHoldState) {
            OnHoldState onHoldState = (OnHoldState) state;
            result = jdbcTemplate.update(
                "UPDATE book_database_entity b SET b.book_state = ?, b.on_hold_at_branch = ?, b.on_hold_by_patron = ?, b.on_hold_till = ?, b.version = ? WHERE book_id = ? AND version = ?",
                OnHold.toString(),
                onHoldState.getHoldPlacedAt().getLibraryBranchId(),
                onHoldState.getByPatron().getPatronId(),
                onHoldState.getHoldTill(),
                book.getVersion().getVersion() + 1,
                book.getBookId().getBookId(),
                book.getVersion().getVersion());
        } else if (state instanceof CheckedOutState) {
            CheckedOutState checkedOutState = (CheckedOutState) state;
            result = jdbcTemplate.update(
                "UPDATE book_database_entity b SET b.book_state = ?, b.checked_out_at_branch = ?, b.checked_out_by_patron = ?, b.version = ? WHERE book_id = ? AND version = ?",
                CheckedOut.toString(),
                checkedOutState.getCheckedOutAt().getLibraryBranchId(),
                checkedOutState.getByPatron().getPatronId(),
                book.getVersion().getVersion() + 1,
                book.getBookId().getBookId(),
                book.getVersion().getVersion());
        } else {
            throw new IllegalStateException("Unknown state: " + state.getClass().getName());
        }
        
        if (result == 0) {
            throw new AggregateRootIsStale("Someone has updated book in the meantime, book: " + book);
        }
        return result;
    }

    private void insertNew(Book book) {
        BookState state = book.getState();
        
        if (state instanceof AvailableState) {
            AvailableState availableState = (AvailableState) state;
            jdbcTemplate.update(
                "INSERT INTO book_database_entity (id, book_id, book_type, book_state, available_at_branch, on_hold_at_branch, on_hold_by_patron, on_hold_till, checked_out_at_branch, checked_out_by_patron, version) VALUES (book_database_entity_seq.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)",
                book.getBookId().getBookId(),
                book.getBookType().toString(),
                Available.toString(),
                availableState.getCurrentBranch().getLibraryBranchId(),
                null, null, null, null, null);
        } else if (state instanceof OnHoldState) {
            OnHoldState onHoldState = (OnHoldState) state;
            jdbcTemplate.update(
                "INSERT INTO book_database_entity (id, book_id, book_type, book_state, available_at_branch, on_hold_at_branch, on_hold_by_patron, on_hold_till, checked_out_at_branch, checked_out_by_patron, version) VALUES (book_database_entity_seq.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)",
                book.getBookId().getBookId(),
                book.getBookType().toString(),
                OnHold.toString(),
                null,
                onHoldState.getHoldPlacedAt().getLibraryBranchId(),
                onHoldState.getByPatron().getPatronId(),
                onHoldState.getHoldTill(),
                null, null);
        } else if (state instanceof CheckedOutState) {
            CheckedOutState checkedOutState = (CheckedOutState) state;
            jdbcTemplate.update(
                "INSERT INTO book_database_entity (id, book_id, book_type, book_state, available_at_branch, on_hold_at_branch, on_hold_by_patron, on_hold_till, checked_out_at_branch, checked_out_by_patron, version) VALUES (book_database_entity_seq.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)",
                book.getBookId().getBookId(),
                book.getBookType().toString(),
                CheckedOut.toString(),
                null, null, null, null,
                checkedOutState.getCheckedOutAt().getLibraryBranchId(),
                checkedOutState.getByPatron().getPatronId());
        }
    }
}
