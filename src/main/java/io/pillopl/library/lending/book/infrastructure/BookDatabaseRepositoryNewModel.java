package io.pillopl.library.lending.book.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.book.new_model.Book;
import io.pillopl.library.lending.book.new_model.BookRepository;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Option;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import static io.pillopl.library.lending.book.infrastructure.BookDatabaseEntity.BookState.*;
import static io.vavr.API.*;

public class BookDatabaseRepositoryNewModel implements BookRepository {

    private final JdbcTemplate jdbcTemplate;

    public BookDatabaseRepositoryNewModel(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Option<Book> findBy(BookId bookId) {
        return Option.ofOptional(
                jdbcTemplate.query(
                        "SELECT * FROM book_database_entity WHERE book_id = ?",
                        this::mapToBookDatabaseEntity,
                        bookId.getBookId()
                ).stream().findFirst()
        ).map(BookDatabaseEntity::toNewModelBook);
    }

    @Override
    public void save(Book book) {
        BookDatabaseEntity entity = fromNewModelBook(book);
        
        int updated = jdbcTemplate.update(
                "UPDATE book_database_entity SET book_type = ?, book_state = ?, available_at_branch = ?, " +
                "on_hold_at_branch = ?, on_hold_by_patron = ?, on_hold_till = ?, " +
                "checked_out_at_branch = ?, checked_out_by_patron = ?, version = ? WHERE book_id = ?",
                entity.book_type.toString(),
                entity.book_state.toString(),
                entity.available_at_branch,
                entity.on_hold_at_branch,
                entity.on_hold_by_patron,
                entity.on_hold_till,
                entity.checked_out_at_branch,
                entity.checked_out_by_patron,
                entity.version,
                entity.book_id
        );
        
        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO book_database_entity (book_id, book_type, book_state, available_at_branch, " +
                    "on_hold_at_branch, on_hold_by_patron, on_hold_till, checked_out_at_branch, checked_out_by_patron, version) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    entity.book_id,
                    entity.book_type.toString(),
                    entity.book_state.toString(),
                    entity.available_at_branch,
                    entity.on_hold_at_branch,
                    entity.on_hold_by_patron,
                    entity.on_hold_till,
                    entity.checked_out_at_branch,
                    entity.checked_out_by_patron,
                    entity.version
            );
        }
    }

    private BookDatabaseEntity mapToBookDatabaseEntity(ResultSet rs, int rowNum) throws SQLException {
        BookDatabaseEntity entity = new BookDatabaseEntity();
        entity.book_id = UUID.fromString(rs.getString("book_id"));
        entity.book_type = BookType.valueOf(rs.getString("book_type"));
        entity.book_state = BookDatabaseEntity.BookState.valueOf(rs.getString("book_state"));
        entity.available_at_branch = rs.getString("available_at_branch") != null ? UUID.fromString(rs.getString("available_at_branch")) : null;
        entity.on_hold_at_branch = rs.getString("on_hold_at_branch") != null ? UUID.fromString(rs.getString("on_hold_at_branch")) : null;
        entity.on_hold_by_patron = rs.getString("on_hold_by_patron") != null ? UUID.fromString(rs.getString("on_hold_by_patron")) : null;
        entity.on_hold_till = rs.getTimestamp("on_hold_till") != null ? rs.getTimestamp("on_hold_till").toInstant() : null;
        entity.checked_out_at_branch = rs.getString("checked_out_at_branch") != null ? UUID.fromString(rs.getString("checked_out_at_branch")) : null;
        entity.checked_out_by_patron = rs.getString("checked_out_by_patron") != null ? UUID.fromString(rs.getString("checked_out_by_patron")) : null;
        entity.version = rs.getInt("version");
        return entity;
    }

    private BookDatabaseEntity fromNewModelBook(Book book) {
        BookDatabaseEntity entity = new BookDatabaseEntity();
        entity.book_id = book.getBookId().getBookId();
        entity.book_type = book.getBookType();
        entity.version = book.getVersion().getVersion();
        
        String currentState = book.getCurrentState();
        if ("AVAILABLE".equals(currentState)) {
            entity.available_at_branch = book.getCurrentBranch().getLibraryBranchId();
            entity.book_state = Available;
            entity.on_hold_at_branch = null;
            entity.on_hold_by_patron = null;
            entity.on_hold_till = null;
            entity.checked_out_at_branch = null;
            entity.checked_out_by_patron = null;
        } else if ("ON_HOLD".equals(currentState)) {
            entity.book_state = OnHold;
            entity.available_at_branch = null;
            entity.on_hold_at_branch = book.getCurrentBranch().getLibraryBranchId();
            entity.on_hold_by_patron = book.getCurrentPatron() != null ? book.getCurrentPatron().getPatronId() : null;
            entity.on_hold_till = null; // Need to get hold till from state
            entity.checked_out_at_branch = null;
            entity.checked_out_by_patron = null;
        } else if ("CHECKED_OUT".equals(currentState)) {
            entity.book_state = CheckedOut;
            entity.available_at_branch = null;
            entity.on_hold_at_branch = null;
            entity.on_hold_by_patron = null;
            entity.on_hold_till = null;
            entity.checked_out_at_branch = book.getCurrentBranch().getLibraryBranchId();
            entity.checked_out_by_patron = book.getCurrentPatron() != null ? book.getCurrentPatron().getPatronId() : null;
        }
        
        return entity;
    }
}
