package io.pillopl.library.lending.patron.model;

import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.BookOnHold;

import java.util.Objects;
import java.util.Set;

class PatronHolds {

    static int MAX_NUMBER_OF_HOLDS = 5;

    private final Set<Hold> resourcesOnHold;

    PatronHolds(Set<Hold> resourcesOnHold) {
        this.resourcesOnHold = Objects.requireNonNull(resourcesOnHold);
    }

    public Set<Hold> getResourcesOnHold() {
        return resourcesOnHold;
    }

    boolean a(BookOnHold bookOnHold) {
        Hold hold = new Hold(bookOnHold.getBookId(), bookOnHold.getHoldPlacedAt());
        return resourcesOnHold.contains(hold);
    }

    int count() {
        return resourcesOnHold.size();
    }

    boolean maximumHoldsAfterHolding(AvailableBook book) {
        return count() + 1 == MAX_NUMBER_OF_HOLDS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatronHolds that = (PatronHolds) o;
        return Objects.equals(resourcesOnHold, that.resourcesOnHold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourcesOnHold);
    }

    @Override
    public String toString() {
        return "PatronHolds{" +
                "resourcesOnHold=" + resourcesOnHold +
                '}';
    }
}
