package xyz.playedu.common.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import xyz.playedu.common.service.PasswordService;
import xyz.playedu.common.util.HelperUtil;

@Service
@Slf4j
public class PasswordServiceImpl implements PasswordService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword, String salt) {
        if (StringUtils.isBlank(encodedPassword)) {
            return false;
        }
        if (isLegacyHash(encodedPassword)) {
            return encodedPassword.equalsIgnoreCase(HelperUtil.MD5(rawPassword + StringUtils.defaultString(salt)));
        }
        try {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        } catch (IllegalArgumentException e) {
            log.warn("Unsupported password hash format");
            return false;
        }
    }

    @Override
    public boolean isLegacyHash(String encodedPassword) {
        return !(encodedPassword.startsWith("$2a$")
                || encodedPassword.startsWith("$2b$")
                || encodedPassword.startsWith("$2y$"));
    }
}
