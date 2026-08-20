package ru.bankpet.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Optional;

@Component
public class HttpsSecurityFilter implements Filter {
    private final boolean requireHttps;
    public HttpsSecurityFilter(@Value("${app.security.require-https:false}") boolean requireHttps) { this.requireHttps = requireHttps; }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) request; HttpServletResponse result = (HttpServletResponse) response;
        String forwardedProto = http.getHeader("X-Forwarded-Proto");
        if (requireHttps && "http".equalsIgnoreCase(forwardedProto)) {
            String host = Optional.ofNullable(http.getHeader("X-Forwarded-Host")).orElse(http.getServerName());
            if (!host.matches("[A-Za-z0-9.-]+(?::[0-9]{1,5})?")) { result.sendError(HttpServletResponse.SC_BAD_REQUEST); return; }
            result.setStatus(HttpServletResponse.SC_PERMANENT_REDIRECT);
            result.setHeader("Location", "https://" + host + http.getRequestURI() + (http.getQueryString() == null ? "" : "?" + http.getQueryString()));
            return;
        }
        result.setHeader("X-Content-Type-Options", "nosniff");
        result.setHeader("X-Frame-Options", "DENY");
        result.setHeader("Referrer-Policy", "no-referrer");
        result.setHeader("Permissions-Policy", "camera=(self), microphone=(), geolocation=()");
        chain.doFilter(request, response);
    }
}
