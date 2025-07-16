package io.pillopl.library.lending.patron.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptySet;

class OverdueCheckouts {

    static int MAX_COUNT_OF_OVERDUE_RESOURCES = 2;

    private final Map<LibraryBranchId, Set<BookId>> overdueCheckouts;

    OverdueCheckouts(Map<LibraryBranchId, Set<BookId>> overdueCheckouts) {
        this.overdueCheckouts = Objects.requireNonNull(overdueCheckouts);
    }

    public Map<LibraryBranchId, Set<BookId>> getOverdueCheckouts() {
        return overdueCheckouts;
    }

    int countAt(LibraryBranchId libraryBranchId) {
        return overdueCheckouts.getOrDefault(libraryBranchId, emptySet()).size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OverdueCheckouts that = (OverdueCheckouts) o;
        return Objects.equals(overdueCheckouts, that.overdueCheckouts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(overdueCheckouts);
    }

    @Override
    public String toString() {
        return "OverdueCheckouts{" +
                "overdueCheckouts=" + overdueCheckouts +
                '}';
    }
}



