package com.scaleup.integration.agencycrm;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/agencies")
public class AgencyHighLevelConnectionController {

    private final AgencyHighLevelConnectionService
            connectionService;

    public AgencyHighLevelConnectionController(
            AgencyHighLevelConnectionService connectionService
    ) {

        this.connectionService =
                connectionService;
    }

    @PostMapping(
            "/{agencyPublicId}/highlevel-connection"
    )
    public ResponseEntity<Map<String, Object>>
    configureHighLevelConnection(

            @PathVariable
            UUID agencyPublicId,

            @Valid
            @RequestBody
            AgencyHighLevelConnectionRequest request
    ) {

        AgencyHighLevelConnection connection =
                connectionService
                        .configureConnection(
                                agencyPublicId,
                                request.locationId(),
                                request.accessToken()
                        );

        /*
         * Never return the access token or encrypted
         * credential in an API response.
         */
        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "agencyId", agencyPublicId,
                        "locationId", connection.getLocationId(),
                        "authType", connection.getAuthType().name(),
                        "connectionStatus",
                        connection
                                .getConnectionStatus()
                                .name()
                )
        );
    }
}