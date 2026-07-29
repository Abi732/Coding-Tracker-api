package com.abi.coding_tracker.security;


import java.io.IOException;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final CustomUserDetailService userDetailService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailService userDetailService){
        this.jwtService = jwtService;
        this.userDetailService = userDetailService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    )throws ServletException, IOException{
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if(authHeader==null || !authHeader.startsWith("Bearer ")){
            log.trace("No bearer Token found in request to : {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try{
            userEmail = jwtService.extractUsername(jwt);
            log.debug("Extracted userName '{}' from JWT", userEmail);
    
            if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = this.userDetailService.loadUserByUsername(userEmail);
    
                if(jwtService.validateToken(jwt, userDetails.getUsername())){
                    log.debug("JWT is valid for user: {}", userEmail);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }else{
                    log.warn("Invalid JWT presented for user {}", userEmail);
                }
            }
        }catch(ExpiredJwtException e){
            log.warn("JWT has expired : {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,"JWT has expired. Please log in again.");
            return;
        }catch(SignatureException e){
            log.warn("Invalid JWT signature: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT signature");
            return;
        }catch(MalformedJwtException e){
            log.warn("Malformed JWT token: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Malformed JWT token");
            return;
        }catch(Exception e){
            log.error("Could not set user authentication in security context", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected authentication error occurred.");
            return;
        }

        filterChain.doFilter(request, response);

    }

    private void sendErrorResponse(HttpServletResponse response, int staus, String message) throws IOException{
        response.setStatus(staus);
        response.setContentType("application/json");

        String json = String.format(
            "{\"timestamp\": \"%s\", \"status\": %d, \"message\":\"%s\"}",
            java.time.LocalDateTime.now().toString(),
            staus,
            message 
        );
        response.getWriter().write(json);
    }

}
