package xyz.playedu.common.service;

public interface PasswordService {
    String hash(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword, String salt);

    boolean isLegacyHash(String encodedPassword);
}
