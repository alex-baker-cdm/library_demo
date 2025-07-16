package io.pillopl.library.lending.librarybranch.model;


import java.util.Objects;
import java.util.UUID;

public class LibraryBranchId {

    private final UUID libraryBranchId;

    public LibraryBranchId(UUID libraryBranchId) {
        this.libraryBranchId = Objects.requireNonNull(libraryBranchId);
    }

    public UUID getLibraryBranchId() {
        return libraryBranchId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibraryBranchId that = (LibraryBranchId) o;
        return Objects.equals(libraryBranchId, that.libraryBranchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(libraryBranchId);
    }

    @Override
    public String toString() {
        return "LibraryBranchId{" +
                "libraryBranchId=" + libraryBranchId +
                '}';
    }
}
