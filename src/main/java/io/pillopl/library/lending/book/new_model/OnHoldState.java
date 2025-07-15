package io.pillopl.library.lending.book.new_model;

import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.commons.aggregates.Version;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.Instant;

@Getter
public class OnHoldState implements BookState {
    private Book book;
    private final LibraryBranchId holdPlacedAt;
    private final PatronId byPatron;
    private final Instant holdTill;

    public OnHoldState(Book book, LibraryBranchId holdPlacedAt, PatronId byPatron, Instant holdTill) {
        this.book = book;
        this.holdPlacedAt = holdPlacedAt;
        this.byPatron = byPatron;
        this.holdTill = holdTill;
    }

    @Override
    public BookState placeOnHold(PatronId patronId, LibraryBranchId branchId, Instant holdTill) {
        return invalidTransition("Book is already on hold");
    }

    @Override
    public BookState checkout(PatronId patronId, LibraryBranchId branchId) {
        if (!patronId.equals(byPatron)) {
            return invalidTransition("Only the patron who placed the hold can check out the book");
        }
        CheckedOutState newState = new CheckedOutState(book, branchId, patronId);
        newState.setBook(book);
        return newState;
    }

    @Override
    public BookState returnBook(LibraryBranchId branchId) {
        return invalidTransition("Cannot return a book that is on hold");
    }

    @Override
    public BookState cancelHold() {
        AvailableState newState = new AvailableState(book, holdPlacedAt);
        newState.setBook(book);
        return newState;
    }

    @Override
    public BookState expireHold() {
        if (Instant.now().isAfter(holdTill)) {
            AvailableState newState = new AvailableState(book, holdPlacedAt);
            newState.setBook(book);
            return newState;
        }
        return this;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    @Override
    public boolean canBeCheckedOut(PatronId patronId) {
        return patronId.equals(byPatron);
    }

    @Override
    public boolean canBePutOnHold(PatronId patronId) {
        return false;
    }

    @Override
    public boolean canBeReturned() {
        return false;
    }

    @Override
    public String getStateName() {
        return "ON_HOLD";
    }

    @Override
    public Version getVersion() {
        return book.getVersion();
    }

    @Override
    public LibraryBranchId getCurrentBranch() {
        return holdPlacedAt;
    }

    @Override
    public PatronId getCurrentPatron() {
        return byPatron;
    }
}
