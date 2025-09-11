package io.pillopl.library.lending.patron.model;

import io.pillopl.library.lending.book.new_model.Book;

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

    boolean a(Book book) {
        Hold hold = new Hold(book.getBookId(), book.getCurrentBranch());
        return resourcesOnHold.contains(hold);
    }

    int count() {
        return resourcesOnHold.size();
    }

    boolean maximumHoldsAfterHolding(Book book) {
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
