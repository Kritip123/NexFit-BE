package org.example.nexfit.entity;

import lombok.*;
import org.example.nexfit.entity.enums.MediaType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "trainer_media")
@CompoundIndex(name = "trainer_order_idx", def = "{'trainerId': 1, 'displayOrder': 1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerMedia {

    @Id
    private String id;

    @Indexed
    private String trainerId;

    private MediaType type; // VIDEO, IMAGE, TRANSFORMATION

    private String s3Key; // S3 object key (null for demo URLs)

    private String mediaUrl; // CDN URL or demo URL

    private String thumbnailUrl; // Thumbnail for videos

    private String title;

    private String description;

    // For transformation type
    private String beforeImageUrl;
    private String afterImageUrl;

    // Media metadata
    private Long fileSizeBytes;
    private String mimeType;
    private Integer durationSeconds; // For videos
    private Integer width;
    private Integer height;

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    private Integer likes = 0;

    @Builder.Default
    private Boolean isDemo = false; // True if using demo/placeholder URLs

    @Builder.Default
    private Boolean isFeatured = false; // True if this is the featured video for reels

    @CreatedDate
    private LocalDateTime createdAt;
}
