package io.pillopl.library.lending.dailysheet.model;

import io.pillopl.library.lending.patron.model.PatronEvent.OverdueCheckoutRegistered;
import io.vavr.collection.List;
import io.vavr.collection.Stream;

import java.util.Objects;

public class CheckoutsToOverdueSheet {

    private final List<OverdueCheckout> checkouts;

    public CheckoutsToOverdueSheet(List<OverdueCheckout> checkouts) {
        this.checkouts = Objects.requireNonNull(checkouts);
    }

    public List<OverdueCheckout> getCheckouts() {
        return checkouts;
    }

    public Stream<OverdueCheckoutRegistered> toStreamOfEvents() {
        return checkouts.toStream()
                .map(OverdueCheckout::toEvent);
    }

    public int count() {
        return checkouts.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckoutsToOverdueSheet that = (CheckoutsToOverdueSheet) o;
        return Objects.equals(checkouts, that.checkouts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(checkouts);
    }

    @Override
    public String toString() {
        return "CheckoutsToOverdueSheet{" +
                "checkouts=" + checkouts +
                '}';
    }
}
