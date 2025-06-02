package com.banque.accountservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secretKey;
    private long expiration;
    private long refreshExpiration;
    private String issuer;

    // Getters et Setters
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public long getExpiration() { return expiration; }
    public void setExpiration(long expiration) { this.expiration = expiration; }

    public long getRefreshExpiration() { return refreshExpiration; }
    public void setRefreshExpiration(long refreshExpiration) { this.refreshExpiration = refreshExpiration; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
}