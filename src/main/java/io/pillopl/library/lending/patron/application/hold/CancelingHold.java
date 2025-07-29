package io.pillopl.library.lending.patron.application.hold;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.patron.model.Patron;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCanceled;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCancelingFailed;
import io.pillopl.library.lending.patron.model.Patrons;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Either;
import io.vavr.control.Try;

import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;
import static io.vavr.API.*;
import static io.vavr.Patterns.$Left;
import static io.vavr.Patterns.$Right;

public class CancelingHold {

    private final FindBookOnHold findBookOnHold;
    private final Patrons patronRepository;
    private final Tracer tracer;

    public CancelingHold(FindBookOnHold findBookOnHold, Patrons patronRepository, OpenTelemetry openTelemetry) {
        this.findBookOnHold = findBookOnHold;
        this.patronRepository = patronRepository;
        this.tracer = openTelemetry.getTracer("library-lending");
    }

    public Try<Result> cancelHold(CancelHoldCommand command) {
        Span span = tracer.spanBuilder("patron.cancel_hold")
                .setAttribute("patron.id", command.getPatronId().getPatronId().toString())
                .setAttribute("book.id", command.getBookId().getBookId().toString())
                .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            return Try.of(() -> {
                BookOnHold bookOnHold = find(command.getBookId(), command.getPatronId());
                Patron patron = find(command.getPatronId());
                Either<BookHoldCancelingFailed, BookHoldCanceled> result = patron.cancelHold(bookOnHold);
                Result finalResult = Match(result).of(
                        Case($Left($()), this::publishEvents),
                        Case($Right($()), this::publishEvents)
                );
                
                span.setAttribute("operation.result", finalResult == Result.Success ? "success" : "failure");
                span.setStatus(io.opentelemetry.api.trace.StatusCode.OK);
                return finalResult;
            }).onFailure(t -> {
                span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, t.getMessage());
                span.recordException(t);
            });
        } finally {
            span.end();
        }
    }

    private Result publishEvents(BookHoldCanceled bookHoldCanceled) {
        patronRepository.publish(bookHoldCanceled);
        return Success;
    }

    private Result publishEvents(BookHoldCancelingFailed bookHoldCancelingFailed) {
        patronRepository.publish(bookHoldCancelingFailed);
        return Rejection;
    }

    private BookOnHold find(BookId bookId, PatronId patronId) {
        return findBookOnHold
                .findBookOnHold(bookId, patronId)
                .getOrElseThrow(() -> new IllegalArgumentException("Cannot find book on hold with Id: " + bookId.getBookId()));
    }

    private Patron find(PatronId patronId) {
        return patronRepository
                .findBy(patronId)
                .getOrElseThrow(() -> new IllegalArgumentException("Patron with given Id does not exists: " + patronId.getPatronId()));
    }
}

