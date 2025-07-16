package io.pillopl.library.lending.patron.model;

import io.pillopl.library.lending.book.model.AvailableBook;
import io.vavr.Function3;
import io.vavr.collection.List;
import io.vavr.control.Either;

import java.util.Objects;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

interface PlacingOnHoldPolicy extends Function3<AvailableBook, Patron, HoldDuration, Either<Rejection, Allowance>> {

    PlacingOnHoldPolicy onlyResearcherPatronsCanHoldRestrictedBooksPolicy = (AvailableBook toHold, Patron patron, HoldDuration holdDuration) -> {
        if (toHold.isRestricted() && patron.isRegular()) {
            return left(Rejection.withReason("Regular patrons cannot hold restricted books"));
        }
        return right(new Allowance());
    };

    PlacingOnHoldPolicy overdueCheckoutsRejectionPolicy = (AvailableBook toHold, Patron patron, HoldDuration holdDuration) -> {
        if (patron.overdueCheckoutsAt(toHold.getLibraryBranch()) >= OverdueCheckouts.MAX_COUNT_OF_OVERDUE_RESOURCES) {
            return left(Rejection.withReason("cannot place on hold when there are overdue checkouts"));
        }
        return right(new Allowance());
    };

    PlacingOnHoldPolicy regularPatronMaximumNumberOfHoldsPolicy = (AvailableBook toHold, Patron patron, HoldDuration holdDuration) -> {
        if (patron.isRegular() && patron.numberOfHolds() >= PatronHolds.MAX_NUMBER_OF_HOLDS) {
            return left(Rejection.withReason("patron cannot hold more books"));
        }
        return right(new Allowance());
    };

    PlacingOnHoldPolicy onlyResearcherPatronsCanPlaceOpenEndedHolds = (AvailableBook toHold, Patron patron, HoldDuration holdDuration) -> {
        if (patron.isRegular() && holdDuration.isOpenEnded()) {
            return left(Rejection.withReason("regular patron cannot place open ended holds"));
        }
        return right(new Allowance());
    };

    static List<PlacingOnHoldPolicy> allCurrentPolicies() {
        return List.of(
                onlyResearcherPatronsCanHoldRestrictedBooksPolicy,
                overdueCheckoutsRejectionPolicy,
                regularPatronMaximumNumberOfHoldsPolicy,
                onlyResearcherPatronsCanPlaceOpenEndedHolds);
    }

}

class Allowance {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Allowance{}";
    }
}

class Rejection {

    static class Reason {
        private final String reason;

        Reason(String reason) {
            this.reason = Objects.requireNonNull(reason);
        }

        public String getReason() {
            return reason;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Reason reason1 = (Reason) o;
            return Objects.equals(reason, reason1.reason);
        }

        @Override
        public int hashCode() {
            return Objects.hash(reason);
        }

        @Override
        public String toString() {
            return "Reason{" +
                    "reason='" + reason + '\'' +
                    '}';
        }
    }

    private final Reason reason;

    Rejection(Reason reason) {
        this.reason = Objects.requireNonNull(reason);
    }

    public Reason getReason() {
        return reason;
    }

    static Rejection withReason(String reason) {
        return new Rejection(new Reason(reason));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rejection rejection = (Rejection) o;
        return Objects.equals(reason, rejection.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reason);
    }

    @Override
    public String toString() {
        return "Rejection{" +
                "reason=" + reason +
                '}';
    }
}

