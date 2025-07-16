package io.pillopl.library.lending.patron.infrastructure;

import org.springframework.data.annotation.Id;

import java.util.UUID;

class OverdueCheckoutDatabaseEntity {

    @Id
    Long id;
    UUID patronId;
    UUID bookId;
    UUID libraryBranchId;

    OverdueCheckoutDatabaseEntity(UUID bookId, UUID patronId, UUID libraryBranchId) {
        this.bookId = bookId;
        this.patronId = patronId;
        this.libraryBranchId = libraryBranchId;
    }

    boolean is(UUID patronId, UUID bookId, UUID libraryBranchId) {
        return  this.patronId.equals(patronId) &&
                this.bookId.equals(bookId) &&
                this.libraryBranchId.equals(libraryBranchId);
    }

    public UUID getPatronId() {
        return patronId;
    }

    public UUID getBookId() {
        return bookId;
    }

    public UUID getLibraryBranchId() {
        return libraryBranchId;
    }
}
