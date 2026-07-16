package com.saas.billing.auth_service.dto.request;

import lombok.Builder;

@Builder

public record PendingRegistrationRequest(

        String firstName,

        String lastName,

        String email,

        String passwordHash

) {}