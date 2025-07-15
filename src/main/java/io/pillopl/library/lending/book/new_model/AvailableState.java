package io.pillopl.library.lending.book.new_model;

import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.commons.aggregates.Version;
import lombok.RequiredArgsConstructor;
import java.time.Instant;

public class AvailableState implements BookState {
    private Book book;
    private final LibraryBranchId branch;

    public AvailableState(Book book, LibraryBranchId branch) {
        this.book = book;
        this.branch = branch;
    }

    @Override
    public BookState placeOnHold(PatronId patronId, LibraryBranchId branchId, Instant holdTill) {
        OnHoldState newState = new OnHoldState(book, branchId, patronId, holdTill);
        newState.setBook(book);
        return newState;
    }

    @Override
    public BookState checkout(PatronId patronId, LibraryBranchId branchId) {
        CheckedOutState newState = new CheckedOutState(book, branchId, patronId);
        newState.setBook(book);
        return newState;
    }

    @Override
    public BookState returnBook(LibraryBranchId branchId) {
        return invalidTransition("Cannot return an available book");
    }

    @Override
    public BookState cancelHold() {
        return invalidTransition("Cannot cancel hold on an available book");
    }

    @Override
    public BookState expireHold() {
        return invalidTransition("Cannot expire hold on an available book");
    }

    @Override
    public boolean canBeCheckedOut(PatronId patronId) {
        return true;
    }

    @Override
    public boolean canBePutOnHold(PatronId patronId) {
        return true;
    }

    @Override
    public boolean canBeReturned() {
        return false;
    }

    @Override
    public String getStateName() {
        return "AVAILABLE";
    }

    @Override
    public Version getVersion() {
        return book.getVersion();
    }

    @Override
    public LibraryBranchId getCurrentBranch() {
        return branch;
    }

    @Override
    public PatronId getCurrentPatron() {
        return null;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}
