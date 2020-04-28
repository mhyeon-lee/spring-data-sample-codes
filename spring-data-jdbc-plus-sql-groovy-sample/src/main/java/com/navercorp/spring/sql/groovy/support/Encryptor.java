package com.navercorp.spring.sql.groovy.support;

public interface Encryptor {
    byte[] encrypt(String value);

    String decrypt(byte[] encrypted);
}
