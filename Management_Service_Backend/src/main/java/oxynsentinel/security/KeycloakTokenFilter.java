package oxynsentinel.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import oxynsentinel.security.KeycloakJwtUtil;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KeycloakTokenFilter extends OncePerRequestFilter {

    private final KeycloakJwtUtil keycloakJwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                JWTClaimsSet claims = keycloakJwtUtil.validateToken(token);
                List<String> roles = keycloakJwtUtil.extractRoles(claims);

                // ✅ Inject attributes into request
                request.setAttribute("userId", claims.getSubject());
                request.setAttribute("roles", roles);
                request.setAttribute("email", claims.getStringClaim("email"));


                // ✅ Spring Security authentication
                List<GrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null, authorities
                );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token: " + e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}