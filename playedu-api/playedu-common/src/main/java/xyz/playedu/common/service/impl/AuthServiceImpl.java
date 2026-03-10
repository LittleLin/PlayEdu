/*
 * Copyright (C) 2023 杭州白書科技有限公司
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.playedu.common.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.playedu.common.config.AuthConfig;
import xyz.playedu.common.service.AuthService;
import xyz.playedu.common.util.RequestUtil;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired private AuthConfig authConfig;

    @Override
    public String loginUsingId(Integer userId, String loginUrl, String prv) {
        long expiredAt = System.currentTimeMillis() + authConfig.getExpired() * 1000L;
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .id(jti)
                .claim("url", loginUrl)
                .claim("prv", prv)
                .claim("exp", String.valueOf(expiredAt))
                .issuedAt(new Date())
                .expiration(new Date(expiredAt))
                .signWith(signingKey(), Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public boolean check(String prv) {
        Claims claims = parseCurrentClaims();
        if (claims == null) {
            return false;
        }
        String tokenPrv = claims.get("prv", String.class);
        return prv.equals(tokenPrv);
    }

    @Override
    public Integer userId() {
        Claims claims = requireCurrentClaims();
        return Integer.parseInt(claims.getSubject());
    }

    @Override
    public void logout() {
        // Stateless JWT logout is handled by the caller's login-record update.
    }

    @Override
    public String jti() {
        Claims claims = requireCurrentClaims();
        return claims.getId();
    }

    @Override
    public Long expired() {
        Claims claims = requireCurrentClaims();
        return Long.parseLong(claims.get("exp", String.class));
    }

    @Override
    public HashMap<String, String> parse(String token) {
        Claims claims = parseClaims(token);
        HashMap<String, String> data = new HashMap<>();
        data.put("jti", claims.getId());
        data.put("exp", claims.get("exp", String.class));
        return data;
    }

    private Claims requireCurrentClaims() {
        Claims claims = parseCurrentClaims();
        if (claims == null) {
            throw new IllegalStateException("token is invalid");
        }
        return claims;
    }

    private Claims parseCurrentClaims() {
        String token = RequestUtil.token();
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return parseClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Failed to parse current token", e);
            return null;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(authConfig.getJwtSecretKey().getBytes(StandardCharsets.UTF_8));
    }
}
