package me.shinsunyoung.springbootdeveloper.config.jwt;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import me.shinsunyoung.springbootdeveloper.domain.User;

@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final JwtProperties jwtProperties;

    public String generateToken(User user, Duration expiredAt) {
        Date now = new Date();

        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), user);
    }

    // JWT token generate method
    private String makeToken(Date expiry, User user) {
        Date now = new Date();

        return Jwts.builder().setHeaderParam(Header.TYPE, Header.JWT_TYPE) // header type: JWT
                // propertise 파일에서 설정한 값
                .setIssuer(jwtProperties.getIssuer()).setIssuedAt(now) // content iat: current time
                .setExpiration(expiry) // content exp: variable expiry member
                .setSubject(user.getEmail()) // content sub: user's email
                .claim("id", user.getId()) // claim id: user's id
                // sign: encrypt hash value with secret key by HS256
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey()).compact();
    }

    // jwt token validation method
    public boolean validToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtProperties.getSecretKey()) // encryption with secret key
                    .parseClaimsJws(token);

            return true;
        } catch (Exception e) { // if invoking error while encryption that token is not validated
            // TODO: handle exception
            return false;

        }
    }

    // the method that bring authentic information by token based
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);

        Set<SimpleGrantedAuthority> authorities =
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        return new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User(claims.getSubject(), "",
                        authorities),
                token, authorities);
    }

    // the method that bring user id by token based
    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        return claims.get("id", Long.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser() // query claims
                .setSigningKey(jwtProperties.getSecretKey()).parseClaimsJws(token).getBody();
    }
}
