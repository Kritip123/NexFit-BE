package org.example.nexfit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainerJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Qualifier("trainerDetailsService")
    private final UserDetailsService trainerDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!isTrainerPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String trainerEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            String tokenType = jwtService.extractClaim(jwt, claims -> claims.get("tokenType", String.class));
            if (!"trainer".equalsIgnoreCase(tokenType)) {
                filterChain.doFilter(request, response);
                return;
            }

            trainerEmail = jwtService.extractUsername(jwt);

            if (trainerEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails trainerDetails = this.trainerDetailsService.loadUserByUsername(trainerEmail);

                if (jwtService.isTokenValid(jwt, trainerDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            trainerDetails,
                            null,
                            trainerDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set trainer authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isTrainerPath(HttpServletRequest request) {
        String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
        String uri = request.getRequestURI();
        String path = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
        return path.startsWith("/trainer/");
    }
}
