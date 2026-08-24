package com.scaleup.ai.qualification;

import com.scaleup.clientlead.ClientLeadDetails;

public interface ClientQualificationService {

    ClientQualificationResult evaluate(
            ClientLeadDetails details
    );
}