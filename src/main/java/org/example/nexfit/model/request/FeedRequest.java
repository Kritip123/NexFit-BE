package org.example.nexfit.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedRequest {

    private Double latitude;
    private Double longitude;
    private String sessionId;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;

    private Long seed; // Random seed for consistent shuffle across pages; null = generate new
}
