package spring.data.r2dbc.support;

import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class EncryptString {
    @NotNull
    String value;
}
