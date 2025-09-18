package io.pillopl.library.lending.book.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.AggregateRootIsStale;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.book.new_model.Book;
import io.pillopl.library.lending.book.new_model.BookRepository;
import io.pillopl.library.lending.book.new_model.BookState;
import io.pillopl.library.lending.book.new_model.FindAvailableBookNewModel;
import io.pillopl.library.lending.book.new_model.FindBookOnHoldNewModel;
import io.pillopl.library.lending.book.new_model.OnHoldState;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.UUID;

import static io.pillopl.library.lending.book.infrastructure.BookDatabaseEntity.BookState.*;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.of;

public class NewBookDatabaseRepository implements BookRepository, FindAvailableBookNewModel, FindBookOnHoldNewModel {

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
                .ofSupplier(() -> of(jdbcTemplate.queryForObject("SELECT * FROM book_database_entity WHERE book_id = ?", 
                        (rs, rowNum) -> {
                            BookDatabaseEntity entity = new BookDatabaseEntity();
                            entity.book_id = (UUID) rs.getObject("book_id");
                            entity.book_type = BookType.valueOf(rs.getString("book_type"));
                            entity.book_state = BookDatabaseEntity.BookState.valueOf(rs.getString("book_state"));
                            entity.available_at_branch = (UUID) rs.getObject("available_at_branch");
                            entity.on_hold_at_branch = (UUID) rs.getObject("on_hold_at_branch");
                            entity.on_hold_by_patron = (UUID) rs.getObject("on_hold_by_patron");
                            entity.on_hold_till = rs.getTimestamp("on_hold_till") != null ? rs.getTimestamp("on_hold_till").toInstant() : null;
                            entity.checked_out_at_branch = (UUID) rs.getObject("checked_out_at_branch");
                            entity.checked_out_by_patron = (UUID) rs.getObject("checked_out_by_patron");
                            entity.version = rs.getInt("version");
                            return entity;
                        }, bookId.getBookId())))
                .getOrElse(none());
    }

    @Override
    public void save(Book book) {
        findBookById(book.getBookId())
                .map(entity -> updateOptimistically(book))
                .onEmpty(() -> insertNew(book));
    }

    private int updateOptimistically(Book book) {
        BookDatabaseEntity entity = fromDomainModel(book);
        int result = jdbcTemplate.update(
                "UPDATE book_database_entity b SET " +
                "b.book_state = ?, " +
                "b.available_at_branch = ?, " +
                "b.on_hold_at_branch = ?, " +
                "b.on_hold_by_patron = ?, " +
                "b.on_hold_till = ?, " +
                "b.checked_out_at_branch = ?, " +
                "b.checked_out_by_patron = ?, " +
                "b.version = ? " +
                "WHERE book_id = ? AND version = ?",
                entity.book_state.toString(),
                entity.available_at_branch,
                entity.on_hold_at_branch,
                entity.on_hold_by_patron,
                entity.on_hold_till,
                entity.checked_out_at_branch,
                entity.checked_out_by_patron,
                entity.version,
                entity.book_id,
                book.getVersion().getVersion());
        
        if (result == 0) {
            throw new AggregateRootIsStale("Someone has updated book in the meantime, book: " + book);
        }
        return result;
    }

    private void insertNew(Book book) {
        BookDatabaseEntity entity = fromDomainModel(book);
        jdbcTemplate.update("INSERT INTO book_database_entity " +
                "(book_id, " +
                "book_type, " +
                "book_state, " +
                "available_at_branch," +
                "on_hold_at_branch, " +
                "on_hold_by_patron, " +
                "on_hold_till, " +
                "checked_out_at_branch, " +
                "checked_out_by_patron, " +
                "version) VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                entity.book_id, 
                entity.book_type.toString(), 
                entity.book_state.toString(), 
                entity.available_at_branch, 
                entity.on_hold_at_branch, 
                entity.on_hold_by_patron, 
                entity.on_hold_till, 
                entity.checked_out_at_branch, 
                entity.checked_out_by_patron, 
                entity.version);
    }

    private BookDatabaseEntity fromDomainModel(Book book) {
        BookDatabaseEntity entity = new BookDatabaseEntity();
        entity.book_id = book.getBookId().getBookId();
        entity.book_type = book.getBookType();
        entity.version = book.getVersion().getVersion() + 1;
        
        String stateName = book.getCurrentState();
        LibraryBranchId currentBranch = book.getCurrentBranch();
        PatronId currentPatron = book.getCurrentPatron();
        
        if ("AVAILABLE".equals(stateName)) {
            entity.book_state = Available;
            entity.available_at_branch = currentBranch != null ? currentBranch.getLibraryBranchId() : null;
            entity.on_hold_at_branch = null;
            entity.on_hold_by_patron = null;
            entity.on_hold_till = null;
            entity.checked_out_at_branch = null;
            entity.checked_out_by_patron = null;
        } else if ("ON_HOLD".equals(stateName)) {
            entity.book_state = OnHold;
            entity.available_at_branch = null;
            entity.on_hold_at_branch = currentBranch != null ? currentBranch.getLibraryBranchId() : null;
            entity.on_hold_by_patron = currentPatron != null ? currentPatron.getPatronId() : null;
            entity.on_hold_till = getHoldTillFromState(book.getState());
            entity.checked_out_at_branch = null;
            entity.checked_out_by_patron = null;
        } else if ("CHECKED_OUT".equals(stateName)) {
            entity.book_state = CheckedOut;
            entity.available_at_branch = null;
            entity.on_hold_at_branch = null;
            entity.on_hold_by_patron = null;
            entity.on_hold_till = null;
            entity.checked_out_at_branch = currentBranch != null ? currentBranch.getLibraryBranchId() : null;
            entity.checked_out_by_patron = currentPatron != null ? currentPatron.getPatronId() : null;
        }
        
        return entity;
    }

    private Instant getHoldTillFromState(BookState state) {
        if (state instanceof OnHoldState) {
            return ((OnHoldState) state).getHoldTill();
        }
        return null;
    }

    private Book toDomainModel(BookDatabaseEntity entity) {
        LibraryBranchId initialBranch = getInitialBranch(entity);
        
        Book book = new Book(
            new BookId(entity.book_id),
            entity.book_type,
            initialBranch,
            new Version(entity.version)
        );
        
        if (entity.book_state == OnHold) {
            book.placeOnHold(
                new PatronId(entity.on_hold_by_patron),
                new LibraryBranchId(entity.on_hold_at_branch),
                entity.on_hold_till
            );
        } else if (entity.book_state == CheckedOut) {
            book.checkout(
                new PatronId(entity.checked_out_by_patron),
                new LibraryBranchId(entity.checked_out_at_branch)
            );
        }
        
        return book;
    }

    private LibraryBranchId getInitialBranch(BookDatabaseEntity entity) {
        if (entity.available_at_branch != null) {
            return new LibraryBranchId(entity.available_at_branch);
        } else if (entity.on_hold_at_branch != null) {
            return new LibraryBranchId(entity.on_hold_at_branch);
        } else if (entity.checked_out_at_branch != null) {
            return new LibraryBranchId(entity.checked_out_at_branch);
        }
        throw new IllegalStateException("Book entity has no branch information");
    }

    @Override
    public Option<Book> findAvailableBookBy(BookId bookId) {
        return findBy(bookId)
                .filter(book -> "AVAILABLE".equals(book.getCurrentState()));
    }

    @Override
    public Option<Book> findBookOnHold(BookId bookId, PatronId patronId) {
        return findBy(bookId)
                .filter(book -> "ON_HOLD".equals(book.getCurrentState()))
                .filter(book -> patronId.equals(book.getCurrentPatron()));
    }
}
