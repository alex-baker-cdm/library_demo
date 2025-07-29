package io.pillopl.library.common.events.publisher;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.commons.events.publisher.JustForwardDomainEventPublisher;
import io.pillopl.library.commons.events.publisher.MeteredDomainEventPublisher;
import io.pillopl.library.commons.events.publisher.StoreAndForwardDomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DomainEventsTestConfig {

    @Bean
    @Primary
    DomainEvents domainEventsWithStorage(ApplicationEventPublisher applicationEventPublisher, MeterRegistry meterRegistry, OpenTelemetry openTelemetry) {
        return new StoreAndForwardDomainEventPublisher(
                new MeteredDomainEventPublisher(
                        new JustForwardDomainEventPublisher(applicationEventPublisher), meterRegistry, openTelemetry),
                new InMemoryEventsStorage()
        );
    }
}
