package genum.shared.DTO.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int totalPages,
        long totalElements,
        boolean last,
        int size,
        int number,
        Sort sort,
        int numberOfElements,
        boolean first,
        boolean empty
) {


    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isLast(),
                page.getSize(),
                page.getNumber(),
                new Sort(page.getSort().isEmpty(), page.getSort().isSorted()),
                page.getNumberOfElements(),
                page.isFirst(),
                page.isEmpty()
        );
    }
}
