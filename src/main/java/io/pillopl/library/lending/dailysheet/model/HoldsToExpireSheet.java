package io.pillopl.library.lending.dailysheet.model;

import io.pillopl.library.lending.patron.model.PatronEvent;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import org.springframework.context.event.EventListener;

import java.util.Objects;

public class HoldsToExpireSheet {

    private final List<ExpiredHold> expiredHolds;

    public HoldsToExpireSheet(List<ExpiredHold> expiredHolds) {
        this.expiredHolds = Objects.requireNonNull(expiredHolds);
    }

    public List<ExpiredHold> getExpiredHolds() {
        return expiredHolds;
    }

    @EventListener
    public Stream<PatronEvent.BookHoldExpired> toStreamOfEvents() {
        return expiredHolds
                .toStream()
                .map(ExpiredHold::toEvent);
    }

    public int count() {
        return expiredHolds.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoldsToExpireSheet that = (HoldsToExpireSheet) o;
        return Objects.equals(expiredHolds, that.expiredHolds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expiredHolds);
    }

    @Override
    public String toString() {
        return "HoldsToExpireSheet{" +
                "expiredHolds=" + expiredHolds +
                '}';
    }
}
