package io.pillopl.library.lending.patron.infrastructure;

import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.UUID;

class HoldDatabaseEntity {

    @Id
    Long id;
    UUID patronId;
    UUID bookId;
    UUID libraryBranchId;
    Instant till;

    HoldDatabaseEntity() {
    }

    public Long getId() {
        return id;
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

    public Instant getTill() {
        return till;
    }

    HoldDatabaseEntity(UUID bookId, UUID patronId, UUID libraryBranchId, Instant till) {
        this.bookId = bookId;
        this.patronId = patronId;
        this.libraryBranchId = libraryBranchId;
        this.till = till;
    }

    boolean is(UUID patronId, UUID bookId, UUID libraryBranchId) {
        return  this.patronId.equals(patronId) &&
                this.bookId.equals(bookId) &&
                this.libraryBranchId.equals(libraryBranchId);
    }

}
