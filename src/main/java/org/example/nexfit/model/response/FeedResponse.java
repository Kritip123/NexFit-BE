package org.example.nexfit.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedResponse {

    private List<TrainerFeedCard> trainers;
    private String sessionId;
    private Long seed; // Random seed used; pass back for consistent pagination
    private int page;
    private int limit;
    private boolean hasMore;
    private int totalCount;
    private int totalPages;
}
