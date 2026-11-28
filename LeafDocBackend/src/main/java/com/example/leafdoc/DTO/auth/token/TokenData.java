package com.example.leafdoc.DTO.auth.token;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(
                value = PendingRegistrationPayload.class,
                name = "REGISTRATION"
        ),
        @JsonSubTypes.Type(
                value = PasswordResetPayload.class,
                name = "PASSWORD_RESET"
        )
})
public interface TokenData {
}
