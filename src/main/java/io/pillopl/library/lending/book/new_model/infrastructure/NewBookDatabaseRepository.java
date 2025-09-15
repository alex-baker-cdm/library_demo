package io.pillopl.library.lending.book.new_model.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.AggregateRootIsStale;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.book.new_model.*;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.application.hold.FindAvailableBook;
import io.pillopl.library.lending.patron.application.hold.FindBookOnHold;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.UUID;

import static io.vavr.control.Option.none;
import static io.vavr.control.Option.of;

public class NewBookDatabaseRepository implements BookRepository, FindAvailableBook, FindBookOnHold {

    private final JdbcTemplate jdbcTemplate;

    public NewBookDatabaseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Option<Book> findBy(BookId bookId) {
        return findBookEntityById(bookId)
                .map(this::toDomainModel);
    }

    private Option<BookDatabaseEntity> findBookEntityById(BookId bookId) {
        return Try
                .ofSupplier(() -> of(jdbcTemplate.queryForObject(
                        "SELECT b.* FROM book_database_entity b WHERE b.book_id = ?", 
                        new BeanPropertyRowMapper<>(BookDatabaseEntity.class), 
                        bookId.getBookId())))
                .getOrElse(none());
    }

    @Override
    public void save(Book book) {
        findBookEntityById(book.getBookId())
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
            throw new AggregateRootIsStale("Someone has updated book in the meantime, book: " + book);
        }
        return result;
    }

    private int updateAvailable(Book book, AvailableState state) {
        return jdbcTemplate.update(
                "UPDATE book_database_entity b SET b.book_state = ?, b.available_at_branch = ?, b.version = ? WHERE book_id = ? AND version = ?",
                "Available",
                state.getCurrentBranch().getLibraryBranchId(),
                book.getVersion().getVersion() + 1,
                book.getBookId().getBookId(),
                book.getVersion().getVersion());
    }

    private int updateOnHold(Book book, OnHoldState state) {
        return jdbcTemplate.update(
                "UPDATE book_database_entity b SET b.book_state = ?, b.on_hold_at_branch = ?, b.on_hold_by_patron = ?, b.on_hold_till = ?, b.version = ? WHERE book_id = ? AND version = ?",
                "OnHold",
                state.getCurrentBranch().getLibraryBranchId(),
                state.getCurrentPatron().getPatronId(),
                state.getHoldTill(),
                book.getVersion().getVersion() + 1,
                book.getBookId().getBookId(),
                book.getVersion().getVersion());
    }

    private int updateCheckedOut(Book book, CheckedOutState state) {
        return jdbcTemplate.update(
                "UPDATE book_database_entity b SET b.book_state = ?, b.checked_out_at_branch = ?, b.checked_out_by_patron = ?, b.version = ? WHERE book_id = ? AND version = ?",
                "CheckedOut",
                state.getCurrentBranch().getLibraryBranchId(),
                state.getCurrentPatron().getPatronId(),
                book.getVersion().getVersion() + 1,
                book.getBookId().getBookId(),
                book.getVersion().getVersion());
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
        insertBook(book.getBookId(), book.getBookType(), "Available", 
                state.getCurrentBranch().getLibraryBranchId(), null, null, null, null, null);
    }

    private void insertOnHold(Book book, OnHoldState state) {
        insertBook(book.getBookId(), book.getBookType(), "OnHold", 
                null, state.getCurrentBranch().getLibraryBranchId(), 
                state.getCurrentPatron().getPatronId(), state.getHoldTill(), null, null);
    }

    private void insertCheckedOut(Book book, CheckedOutState state) {
        insertBook(book.getBookId(), book.getBookType(), "CheckedOut", 
                null, null, null, null, 
                state.getCurrentBranch().getLibraryBranchId(), 
                state.getCurrentPatron().getPatronId());
    }

    private void insertBook(BookId bookId, BookType bookType, String state, 
                           UUID availableAt, UUID onHoldAt, UUID onHoldBy, Instant onHoldTill, 
                           UUID checkedOutAt, UUID checkedOutBy) {
        jdbcTemplate.update("INSERT INTO book_database_entity " +
                        "(id, book_id, book_type, book_state, available_at_branch, " +
                        "on_hold_at_branch, on_hold_by_patron, on_hold_till, " +
                        "checked_out_at_branch, checked_out_by_patron, version) VALUES " +
                        "(book_database_entity_seq.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)",
                bookId.getBookId(), bookType.toString(), state, availableAt, 
                onHoldAt, onHoldBy, onHoldTill, checkedOutAt, checkedOutBy);
    }

    private Book toDomainModel(BookDatabaseEntity entity) {
        BookId bookId = new BookId(entity.book_id);
        BookType bookType = entity.book_type;
        Version version = new Version(entity.version);
        
        if ("Available".equals(entity.book_state.toString())) {
            return new Book(bookId, bookType, new LibraryBranchId(entity.available_at_branch), version);
        } else if ("OnHold".equals(entity.book_state.toString())) {
            Book book = new Book(bookId, bookType, new LibraryBranchId(entity.on_hold_at_branch), version);
            book.placeOnHold(new PatronId(entity.on_hold_by_patron), 
                            new LibraryBranchId(entity.on_hold_at_branch), 
                            entity.on_hold_till);
            return book;
        } else if ("CheckedOut".equals(entity.book_state.toString())) {
            Book book = new Book(bookId, bookType, new LibraryBranchId(entity.checked_out_at_branch), version);
            book.placeOnHold(new PatronId(entity.checked_out_by_patron), 
                            new LibraryBranchId(entity.checked_out_at_branch), 
                            Instant.now().plusSeconds(3600));
            book.checkout(new PatronId(entity.checked_out_by_patron), 
                         new LibraryBranchId(entity.checked_out_at_branch));
            return book;
        } else {
            throw new IllegalStateException("Unknown book state: " + entity.book_state);
        }
    }

    @Override
    public Option<io.pillopl.library.lending.book.model.AvailableBook> findAvailableBookBy(BookId bookId) {
        return findBy(bookId)
                .filter(book -> "AVAILABLE".equals(book.getCurrentState()))
                .map(this::convertToLegacyAvailableBook);
    }

    @Override
    public Option<io.pillopl.library.lending.book.model.BookOnHold> findBookOnHold(BookId bookId, PatronId patronId) {
        return findBy(bookId)
                .filter(book -> "ON_HOLD".equals(book.getCurrentState()) && 
                               patronId.equals(book.getCurrentPatron()))
                .map(this::convertToLegacyBookOnHold);
    }

    private io.pillopl.library.lending.book.model.AvailableBook convertToLegacyAvailableBook(Book book) {
        AvailableState state = (AvailableState) book.getState();
        return new io.pillopl.library.lending.book.model.AvailableBook(
                book.getBookId(), 
                book.getBookType(), 
                state.getCurrentBranch(), 
                book.getVersion());
    }

    private io.pillopl.library.lending.book.model.BookOnHold convertToLegacyBookOnHold(Book book) {
        OnHoldState state = (OnHoldState) book.getState();
        return new io.pillopl.library.lending.book.model.BookOnHold(
                book.getBookId(), 
                book.getBookType(), 
                state.getCurrentBranch(), 
                state.getCurrentPatron(), 
                state.getHoldTill(), 
                book.getVersion());
    }
}
