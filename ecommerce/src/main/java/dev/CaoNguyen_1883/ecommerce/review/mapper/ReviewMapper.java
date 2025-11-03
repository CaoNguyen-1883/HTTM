package dev.CaoNguyen_1883.ecommerce.review.mapper;

import dev.CaoNguyen_1883.ecommerce.review.dto.ReviewDto;
import dev.CaoNguyen_1883.ecommerce.review.dto.ReviewImageDto;
import dev.CaoNguyen_1883.ecommerce.review.entity.Review;
import dev.CaoNguyen_1883.ecommerce.review.entity.ReviewImage;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ReviewMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "userAvatar", source = "user.avatarUrl")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "images", source = "images")
    @Mapping(target = "totalVotes", expression = "java(review.getTotalVotes())")
    @Mapping(target = "helpfulPercentage", expression = "java(review.getHelpfulPercentage())")
    @Mapping(target = "repliedByName", source = "repliedBy.fullName")
    @Mapping(target = "userVote", ignore = true)  // Set manually in service
    public abstract ReviewDto toDto(Review review);

    public abstract List<ReviewDto> toDtoList(List<Review> reviews);

    @Mapping(target = "imageUrl", source = "imageUrl")
    @Mapping(target = "displayOrder", source = "displayOrder")
    public abstract ReviewImageDto toImageDto(ReviewImage image);

    public abstract List<ReviewImageDto> toImageDtoList(List<ReviewImage> images);
}