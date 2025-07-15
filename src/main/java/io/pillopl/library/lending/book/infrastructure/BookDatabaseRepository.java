package io.pillopl.library.lending.book.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.AggregateRootIsStale;
import io.pillopl.library.lending.book.model.*;
import io.pillopl.library.lending.book.new_model.Book;
import io.pillopl.library.lending.patron.application.hold.FindAvailableBook;
import io.pillopl.library.lending.patron.application.hold.FindBookOnHold;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.UUID;

import static io.pillopl.library.lending.book.infrastructure.BookDatabaseEntity.BookState.*;
import static io.vavr.API.*;
import static io.vavr.Patterns.$Some;
import static io.vavr.Predicates.instanceOf;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.of;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class BookDatabaseRepository implements BookRepository, FindAvailableBook, FindBookOnHold {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Option<io.pillopl.library.lending.book.model.Book> findBy(BookId bookId) {
        return findBookById(bookId)
                .map(BookDatabaseEntity::toDomainModel);
    }

    private Option<BookDatabaseEntity> findBookById(BookId bookId) {
        return Try
                .ofSupplier(() -> of(jdbcTemplate.queryForObject("SELECT b.* FROM book_database_entity b WHERE b.book_id = ?", new BeanPropertyRowMapper<>(BookDatabaseEntity.class), bookId.getBookId())))
                .getOrElse(none());
    }

    @Override
    public void save(io.pillopl.library.lending.book.model.Book book) {
        findBy(book.bookId())
                .map(entity -> updateOptimistically((Book) book))
                .onEmpty(() -> insertNew((Book) book));
    }

    private int updateOptimistically(Book book) {
        int result = update(book);
        if (result == 0) {
            throw new AggregateRootIsStale("Someone has updated book in the meantime, book: " + book);
        }
        return result;
    }

    private int update(Book book) {
        String stateName = book.getCurrentState();
        if ("AVAILABLE".equals(stateName)) {
            return jdbcTemplate.update("UPDATE book_database_entity b SET b.book_state = ?, b.available_at_branch = ?, b.version = ? WHERE book_id = ? AND version = ?",
                    Available.toString(),
                    book.getCurrentBranch().getLibraryBranchId(),
                    book.getVersion().getVersion() + 1,
                    book.getBookId().getBookId(),
                    book.getVersion().getVersion());
        } else if ("ON_HOLD".equals(stateName)) {
            return jdbcTemplate.update("UPDATE book_database_entity b SET b.book_state = ?, b.on_hold_at_branch = ?, b.on_hold_by_patron = ?, b.version = ? WHERE book_id = ? AND version = ?",
                    OnHold.toString(),
                    book.getCurrentBranch().getLibraryBranchId(),
                    book.getCurrentPatron().getPatronId(),
                    book.getVersion().getVersion() + 1,
                    book.getBookId().getBookId(),
                    book.getVersion().getVersion());
        } else if ("CHECKED_OUT".equals(stateName)) {
            return jdbcTemplate.update("UPDATE book_database_entity b SET b.book_state = ?, b.checked_out_at_branch = ?, b.checked_out_by_patron = ?, b.version = ? WHERE book_id = ? AND version = ?",
                    CheckedOut.toString(),
                    book.getCurrentBranch().getLibraryBranchId(),
                    book.getCurrentPatron().getPatronId(),
                    book.getVersion().getVersion() + 1,
                    book.getBookId().getBookId(),
                    book.getVersion().getVersion());
        }
        return 0;
    }

    private void insertNew(Book book) {
        String stateName = book.getCurrentState();
        if ("AVAILABLE".equals(stateName)) {
            insert(book.getBookId(), book.getBookType(), Available, book.getCurrentBranch().getLibraryBranchId(), null, null, null, null, null);
        } else if ("ON_HOLD".equals(stateName)) {
            insert(book.getBookId(), book.getBookType(), OnHold, null, book.getCurrentBranch().getLibraryBranchId(), book.getCurrentPatron().getPatronId(), null, null, null);
        } else if ("CHECKED_OUT".equals(stateName)) {
            insert(book.getBookId(), book.getBookType(), CheckedOut, null, null, null, null, book.getCurrentBranch().getLibraryBranchId(), book.getCurrentPatron().getPatronId());
        }
    }

    private int insert(BookId bookId, BookType bookType, BookDatabaseEntity.BookState state, UUID availableAt, UUID onHoldAt, UUID onHoldBy, Instant onHoldTill, UUID checkedOutAt, UUID checkedOutBy) {
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
                bookId.getBookId(), bookType.toString(), state.toString(), availableAt, onHoldAt, onHoldBy, onHoldTill, checkedOutAt, checkedOutBy);
    }

    @Override
    public Option<Book> findAvailableBookBy(BookId bookId) {
        return findBy(bookId)
                .filter(book -> book instanceof Book && "AVAILABLE".equals(((Book) book).getCurrentState()))
                .map(book -> (Book) book);
    }

    @Override
    public Option<Book> findBookOnHold(BookId bookId, PatronId patronId) {
        return findBy(bookId)
                .filter(book -> book instanceof Book && "ON_HOLD".equals(((Book) book).getCurrentState()))
                .filter(book -> {
                    Book newBook = (Book) book;
                    return newBook.getCurrentPatron() != null && newBook.getCurrentPatron().equals(patronId);
                })
                .map(book -> (Book) book);
    }

}

