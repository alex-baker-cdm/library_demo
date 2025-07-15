package io.pillopl.library.lending.book.new_model;

import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.commons.aggregates.Version;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.Instant;

@Getter
public class CheckedOutState implements BookState {
    private Book book;
    private final LibraryBranchId checkedOutAt;
    private final PatronId byPatron;

    public CheckedOutState(Book book, LibraryBranchId checkedOutAt, PatronId byPatron) {
        this.book = book;
        this.checkedOutAt = checkedOutAt;
        this.byPatron = byPatron;
    }

    @Override
    public BookState placeOnHold(PatronId patronId, LibraryBranchId branchId, Instant holdTill) {
        return invalidTransition("Cannot place a hold on a checked out book");
    }

    @Override
    public BookState checkout(PatronId patronId, LibraryBranchId branchId) {
        return invalidTransition("Book is already checked out");
    }

    @Override
    public BookState returnBook(LibraryBranchId branchId) {
        AvailableState newState = new AvailableState(book, branchId);
        newState.setBook(book);
        return newState;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    @Override
    public BookState cancelHold() {
        return invalidTransition("Cannot cancel hold on a checked out book");
    }

    @Override
    public BookState expireHold() {
        return invalidTransition("Cannot expire hold on a checked out book");
    }

    @Override
    public boolean canBeCheckedOut(PatronId patronId) {
        return false;
    }

    @Override
    public boolean canBePutOnHold(PatronId patronId) {
        return false;
    }

    @Override
    public boolean canBeReturned() {
        return true;
    }

    @Override
    public String getStateName() {
        return "CHECKED_OUT";
    }

    @Override
    public Version getVersion() {
        return book.getVersion();
    }

    @Override
    public LibraryBranchId getCurrentBranch() {
        return checkedOutAt;
    }

    @Override
    public PatronId getCurrentPatron() {
        return byPatron;
    }
}
