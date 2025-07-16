package io.pillopl.library.lending.patron.model;

import java.util.Objects;

import java.time.Duration;
import java.time.Instant;

import static io.pillopl.library.lending.patron.model.NumberOfDays.of;
import static java.time.Instant.now;

public class CheckoutDuration {

    static final int MAX_CHECKOUT_DURATION  = 60;

    private final NumberOfDays noOfDays;
    private final Instant from;

    private CheckoutDuration(Instant from, NumberOfDays noOfDays) {
        if(noOfDays.isGreaterThan(MAX_CHECKOUT_DURATION)) {
            throw new IllegalArgumentException("Cannot checkout for more than " + MAX_CHECKOUT_DURATION + " days!");
        }
        this.noOfDays = Objects.requireNonNull(noOfDays);
        this.from = Objects.requireNonNull(from);
    }

    public NumberOfDays getNoOfDays() {
        return noOfDays;
    }

    public Instant getFrom() {
        return from;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckoutDuration that = (CheckoutDuration) o;
        return Objects.equals(noOfDays, that.noOfDays) && Objects.equals(from, that.from);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noOfDays, from);
    }

    @Override
    public String toString() {
        return "CheckoutDuration{" +
                "noOfDays=" + noOfDays +
                ", from=" + from +
                '}';
    }

    public static CheckoutDuration forNoOfDays(int noOfDays) {
        return forNoOfDays(now(), noOfDays);
    }

    static CheckoutDuration forNoOfDays(Instant from, int noOfDays) {
        return new CheckoutDuration(from, of(noOfDays));
    }

    public static CheckoutDuration maxDuration() {
        return forNoOfDays(MAX_CHECKOUT_DURATION);
    }

    Instant to() {
        return from.plus(Duration.ofDays(noOfDays.getDays()));
    }
}
