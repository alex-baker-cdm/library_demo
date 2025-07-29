package io.pillopl.library.commons.events.publisher;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.pillopl.library.commons.events.DomainEvent;
import io.pillopl.library.commons.events.DomainEvents;


public class MeteredDomainEventPublisher implements DomainEvents {

    private static final String DOMAIN_EVENTS = "domain_events";
    private static final String TAG_NAME = "name";

    private final DomainEvents delegate;
    private final MeterRegistry metricsRegistry;
    private final Tracer tracer;

    public MeteredDomainEventPublisher(DomainEvents delegate, MeterRegistry metricsRegistry, OpenTelemetry openTelemetry) {
        this.delegate = delegate;
        this.metricsRegistry = metricsRegistry;
        this.tracer = openTelemetry.getTracer("library-domain-events");
    }

    @Override
    public void publish(DomainEvent event) {
        Span span = tracer.spanBuilder("domain_event.publish")
                .setAttribute("event.type", event.getClass().getSimpleName())
                .setAttribute("event.aggregateId", event.getAggregateId().toString())
                .setAttribute("event.when", event.getWhen().toString())
                .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            delegate.publish(event);
            metricsRegistry.counter(DOMAIN_EVENTS, TAG_NAME, event.getClass().getSimpleName()).increment();
            span.setStatus(io.opentelemetry.api.trace.StatusCode.OK);
        } catch (Exception e) {
            span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
