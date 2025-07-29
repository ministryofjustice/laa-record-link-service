package uk.gov.justice.record.link.model;

import java.util.List;

public record PagedUserRequest<T>(
    List<T> linkedRequests,
    int pageSize,
    int totalPages,
    long totalItems,
    int currentPage,
    boolean hasNext,
    boolean hasPrevious
) {}
