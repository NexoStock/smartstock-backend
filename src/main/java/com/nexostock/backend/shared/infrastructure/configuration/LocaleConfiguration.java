
package com.nexostock.backend.shared.infrastructure.configuration;

import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
public class LocaleConfiguration {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver =
                new AcceptHeaderLocaleResolver();

        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setSupportedLocales(
                List.of(Locale.ENGLISH, Locale.forLanguageTag("es"))
        );

        return resolver;
    }
}
