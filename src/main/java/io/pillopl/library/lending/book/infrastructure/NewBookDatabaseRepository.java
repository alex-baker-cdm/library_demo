package io.pillopl.library.lending.book.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.AggregateRootIsStale;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.book.new_model.*;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.UUID;

import static io.pillopl.library.lending.book.infrastructure.BookDatabaseEntity.BookState.*;
import static io.vavr.API.*;
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
                .map(this::toDomainModel);
    }

    private Option<BookDatabaseEntity> findBookById(BookId bookId) {
        return Try
                .ofSupplier(() -> of(jdbcTemplate.queryForObject(
                        "SELECT b.* FROM book_database_entity b WHERE b.book_id = ?",
                        new BeanPropertyRowMapper<>(BookDatabaseEntity.class),
                        bookId.getBookId())))
                .getOrElse(none());
    }

    private Book toDomainModel(BookDatabaseEntity entity) {
        UUID branchId = Match(entity.book_state).of(
                Case($(Available), () -> entity.available_at_branch),
                Case($(OnHold), () -> entity.on_hold_at_branch),
                Case($(CheckedOut), () -> entity.checked_out_at_branch)
        );

        Book book = new Book(
                new BookId(entity.book_id),
                entity.book_type,
                new LibraryBranchId(branchId),
                new Version(entity.version)
        );

        if (entity.book_state == OnHold) {
            OnHoldState onHoldState = new OnHoldState(
                    book,
                    new LibraryBranchId(entity.on_hold_at_branch),
                    new PatronId(entity.on_hold_by_patron),
                    entity.on_hold_till
            );
            book.setState(onHoldState);
        } else if (entity.book_state == CheckedOut) {
            CheckedOutState checkedOutState = new CheckedOutState(
                    book,
                    new LibraryBranchId(entity.checked_out_at_branch),
                    new PatronId(entity.checked_out_by_patron)
            );
            book.setState(checkedOutState);
        }

        return book;
    }

    @Override
    public void save(Book book) {
        findBy(book.getBookId())
                .map(entity -> updateOptimistically(book))
                .onEmpty(() -> insertNew(book));
    }

    private int updateOptimistically(Book book) {
        BookState state = book.getState();
        String stateName = state.getStateName();

        int result;
        if ("AVAILABLE".equals(stateName)) {
            result = updateAvailable(book, (AvailableState) state);
        } else if ("ON_HOLD".equals(stateName)) {
            result = updateOnHold(book, (OnHoldState) state);
        } else if ("CHECKED_OUT".equals(stateName)) {
            result = updateCheckedOut(book, (CheckedOutState) state);
        } else {
            throw new IllegalStateException("Unknown state: " + stateName);
        }

        if (result == 0) {
            throw new AggregateRootIsStale("Someone has updated book in the meantime, book: " + book.getBookId());
        }
        return result;
    }

    private int updateAvailable(Book book, AvailableState state) {
        return jdbcTemplate.update(
                "UPDATE book_database_entity SET book_state = ?, available_at_branch = ?, version = ? WHERE book_id = ? AND version = ?",
                Available.toString(),
                state.getCurrentBranch().getLibraryBranchId(),
                book.getVersion().getVersion() + 1,
                book.getBookId().getBookId(),
                book.getVersion().getVersion()
        );
    }

    private int updateOnHold(Book book, OnHoldState state) {
        return jdbcTemplate.update(
                "UPDATE book_database_entity SET book_state = ?, on_hold_at_branch = ?, on_hold_by_patron = ?, on_hold_till = ?, version = ? WHERE book_id = ? AND version = ?",
                OnHold.toString(),
                state.getHoldPlacedAt().getLibraryBranchId(),
                state.getByPatron().getPatronId(),
                state.getHoldTill(),
                book.getVersion().getVersion() + 1,
                book.getBookId().getBookId(),
                book.getVersion().getVersion()
        );
    }

    private int updateCheckedOut(Book book, CheckedOutState state) {
        return jdbcTemplate.update(
                "UPDATE book_database_entity SET book_state = ?, checked_out_at_branch = ?, checked_out_by_patron = ?, version = ? WHERE book_id = ? AND version = ?",
                CheckedOut.toString(),
                state.getCheckedOutAt().getLibraryBranchId(),
                state.getByPatron().getPatronId(),
                book.getVersion().getVersion() + 1,
                book.getBookId().getBookId(),
                book.getVersion().getVersion()
        );
    }

    private void insertNew(Book book) {
        BookState state = book.getState();
        String stateName = state.getStateName();

        if ("AVAILABLE".equals(stateName)) {
            insertAvailable(book, (AvailableState) state);
        } else if ("ON_HOLD".equals(stateName)) {
            insertOnHold(book, (OnHoldState) state);
        } else if ("CHECKED_OUT".equals(stateName)) {
            insertCheckedOut(book, (CheckedOutState) state);
        } else {
            throw new IllegalStateException("Unknown state: " + stateName);
        }
    }

    private void insertAvailable(Book book, AvailableState state) {
        jdbcTemplate.update(
                "INSERT INTO book_database_entity (id, book_id, book_type, book_state, available_at_branch, on_hold_at_branch, on_hold_by_patron, on_hold_till, checked_out_at_branch, checked_out_by_patron, version) " +
                        "VALUES (book_database_entity_seq.nextval, ?, ?, ?, ?, NULL, NULL, NULL, NULL, NULL, 0)",
                book.getBookId().getBookId(),
                book.getBookType().toString(),
                Available.toString(),
                state.getCurrentBranch().getLibraryBranchId()
        );
    }

    private void insertOnHold(Book book, OnHoldState state) {
        jdbcTemplate.update(
                "INSERT INTO book_database_entity (id, book_id, book_type, book_state, available_at_branch, on_hold_at_branch, on_hold_by_patron, on_hold_till, checked_out_at_branch, checked_out_by_patron, version) " +
                        "VALUES (book_database_entity_seq.nextval, ?, ?, ?, NULL, ?, ?, ?, NULL, NULL, 0)",
                book.getBookId().getBookId(),
                book.getBookType().toString(),
                OnHold.toString(),
                state.getHoldPlacedAt().getLibraryBranchId(),
                state.getByPatron().getPatronId(),
                state.getHoldTill()
        );
    }

    private void insertCheckedOut(Book book, CheckedOutState state) {
        jdbcTemplate.update(
                "INSERT INTO book_database_entity (id, book_id, book_type, book_state, available_at_branch, on_hold_at_branch, on_hold_by_patron, on_hold_till, checked_out_at_branch, checked_out_by_patron, version) " +
                        "VALUES (book_database_entity_seq.nextval, ?, ?, ?, NULL, NULL, NULL, NULL, ?, ?, 0)",
                book.getBookId().getBookId(),
                book.getBookType().toString(),
                CheckedOut.toString(),
                state.getCheckedOutAt().getLibraryBranchId(),
                state.getByPatron().getPatronId()
        );
    }
}
