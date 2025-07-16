package io.pillopl.library.lending.book.new_model;

import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.commons.aggregates.Version;
import java.time.Instant;
import java.util.Objects;

public class AvailableState implements BookState {
    private final Book book;
    private final LibraryBranchId branch;

    public AvailableState(Book book, LibraryBranchId branch) {
        this.book = Objects.requireNonNull(book);
        this.branch = Objects.requireNonNull(branch);
    }

    @Override
    public BookState placeOnHold(PatronId patronId, LibraryBranchId branchId, Instant holdTill) {
        return new OnHoldState(book, branchId, patronId, holdTill);
    }

    @Override
    public BookState checkout(PatronId patronId, LibraryBranchId branchId) {
        return new CheckedOutState(book, branchId, patronId);
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
}
