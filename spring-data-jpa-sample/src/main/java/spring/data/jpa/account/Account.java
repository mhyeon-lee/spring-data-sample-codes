package spring.data.jpa.account;

import lombok.*;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(indexes = {@Index(columnList = "loginId", unique = true)})
@SQLDelete(sql = "UPDATE account SET state = 'DELETED' WHERE id = ?", check = ResultCheckStyle.COUNT)
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
public class Account {
    @Id
    private UUID id;

    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false, updatable = false)
    private String loginId;

    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false, updatable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private AccountState state;

    @NotBlank
    @Convert(converter = EmailEncryptor.class)
    @Column(nullable = false, columnDefinition = "LONGVARBINARY")
    private String email;

    @NotNull
    @PastOrPresent
    @Builder.Default
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @PostRemove
    void markDeleted() {
        this.state = AccountState.DELETED;
    }

    static class EmailEncryptor implements AttributeConverter<String, byte[]> {
        private final String transformation = "AES/ECB/PKCS5Padding";
        private final String algorithm = "AES";
        private final byte[] keyBytes = "thisisa128bitkey".getBytes(StandardCharsets.UTF_8);

        @Override
        public byte[] convertToDatabaseColumn(String attribute) {
            if (attribute == null) {
                return null;
            }

            try {
                Cipher cipher = Cipher.getInstance(transformation);
                SecretKey secretKey = new SecretKeySpec(keyBytes, algorithm);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                return cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            } catch (Exception ex) {
                throw new RuntimeException("Encrypt email is failed.", ex);
            }
        }

        @Override
        public String convertToEntityAttribute(byte[] dbData) {
            if (dbData == null) {
                return null;
            }

            try {
                Cipher cipher = Cipher.getInstance(transformation);
                SecretKey secretKey = new SecretKeySpec(keyBytes, algorithm);
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                byte[] decrypted = cipher.doFinal(dbData);
                return new String(decrypted, StandardCharsets.UTF_8);
            } catch (Exception ex) {
                throw new RuntimeException("Decrypt email is failed.", ex);
            }
        }
    }
}
