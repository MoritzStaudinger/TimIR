package at.ac.tuwien.lucombonet.Entity;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryStore {

    private Long id;

    private String query;

    private Long result_count;

    private Timestamp executed;

    private String result_hash;

}
