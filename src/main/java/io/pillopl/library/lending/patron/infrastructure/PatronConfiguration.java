package io.pillopl.library.lending.patron.infrastructure;

import io.opentelemetry.api.OpenTelemetry;
import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.dailysheet.model.DailySheet;
import io.pillopl.library.lending.patron.application.checkout.CheckingOutBookOnHold;
import io.pillopl.library.lending.patron.application.checkout.RegisteringOverdueCheckout;
import io.pillopl.library.lending.patron.application.hold.CancelingHold;
import io.pillopl.library.lending.patron.application.hold.ExpiringHolds;
import io.pillopl.library.lending.patron.application.hold.FindAvailableBook;
import io.pillopl.library.lending.patron.application.hold.FindBookOnHold;
import io.pillopl.library.lending.patron.application.hold.HandleDuplicateHold;
import io.pillopl.library.lending.patron.application.hold.PlacingOnHold;
import io.pillopl.library.lending.patron.model.PatronFactory;
import io.pillopl.library.lending.patron.model.Patrons;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@Configuration
@EnableJdbcRepositories
public class PatronConfiguration {

    @Bean
    CheckingOutBookOnHold checkingOutBookOnHold(FindBookOnHold findBookOnHold, Patrons patronRepository) {
        return new CheckingOutBookOnHold(findBookOnHold, patronRepository);
    }

    @Bean
    RegisteringOverdueCheckout registeringOverdueCheckout(DailySheet dailySheet, Patrons patronRepository) {
        return new RegisteringOverdueCheckout(dailySheet, patronRepository);
    }

    @Bean
    CancelingHold cancelingHold(FindBookOnHold findBookOnHold, Patrons patronRepository, OpenTelemetry openTelemetry) {
        return new CancelingHold(findBookOnHold, patronRepository, openTelemetry);
    }

    @Bean
    ExpiringHolds expiringHolds(DailySheet dailySheet, Patrons patronRepository) {
        return new ExpiringHolds(dailySheet, patronRepository);
    }

    @Bean
    HandleDuplicateHold handleDuplicateHold(CancelingHold cancelingHold) {
        return new HandleDuplicateHold(cancelingHold);
    }

    @Bean
    PlacingOnHold placingOnHold(FindAvailableBook findAvailableBook, Patrons patronRepository, OpenTelemetry openTelemetry) {
        return new PlacingOnHold(findAvailableBook, patronRepository, openTelemetry);
    }

    @Bean
    Patrons patronRepository(PatronEntityRepository patronEntityRepository,
                             DomainEvents domainEvents) {
        return new PatronsDatabaseRepository(
                patronEntityRepository,
                new DomainModelMapper(new PatronFactory()),
                domainEvents);
    }
}
