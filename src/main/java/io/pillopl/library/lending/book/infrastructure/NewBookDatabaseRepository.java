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
        int result = updateBook(book);
        if (result == 0) {
            throw new AggregateRootIsStale("Someone has updated book in the meantime, book: " + book);
        }
        return result;
    }

    private int updateBook(Book book) {
        BookState state = book.getState();
        String stateName = state.getStateName();
        
        if ("AVAILABLE".equals(stateName)) {
            return updateAvailableBook(book, (AvailableState) state);
        } else if ("ON_HOLD".equals(stateName)) {
            return updateOnHoldBook(book, (OnHoldState) state);
        } else if ("CHECKED_OUT".equals(stateName)) {
            return updateCheckedOutBook(book, (CheckedOutState) state);
        }
        
        throw new IllegalStateException("Unknown book state: " + stateName);
    }

    private int updateAvailableBook(Book book, AvailableState state) {
        return jdbcTemplate.update(
            "UPDATE book_database_entity b SET b.book_state = ?, b.available_at_branch = ?, b.version = ? WHERE book_id = ? AND version = ?",
            Available.toString(),
            state.getCurrentBranch().getLibraryBranchId(),
            book.getVersion().getVersion() + 1,
            book.getBookId().getBookId(),
            book.getVersion().getVersion());
    }

    private int updateOnHoldBook(Book book, OnHoldState state) {
        return jdbcTemplate.update(
            "UPDATE book_database_entity b SET b.book_state = ?, b.on_hold_at_branch = ?, b.on_hold_by_patron = ?, b.on_hold_till = ?, b.version = ? WHERE book_id = ? AND version = ?",
            OnHold.toString(),
            state.getHoldPlacedAt().getLibraryBranchId(),
            state.getByPatron().getPatronId(),
            state.getHoldTill(),
            book.getVersion().getVersion() + 1,
            book.getBookId().getBookId(),
            book.getVersion().getVersion());
    }

    private int updateCheckedOutBook(Book book, CheckedOutState state) {
        return jdbcTemplate.update(
            "UPDATE book_database_entity b SET b.book_state = ?, b.checked_out_at_branch = ?, b.checked_out_by_patron = ?, b.version = ? WHERE book_id = ? AND version = ?",
            CheckedOut.toString(),
            state.getCheckedOutAt().getLibraryBranchId(),
            state.getByPatron().getPatronId(),
            book.getVersion().getVersion() + 1,
            book.getBookId().getBookId(),
            book.getVersion().getVersion());
    }

    private void insertNew(Book book) {
        BookState state = book.getState();
        String stateName = state.getStateName();
        
        if ("AVAILABLE".equals(stateName)) {
            insertAvailableBook(book, (AvailableState) state);
        } else if ("ON_HOLD".equals(stateName)) {
            insertOnHoldBook(book, (OnHoldState) state);
        } else if ("CHECKED_OUT".equals(stateName)) {
            insertCheckedOutBook(book, (CheckedOutState) state);
        } else {
            throw new IllegalStateException("Unknown book state: " + stateName);
        }
    }

    private int insertAvailableBook(Book book, AvailableState state) {
        return insert(book.getBookId(), book.getBookType(), Available, 
            state.getCurrentBranch().getLibraryBranchId(), null, null, null, null, null);
    }

    private int insertOnHoldBook(Book book, OnHoldState state) {
        return insert(book.getBookId(), book.getBookType(), OnHold, 
            null, state.getHoldPlacedAt().getLibraryBranchId(), 
            state.getByPatron().getPatronId(), state.getHoldTill(), null, null);
    }

    private int insertCheckedOutBook(Book book, CheckedOutState state) {
        return insert(book.getBookId(), book.getBookType(), CheckedOut, 
            null, null, null, null, 
            state.getCheckedOutAt().getLibraryBranchId(), 
            state.getByPatron().getPatronId());
    }

    private int insert(BookId bookId, BookType bookType, BookDatabaseEntity.BookState state, 
                      UUID availableAt, UUID onHoldAt, UUID onHoldBy, Instant onHoldTill, 
                      UUID checkedOutAt, UUID checkedOutBy) {
        return jdbcTemplate.update("INSERT INTO book_database_entity " +
                        "(id, " +
                        "book_id, " +
                        "book_type, " +
                        "book_state, " +
                        "available_at_branch," +
                        "on_hold_at_branch, " +
                        "on_hold_by_patron, " +
                        "on_hold_till, " +
                        "checked_out_at_branch, " +
                        "checked_out_by_patron, " +
                        "version) VALUES " +
                        "(book_database_entity_seq.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)",
                bookId.getBookId(), bookType.toString(), state.toString(), 
                availableAt, onHoldAt, onHoldBy, onHoldTill, checkedOutAt, checkedOutBy);
    }

    private Book toDomainModel(BookDatabaseEntity entity) {
        BookId bookId = new BookId(entity.book_id);
        BookType bookType = entity.book_type;
        Version version = new Version(entity.version);
        
        return Match(entity.book_state).of(
                Case($(Available), () -> createAvailableBook(bookId, bookType, entity, version)),
                Case($(OnHold), () -> createOnHoldBook(bookId, bookType, entity, version)),
                Case($(CheckedOut), () -> createCheckedOutBook(bookId, bookType, entity, version))
        );
    }

    private Book createAvailableBook(BookId bookId, BookType bookType, BookDatabaseEntity entity, Version version) {
        LibraryBranchId branch = new LibraryBranchId(entity.available_at_branch);
        Book book = new Book(bookId, bookType, branch, version);
        return book;
    }

    private Book createOnHoldBook(BookId bookId, BookType bookType, BookDatabaseEntity entity, Version version) {
        LibraryBranchId branch = new LibraryBranchId(entity.on_hold_at_branch);
        Book book = new Book(bookId, bookType, branch, version);
        
        PatronId patronId = new PatronId(entity.on_hold_by_patron);
        book.placeOnHold(patronId, branch, entity.on_hold_till);
        
        return book;
    }

    private Book createCheckedOutBook(BookId bookId, BookType bookType, BookDatabaseEntity entity, Version version) {
        LibraryBranchId branch = new LibraryBranchId(entity.checked_out_at_branch);
        Book book = new Book(bookId, bookType, branch, version);
        
        PatronId patronId = new PatronId(entity.checked_out_by_patron);
        book.checkout(patronId, branch);
        
        return book;
    }
}
