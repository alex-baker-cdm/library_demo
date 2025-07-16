package io.pillopl.library.lending.patronprofile.model;

import io.pillopl.library.catalogue.BookId;
import io.vavr.control.Option;

import java.util.Objects;

public class PatronProfile {

    private final HoldsView holdsView;
    private final CheckoutsView currentCheckouts;

    public PatronProfile(HoldsView holdsView, CheckoutsView currentCheckouts) {
        this.holdsView = Objects.requireNonNull(holdsView);
        this.currentCheckouts = Objects.requireNonNull(currentCheckouts);
    }

    public HoldsView getHoldsView() {
        return holdsView;
    }

    public CheckoutsView getCurrentCheckouts() {
        return currentCheckouts;
    }

    public Option<Hold> findHold(BookId bookId) {
        return
                holdsView
                        .getCurrentHolds()
                        .toStream()
                        .find(hold -> hold.getBook().equals(bookId));
    }

    public Option<Checkout> findCheckout(BookId bookId) {
        return
                currentCheckouts
                        .getCurrentCheckouts()
                        .toStream()
                        .find(hold -> hold.getBook().equals(bookId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatronProfile that = (PatronProfile) o;
        return Objects.equals(holdsView, that.holdsView) && Objects.equals(currentCheckouts, that.currentCheckouts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(holdsView, currentCheckouts);
    }

    @Override
    public String toString() {
        return "PatronProfile{" +
                "holdsView=" + holdsView +
                ", currentCheckouts=" + currentCheckouts +
                '}';
    }
}
