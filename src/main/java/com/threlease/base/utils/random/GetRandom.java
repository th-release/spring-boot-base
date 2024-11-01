package com.threlease.base.utils.random;

import java.math.BigInteger;
import java.security.SecureRandom;

public class GetRandom {
    private static final String NUMERIC = "0123456789";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHANUMERIC = NUMERIC + LOWERCASE + UPPERCASE;

    private static String getCharacters(RandomType type) {
        return switch (type) {
            case NUMBER -> NUMERIC;
            case LOWERCASE -> LOWERCASE;
            case UPPERCASE -> UPPERCASE;
            case ALL -> ALPHANUMERIC;
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };
    }

    public static String run(RandomType type, int length) {
        StringBuilder result = new StringBuilder();
        String characters = getCharacters(type);
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            result.append(characters.charAt(randomIndex));
        }

        return result.toString();
    }

    public static String generateRandomHexString(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        BigInteger num = new BigInteger(1, bytes);
        return String.format("%0" + (length << 1) + "x", num);
    }
}
