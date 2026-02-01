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
public class TrainingCategoryResponse {

    private String id;
    private String name;
    private String icon;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private List<SubcategoryInfo> subcategories;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubcategoryInfo {
        private String id;
        private String name;
        private String description;
        private String icon;
    }
}
