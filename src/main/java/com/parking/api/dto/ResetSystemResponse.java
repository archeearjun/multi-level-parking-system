package com.parking.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after resetting the live parking system")
public record ResetSystemResponse(
        String message,
        DashboardResponse dashboard
) {
}
