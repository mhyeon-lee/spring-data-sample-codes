package spring.data.r2dbc.support;

public interface Encryptor {
    byte[] encrypt(String value);

    String decrypt(byte[] encrypted);
}
