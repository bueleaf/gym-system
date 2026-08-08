package com.example.training.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TransactionIdFilter extends OncePerRequestFilter
{
    private static final Logger LOG =
            LoggerFactory.getLogger(TransactionIdFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException
    {
        String transactionId = request.getHeader("X-Transaction-Id");

        if (transactionId == null || transactionId.isBlank())
        {
            transactionId = UUID.randomUUID().toString();
        }

        MDC.put("transactionId", transactionId);
        response.setHeader("X-Transaction-Id", transactionId);

        try
        {
            LOG.info("Incoming request: {} {}",
                    request.getMethod(),
                    request.getRequestURI());

            chain.doFilter(request, response);

            LOG.info("Completed request: {} {} {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus());
        }
        finally
        {
            MDC.remove("transactionId");
        }
    }


}
