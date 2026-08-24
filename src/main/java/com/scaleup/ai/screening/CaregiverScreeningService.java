package com.scaleup.ai.screening;

import com.scaleup.caregiverlead.CaregiverLeadDetails;

public interface CaregiverScreeningService {

    CaregiverScreeningResult evaluate(
            CaregiverLeadDetails details
    );
}