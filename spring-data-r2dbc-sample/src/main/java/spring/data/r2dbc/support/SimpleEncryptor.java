package spring.data.r2dbc.support;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class SimpleEncryptor implements Encryptor {
    private final String transformation = "AES/ECB/PKCS5Padding";
    private final String algorithm = "AES";
    private final byte[] keyBytes = "thisisa128bitkey".getBytes(StandardCharsets.UTF_8);

    @Override
    public byte[] encrypt(String value) {
        if (value == null) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(transformation);
            SecretKey secretKey = new SecretKeySpec(keyBytes, algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new RuntimeException("Encrypt value is failed.", ex);
        }
    }

    @Override
    public String decrypt(byte[] encrypted) {
        if (encrypted == null) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(transformation);
            SecretKey secretKey = new SecretKeySpec(keyBytes, algorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException("Decrypt email is failed.", ex);
        }
    }
}
