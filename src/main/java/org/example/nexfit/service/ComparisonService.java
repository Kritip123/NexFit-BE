package org.example.nexfit.service;

import org.example.nexfit.model.response.ComparisonResponse;

public interface ComparisonService {

    ComparisonResponse getComparison(String userId, Double latitude, Double longitude);
}
