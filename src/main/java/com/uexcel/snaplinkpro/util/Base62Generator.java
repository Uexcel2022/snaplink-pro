package com.uexcel.snaplinkpro.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class Base62Generator {

    private static final String BASE62 =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final int LENGTH = 7;

    private final SecureRandom random = new SecureRandom();

    public String generate() {

        StringBuilder code = new StringBuilder();

        for (int i = 0; i < LENGTH; i++) {
            code.append(
                    BASE62.charAt(
                            random.nextInt(BASE62.length())
                    )
            );
        }

        return code.toString();
    }
}