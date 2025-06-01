package barojob.server.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SliceResponseDto<T> {
    private List<T> content;
    private int currentPage;
    private int size;
    private boolean first;
    private boolean last;
    private boolean hasNext;

    public static <T> SliceResponseDto<T> from(Slice<T> slice) {
        return SliceResponseDto.<T>builder()
                .content(slice.getContent())
                .currentPage(slice.getNumber())
                .size(slice.getSize())
                .first(slice.isFirst())
                .last(slice.isLast())
                .hasNext(slice.hasNext())
                .build();
    }
}
