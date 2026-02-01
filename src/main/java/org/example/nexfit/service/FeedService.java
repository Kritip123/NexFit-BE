package org.example.nexfit.service;

import org.example.nexfit.model.request.FeedRequest;
import org.example.nexfit.model.response.FeedResponse;

public interface FeedService {

    FeedResponse getFeed(String userId, FeedRequest request);
}
