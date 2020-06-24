package com.company.imagebook.entities.image;

import java.util.stream.Stream;
import org.springframework.data.jpa.domain.Specification;

public final class ImageSpecifications {

  private ImageSpecifications() {
    throw new UnsupportedOperationException();
  }

  public static Specification<Image> descriptionContains(final String description) {
    if (description == null) {
      return null;
    }
    if (description.contains(" ")) {
      //noinspection ConstantConditions
      return Stream.of(description.split("\\s+"))
          .map(ImageSpecifications::descriptionContains)
          .reduce(Specification::or)
          .orElse(null);
    }
    return (root, query, builder) -> builder.or(
        builder.like(root.get("description"), description + " %"),
        builder.like(root.get("description"), "% " + description),
        builder.like(root.get("description"), "% " + description + " %"));
  }

  public static Specification<Image> hasType(final ImageType type) {
    return type == null ? null : (root, query, builder) ->
        builder.equal(root.get("imageType"), type);
  }

  public static Specification<Image> sizeIs(final Integer size) {
    return size == null ? null : (root, query, builder) ->
        builder.equal(root.get("contentSize"), size);
  }

  public static Specification<Image> hasMinSize(final Integer size) {
    return size == null ? null : (root, query, builder) ->
        builder.greaterThanOrEqualTo(root.get("contentSize"), size);
  }

  public static Specification<Image> hasMaxSize(final Integer size) {
    return size == null ? null : (root, query, builder) ->
        builder.lessThanOrEqualTo(root.get("contentSize"), size);
  }
}
