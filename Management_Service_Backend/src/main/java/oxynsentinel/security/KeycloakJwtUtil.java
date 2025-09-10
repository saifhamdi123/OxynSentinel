package oxynsentinel.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.*;
import com.nimbusds.jose.proc.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class KeycloakJwtUtil {

    @Value("${keycloak.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${keycloak.issuer-uri}")
    private String issuer;

    @Value("${keycloak.client-id}")
    private String clientId;

    public JWTClaimsSet validateToken(String token) throws Exception {
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

        JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(jwkSetUri));
        JWSKeySelector<SecurityContext> keySelector =
                new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);

        jwtProcessor.setJWSKeySelector(keySelector);

        SecurityContext ctx = null;
        JWTClaimsSet claims = jwtProcessor.process(token, ctx);

        // Extra validation
        if (!issuer.equals(claims.getIssuer())) {
            throw new Exception("Invalid issuer");
        }

        if (!claims.getAudience().contains(clientId) && !claims.getAudience().contains("account")) {
            throw new Exception("Invalid audience");
        }

        System.out.println("Issuer: " + claims.getIssuer());
        System.out.println("Audience: " + claims.getAudience());
        System.out.println("Subject: " + claims.getSubject());


        return claims;
    }

    public List<String> extractRoles(JWTClaimsSet claims) {
        try {
            Map<String, Object> resourceAccess = (Map<String, Object>) claims.getClaim("resource_access");
            if (resourceAccess == null) return Collections.emptyList();

            Map<String, Object> client = (Map<String, Object>) resourceAccess.get(clientId);
            if (client == null) return Collections.emptyList();

            return (List<String>) client.get("roles");
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
