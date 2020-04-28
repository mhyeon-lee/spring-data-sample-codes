package spring.data.r2dbc.issue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Value
@Builder
@AllArgsConstructor
public class IssueContent {
    @NotNull
    String body;

    @NotBlank
    @Size(max = 20)
    String mimeType;
}
