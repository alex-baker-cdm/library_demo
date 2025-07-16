package io.pillopl.library.lending.patron.application.checkout;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.CheckoutDuration;
import io.pillopl.library.lending.patron.model.PatronId;
import java.time.Instant;
import java.util.Objects;

public class CheckOutBookCommand {
    private final Instant timestamp;
    private final PatronId patronId;
    private final LibraryBranchId libraryId;
    private final BookId bookId;
    private final Integer noOfDays;

    public CheckOutBookCommand(Instant timestamp, PatronId patronId, LibraryBranchId libraryId, BookId bookId, Integer noOfDays) {
        this.timestamp = Objects.requireNonNull(timestamp);
        this.patronId = Objects.requireNonNull(patronId);
        this.libraryId = Objects.requireNonNull(libraryId);
        this.bookId = Objects.requireNonNull(bookId);
        this.noOfDays = Objects.requireNonNull(noOfDays);
    }

    public static CheckOutBookCommand create(PatronId patronId, LibraryBranchId libraryBranchId, BookId bookId, int noOfDays) {
        return new CheckOutBookCommand(Instant.now(), patronId, libraryBranchId, bookId, noOfDays);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public PatronId getPatronId() {
        return patronId;
    }

    public LibraryBranchId getLibraryId() {
        return libraryId;
    }

    public BookId getBookId() {
        return bookId;
    }

    public Integer getNoOfDays() {
        return noOfDays;
    }

    CheckoutDuration getCheckoutDuration() {
        return CheckoutDuration.forNoOfDays(noOfDays);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckOutBookCommand that = (CheckOutBookCommand) o;
        return Objects.equals(timestamp, that.timestamp) && Objects.equals(patronId, that.patronId) && Objects.equals(libraryId, that.libraryId) && Objects.equals(bookId, that.bookId) && Objects.equals(noOfDays, that.noOfDays);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, patronId, libraryId, bookId, noOfDays);
    }

    @Override
    public String toString() {
        return "CheckOutBookCommand{" +
                "timestamp=" + timestamp +
                ", patronId=" + patronId +
                ", libraryId=" + libraryId +
                ", bookId=" + bookId +
                ", noOfDays=" + noOfDays +
                '}';
    }
}
