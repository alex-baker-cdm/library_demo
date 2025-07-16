package io.pillopl.library.lending.patronprofile.model;

import io.vavr.collection.List;

import java.util.Objects;

public class CheckoutsView {

    private final List<Checkout> currentCheckouts;

    public CheckoutsView(List<Checkout> currentCheckouts) {
        this.currentCheckouts = Objects.requireNonNull(currentCheckouts);
    }

    public List<Checkout> getCurrentCheckouts() {
        return currentCheckouts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckoutsView that = (CheckoutsView) o;
        return Objects.equals(currentCheckouts, that.currentCheckouts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentCheckouts);
    }

    @Override
    public String toString() {
        return "CheckoutsView{" +
                "currentCheckouts=" + currentCheckouts +
                '}';
    }
}
