package spring.data.jdbc.support;

public interface Encryptor {
    byte[] encrypt(String value);

    String decrypt(byte[] encrypted);
}
