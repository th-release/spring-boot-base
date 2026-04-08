package com.threlease.base.common.configs;

import com.threlease.base.common.properties.app.AppProperties;
import com.threlease.base.common.properties.app.i18n.I18nProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Configuration
public class I18nConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setValidationMessageSource(messageSource);
        return validatorFactoryBean;
    }

    @Bean
    public LocaleResolver localeResolver(AppProperties appProperties) {
        I18nProperties i18n = appProperties.getI18n() != null ? appProperties.getI18n() : new I18nProperties();
        return new LocaleResolver() {
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                Locale defaultLocale = toLocale(i18n.getDefaultLocale());
                if (!i18n.isEnabled()) {
                    return defaultLocale;
                }

                String acceptLanguage = Objects.toString(request.getHeader("Accept-Language"), "").trim();
                if (acceptLanguage.isBlank()) {
                    return defaultLocale;
                }

                List<Locale.LanguageRange> languageRanges = Locale.LanguageRange.parse(acceptLanguage);
                List<Locale> supportedLocales = i18n.getSupportedLocales().stream()
                        .map(I18nConfig::toLocale)
                        .toList();
                Locale matched = Locale.lookup(languageRanges, supportedLocales);
                return matched != null ? matched : defaultLocale;
            }

            @Override
            public void setLocale(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Locale locale) {
                throw new UnsupportedOperationException("Locale is resolved from configuration and Accept-Language only.");
            }
        };
    }

    private static Locale toLocale(String localeCode) {
        if (localeCode == null || localeCode.isBlank()) {
            return Locale.KOREAN;
        }
        return Locale.forLanguageTag(localeCode.replace('_', '-'));
    }
}
