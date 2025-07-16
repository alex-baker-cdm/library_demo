package io.pillopl.library.lending.patron.model;

import java.util.Objects;

public class NumberOfDays {

    private final int days;

    private NumberOfDays(int days) {
        if(days <= 0) {
            throw new IllegalArgumentException("Cannot use negative integer or zero as number of days");
        }
        this.days = days;
    }

    public static NumberOfDays of(int days) {
        return new NumberOfDays(days);
    }

    public int getDays() {
        return days;
    }

    boolean isGreaterThan(int days) {
        return this.days > days;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumberOfDays that = (NumberOfDays) o;
        return days == that.days;
    }

    @Override
    public int hashCode() {
        return Objects.hash(days);
    }

    @Override
    public String toString() {
        return "NumberOfDays{" +
                "days=" + days +
                '}';
    }
}
