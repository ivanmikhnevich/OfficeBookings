package ivanmikhnevich.officebookings.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private final RSAPublicKey publickey;
    private final String expectedIssuer;
    private final String expectedAudience;

    public JWTAuthenticationFilter(RSAPublicKey publickey, String expectedIssuer, String expectedAudience) {
        this.publickey = publickey;
        this.expectedIssuer = expectedIssuer;
        this.expectedAudience = expectedAudience;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // Отсутствие заголовка — не 401 здесь; пусть решает entry point.
        // Так публичные ручки (например, /actuator/health) останутся доступны.
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            // 1. Проверка подписи
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!jwt.verify(verifier)) {
                chain.doFilter(request, response); // останется анонимным → 401
                return;
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            // 2. Проверка iss / aud / exp / sub
            if (!expectedIssuer.equals(claims.getIssuer())) {
                chain.doFilter(request, response);
                return;
            }
            List<String> aud = claims.getAudience();
            if (aud == null || !aud.contains(expectedAudience)) {
                chain.doFilter(request, response);
                return;
            }
            Date exp = claims.getExpirationTime();
            if (exp == null || exp.before(new Date())) {
                chain.doFilter(request, response);
                return;
            }
            String sub = claims.getSubject();
            if (sub == null || sub.isBlank()) {
                chain.doFilter(request, response);
                return;
            }

            // 3. Роли
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.getClaim("roles");
            Set<String> roleSet = roles == null ? Set.of() : Set.copyOf(roles);

            CurrentUser principal = new CurrentUser(sub, roleSet);

            var authorities = roleSet.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toSet());

            var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception ex) {
            // Битый токен → остаёмся анонимными → 401 на защищённых ручках
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
