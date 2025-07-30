package io.pillopl.library.lending.book.new_model.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.book.new_model.*;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronId;

import java.time.Instant;
import java.util.UUID;

public class NewBookDatabaseEntity {
    UUID book_id;
    BookType book_type;
    String book_state;
    UUID available_at_branch;
    UUID on_hold_at_branch;
    UUID on_hold_by_patron;
    Instant on_hold_till;
    UUID checked_out_at_branch;
    UUID checked_out_by_patron;
    int version;

    Book toDomainModel() {
        LibraryBranchId currentBranch = getCurrentBranch();
        Version bookVersion = new Version(version);
        
        Book book = new Book(new BookId(book_id), book_type, currentBranch, bookVersion);
        
        switch (book_state) {
            case "AVAILABLE":
                return book;
            case "ON_HOLD":
                PatronId patron = new PatronId(on_hold_by_patron);
                LibraryBranchId holdBranch = new LibraryBranchId(on_hold_at_branch);
                book.placeOnHold(patron, holdBranch, on_hold_till);
                return book;
            case "CHECKED_OUT":
                PatronId checkedOutPatron = new PatronId(checked_out_by_patron);
                LibraryBranchId checkedOutBranch = new LibraryBranchId(checked_out_at_branch);
                book.checkout(checkedOutPatron, checkedOutBranch);
                return book;
            default:
                throw new IllegalStateException("Unknown book state: " + book_state);
        }
    }
    
    private LibraryBranchId getCurrentBranch() {
        if (available_at_branch != null) return new LibraryBranchId(available_at_branch);
        if (on_hold_at_branch != null) return new LibraryBranchId(on_hold_at_branch);
        if (checked_out_at_branch != null) return new LibraryBranchId(checked_out_at_branch);
        throw new IllegalStateException("No branch found for book");
    }
    
    static NewBookDatabaseEntity fromDomainModel(Book book) {
        NewBookDatabaseEntity entity = new NewBookDatabaseEntity();
        entity.book_id = book.getBookId().getBookId();
        entity.book_type = book.getBookType();
        entity.book_state = book.getCurrentState();
        entity.version = book.getVersion().getVersion();
        
        LibraryBranchId currentBranch = book.getCurrentBranch();
        PatronId currentPatron = book.getCurrentPatron();
        
        switch (book.getCurrentState()) {
            case "AVAILABLE":
                entity.available_at_branch = currentBranch.getLibraryBranchId();
                break;
            case "ON_HOLD":
                entity.on_hold_at_branch = currentBranch.getLibraryBranchId();
                entity.on_hold_by_patron = currentPatron.getPatronId();
                if (book.getState() instanceof OnHoldState) {
                    entity.on_hold_till = ((OnHoldState) book.getState()).getHoldTill();
                }
                break;
            case "CHECKED_OUT":
                entity.checked_out_at_branch = currentBranch.getLibraryBranchId();
                entity.checked_out_by_patron = currentPatron.getPatronId();
                break;
        }
        
        return entity;
    }
}
