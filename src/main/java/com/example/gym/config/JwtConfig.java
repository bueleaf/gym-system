package com.example.gym.config;

import com.example.gym.web.JwtProperties;
import com.example.gym.web.RevokedTokenValidator;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public RSAPublicKey jwtPublicKey(
            JwtProperties properties
    ) throws IOException {

        try (InputStream inputStream =
                     properties.publicKey()
                             .getInputStream()) {

            RSAPublicKey publicKey =
                    RsaKeyConverters.x509()
                            .convert(inputStream);

            if (publicKey == null) {
                throw new IllegalStateException(
                        "Could not read JWT public key"
                );
            }

            return publicKey;
        }
    }

    @Bean
    public RSAPrivateKey jwtPrivateKey(
            JwtProperties properties
    ) throws IOException {

        try (InputStream inputStream =
                     properties.privateKey()
                             .getInputStream()) {

            RSAPrivateKey privateKey =
                    RsaKeyConverters.pkcs8()
                            .convert(inputStream);

            if (privateKey == null) {
                throw new IllegalStateException(
                        "Could not read JWT private key"
                );
            }

            return privateKey;
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(
            RSAPublicKey publicKey,
            RSAPrivateKey privateKey
    ) {
        RSAKey rsaKey =
                new RSAKey.Builder(publicKey)
                        .privateKey(privateKey)
                        .build();

        JWKSource<SecurityContext> jwkSource =
                new ImmutableJWKSet<>(
                        new JWKSet(rsaKey)
                );

        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(
            RSAPublicKey publicKey,
            JwtProperties properties,
            RevokedTokenValidator revokedTokenValidator
    ) {
        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withPublicKey(publicKey)
                        .build();

        var standardValidator =
                JwtValidators.createDefaultWithIssuer(
                        properties.issuer()
                );

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<Jwt>(
                        standardValidator,
                        revokedTokenValidator
                )
        );

        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter
    jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName(
                "roles"
        );

        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return converter;
    }
}