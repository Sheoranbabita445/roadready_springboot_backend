package com.springboot.roadready.dto.request;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record DamageChargeRequestDto(

        @NotNull(message = "Damage charge is required")
        @PositiveOrZero(message = "Damage charge cannot be negative")
        BigDecimal damageCharge,

        @Size(max = 1000, message = "Damage notes cannot exceed 1000 characters")
        String damageNotes

) {}