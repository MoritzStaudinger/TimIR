package at.ac.tuwien.lucombonet.Entity.JSON;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class DocumentJson {
    private String docno;
    private String text;
    private OriginalDocument original_document;
}

