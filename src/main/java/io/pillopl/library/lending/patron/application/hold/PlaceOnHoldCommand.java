package io.pillopl.library.lending.patron.application.hold;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.HoldDuration;
import io.pillopl.library.lending.patron.model.NumberOfDays;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Option;
import java.time.Instant;
import java.util.Objects;

public class PlaceOnHoldCommand {
    private final Instant timestamp;
    private final PatronId patronId;
    private final LibraryBranchId libraryId;
    private final BookId bookId;
    private final Option<Integer> noOfDays;

    public PlaceOnHoldCommand(Instant timestamp, PatronId patronId, LibraryBranchId libraryId, BookId bookId, Option<Integer> noOfDays) {
        this.timestamp = Objects.requireNonNull(timestamp);
        this.patronId = Objects.requireNonNull(patronId);
        this.libraryId = Objects.requireNonNull(libraryId);
        this.bookId = Objects.requireNonNull(bookId);
        this.noOfDays = noOfDays;
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

    public Option<Integer> getNoOfDays() {
        return noOfDays;
    }

    static PlaceOnHoldCommand closeEnded(PatronId patronId, LibraryBranchId libraryBranchId, BookId bookId, int forDays) {
        return new PlaceOnHoldCommand(Instant.now(), patronId, libraryBranchId, bookId, Option.of(forDays));
    }

    static PlaceOnHoldCommand openEnded(PatronId patronId, LibraryBranchId libraryBranchId, BookId bookId) {
        return new PlaceOnHoldCommand(Instant.now(), patronId, libraryBranchId, bookId, Option.none());
    }

    HoldDuration getHoldDuration() {
        return noOfDays
                .map(NumberOfDays::of)
                .map(HoldDuration::closeEnded)
                .getOrElse(HoldDuration.openEnded());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaceOnHoldCommand that = (PlaceOnHoldCommand) o;
        return Objects.equals(timestamp, that.timestamp) && Objects.equals(patronId, that.patronId) && Objects.equals(libraryId, that.libraryId) && Objects.equals(bookId, that.bookId) && Objects.equals(noOfDays, that.noOfDays);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, patronId, libraryId, bookId, noOfDays);
    }

    @Override
    public String toString() {
        return "PlaceOnHoldCommand{" +
                "timestamp=" + timestamp +
                ", patronId=" + patronId +
                ", libraryId=" + libraryId +
                ", bookId=" + bookId +
                ", noOfDays=" + noOfDays +
                '}';
    }
}
