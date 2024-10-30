package at.ac.tuwien.lucombonet.Endpoint.DTO;

import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SearchResultMeta implements SearchResultMetaInt{
    private String query;
    private Integer resultCount;
    private String resultHash;
    private Timestamp executed;
    List<SearchResultInt> results;;
}
