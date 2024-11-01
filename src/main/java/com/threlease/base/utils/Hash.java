package com.threlease.base.utils;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;

public class Hash {
    public static String generateSHA512(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Handle NoSuchAlgorithmException (unavailable algorithm)
            e.printStackTrace();
            return null;
        }
    }

    public static String generateSHA256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Handle NoSuchAlgorithmException (unavailable algorithm)
            e.printStackTrace();
            return null;
        }
    }

    public static String hexToBinary2(String hex) {
        HashMap<Character, String> lookup = new HashMap<>();

        lookup.put('0', "0000");
        lookup.put('1', "0001");
        lookup.put('2', "0010");
        lookup.put('3', "0011");
        lookup.put('4', "0100");
        lookup.put('5', "0101");
        lookup.put('6', "0110");
        lookup.put('7', "0111");
        lookup.put('8', "1000");
        lookup.put('9', "1001");
        lookup.put('a', "1010");
        lookup.put('b', "1011");
        lookup.put('c', "1100");
        lookup.put('d', "1101");
        lookup.put('e', "1110");
        lookup.put('f', "1111");
        lookup.put('A', "1010");
        lookup.put('B', "1011");
        lookup.put('C', "1100");
        lookup.put('D', "1101");
        lookup.put('E', "1110");
        lookup.put('F', "1111");

        StringBuilder ret = new StringBuilder();
        for (int i = 0, len = hex.length(); i < len; i++) {
            ret.append(lookup.get(hex.charAt(i)));
        }
        return ret.toString();
    }

    public static String hexToBinary(String hex) {
        BigInteger num = new BigInteger(hex, 16);
        return num.toString(2);
    }

    public static String base64Encode(String str) {
        byte[] encodedBytes = Base64.getEncoder().encode(str.getBytes());
        return new String(encodedBytes);
    }

    public static String base64Decode(String str) {
        byte[] decodedBytes = Base64.getDecoder().decode(str.getBytes());
        return new String(decodedBytes);
    }
}
