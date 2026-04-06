package com.threlease.base.common.utils.crypto;

import org.springframework.stereotype.Component;

@Component
public class Base64Component {
    public String base64Encode(String str) {
        byte[] encodedBytes = java.util.Base64.getEncoder().encode(str.getBytes());
        return new String(encodedBytes);
    }

    public String base64Decode(String str) {
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(str.getBytes());
        return new String(decodedBytes);
    }
}
