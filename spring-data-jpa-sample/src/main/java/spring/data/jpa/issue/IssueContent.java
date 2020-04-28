package spring.data.jpa.issue;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "id")
@ToString
public class IssueContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Lob
    @Column(nullable = false, updatable = false)
    private String body;

    @NotBlank
    @Size(max = 20)
    @Column(length = 20, nullable = false, updatable = false)
    private String mimeType;

    public IssueContent(String body, String mimeType) {
        this.body = body;
        this.mimeType = mimeType;
    }
}
