package at.ac.tuwien.lucombonet.Entity.JSON;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OriginalDocument {
    private String doc_id;
    private String title;
    @JsonProperty("abstract")
    private String abstractString;
    private List<String> authors;
    private String year;
    private String booktitle;
}
