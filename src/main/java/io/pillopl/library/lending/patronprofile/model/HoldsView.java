package io.pillopl.library.lending.patronprofile.model;

import io.vavr.collection.List;

import java.util.Objects;

public class HoldsView {

    private final List<Hold> currentHolds;

    public HoldsView(List<Hold> currentHolds) {
        this.currentHolds = Objects.requireNonNull(currentHolds);
    }

    public List<Hold> getCurrentHolds() {
        return currentHolds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoldsView holdsView = (HoldsView) o;
        return Objects.equals(currentHolds, holdsView.currentHolds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentHolds);
    }

    @Override
    public String toString() {
        return "HoldsView{" +
                "currentHolds=" + currentHolds +
                '}';
    }
}
