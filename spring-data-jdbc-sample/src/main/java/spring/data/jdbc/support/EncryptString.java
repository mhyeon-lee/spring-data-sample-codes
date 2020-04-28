package spring.data.jdbc.support;

import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class EncryptString {
    @NotNull
    String value;
}
