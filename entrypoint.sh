#!/bin/sh
set -e

JAVA_OPTS="${JAVA_OPTS} \
  -Dspring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME} \
  -Dspring.datasource.username=${DB_USER} \
  -Dspring.datasource.password=${DB_PASSWORD} \
  -Dspring.datasource.driver-class-name=org.postgresql.Driver \
  -Dserver.port=${SERVER_PORT:-8080} \
  -Dapp.security.jwt.secret=${JWT_SECRET} \
  -Dapp.security.jwt.access-expiration-ms=${JWT_ACCESS_EXPIRATION_MS:-3600000} \
  -Dapp.security.jwt.refresh-expiration-ms=${JWT_REFRESH_EXPIRATION_MS:-604800000} \
  -Dapp.encryption.key=${ENCRYPTION_KEY} \
  -Dapp.stripe.secret-key=${STRIPE_SECRET_KEY} \
  -Dapp.cors.allowed-origins=${CORS_ALLOWED_ORIGINS} \
  -Dapp.oauth2.success-url=${OAUTH2_SUCCESS_URL:-/} \
  -Dapp.oauth2.google-redirect-uri=${GOOGLE_REDIRECT_URI} \
  -Dapp.email.employee-domain=${EMPLOYEE_EMAIL_DOMAIN} \
  -Dapp.email.patient-domains=${PATIENT_EMAIL_DOMAINS} \
  -Dapp.consent.version=${CONSENT_VERSION:-v1.0} \
  -Dapp.email.enabled=${EMAIL_ENABLED:-false} \
  -Dapp.email.provider=${EMAIL_PROVIDER:-smtp} \
  -Dapp.email.smtp.host=${SMTP_HOST} \
  -Dapp.email.smtp.port=${SMTP_PORT:-587} \
  -Dapp.email.smtp.username=${SMTP_USERNAME} \
  -Dapp.email.smtp.password=${SMTP_PASSWORD} \
  -Dapp.email.from=${EMAIL_DEFAULT_FROM}"

exec java $JAVA_OPTS -jar app.jar
