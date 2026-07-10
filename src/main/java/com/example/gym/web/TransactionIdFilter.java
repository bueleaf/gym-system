package com.example.gym.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

public class TransactionIdFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(TransactionIdFilter.class);

    private static final String TRANSACTION_ID =
            "transactionId";

    private static final String TRANSACTION_HEADER =
            "X-Transaction-Id";

    private static final Pattern PASSWORD_FIELD_PATTERN =
            Pattern.compile(
                    "(?i)(\"(?:oldPassword|newPassword|password)\"\\s*:\\s*\")([^\"]*)(\")"
            );

    private static final Pattern PASSWORD_QUERY_PATTERN =
            Pattern.compile("(?i)(password=)([^&]*)");

    private static final int MAX_LOGGED_BODY_LENGTH = 2_000;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest =
                new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse =
                new ContentCachingResponseWrapper(response);

        String transactionId =
                request.getHeader(TRANSACTION_HEADER);

        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }

        MDC.put(TRANSACTION_ID, transactionId);

        response.setHeader(
                TRANSACTION_HEADER,
                transactionId
        );

        long startTime = System.currentTimeMillis();

        try {
            logger.info(
                    "REST request started: method={}, path={}, query={}",
                    wrappedRequest.getMethod(),
                    wrappedRequest.getRequestURI(),
                    redactQuery(wrappedRequest.getQueryString())
            );

            filterChain.doFilter(wrappedRequest, wrappedResponse);

            logger.info(
                    "REST request completed: method={}, path={}, query={}, requestBody={}, status={}, responseBody={}, durationMs={}",
                    wrappedRequest.getMethod(),
                    wrappedRequest.getRequestURI(),
                    redactQuery(wrappedRequest.getQueryString()),
                    loggedBody(wrappedRequest.getContentAsByteArray(), wrappedRequest.getCharacterEncoding()),
                    wrappedResponse.getStatus(),
                    loggedBody(wrappedResponse.getContentAsByteArray(), wrappedResponse.getCharacterEncoding()),
                    System.currentTimeMillis() - startTime
            );
        } catch (Exception exception) {
            logger.error(
                    "REST request failed: method={}, path={}, query={}, body={}, message={}",
                    wrappedRequest.getMethod(),
                    wrappedRequest.getRequestURI(),
                    redactQuery(wrappedRequest.getQueryString()),
                    loggedBody(wrappedRequest.getContentAsByteArray(), wrappedRequest.getCharacterEncoding()),
                    exception.getMessage(),
                    exception
            );

            throw exception;
        } finally {
            wrappedResponse.copyBodyToResponse();
            MDC.remove(TRANSACTION_ID);
        }
    }

    private String loggedBody(byte[] content, String characterEncoding) {
        if (content.length == 0) {
            return "";
        }

        Charset charset = characterEncoding == null
                ? StandardCharsets.UTF_8
                : Charset.forName(characterEncoding);

        String body = new String(content, charset);
        String redactedBody = PASSWORD_FIELD_PATTERN
                .matcher(body)
                .replaceAll("$1***$3");

        if (redactedBody.length() <= MAX_LOGGED_BODY_LENGTH) {
            return redactedBody;
        }

        return redactedBody.substring(0, MAX_LOGGED_BODY_LENGTH)
                + "...[truncated]";
    }

    private String redactQuery(String query) {
        if (query == null || query.isBlank()) {
            return query;
        }

        return PASSWORD_QUERY_PATTERN
                .matcher(query)
                .replaceAll("$1***");
    }
}
