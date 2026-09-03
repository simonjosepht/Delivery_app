package com.simon.application.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal {

    private final Long id;
    private final String email;

    @Override
    public String toString() {
        return email;
    }
}
