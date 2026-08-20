package com.scaleup.publicapi.dto;

import com.scaleup.lead.PreferredContactMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLeadRequest(

        @NotBlank
        @Size(max = 150)
        String campaignKey,

        @NotBlank
        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @NotBlank
        @Size(max = 30)
        String phone,

        @Email
        @Size(max = 255)
        String email,

        @Size(max = 20)
        String zipCode,

        PreferredContactMethod preferredContactMethod,

        @Size(max = 2000)
        String initialRequestNotes,

        @NotNull
        Boolean consentGiven,

        @Valid
        ClientLeadRequest clientDetails,

        @Valid
        CaregiverLeadRequest caregiverDetails

) {

    @AssertTrue(
            message = "Consent must be provided before submitting the form."
    )
    public boolean isConsentAccepted() {
        return Boolean.TRUE.equals(consentGiven);
    }
}