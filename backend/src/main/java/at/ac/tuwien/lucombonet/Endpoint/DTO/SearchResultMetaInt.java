package at.ac.tuwien.lucombonet.Endpoint.DTO;

import java.util.List;
import java.sql.Timestamp;

public interface SearchResultMetaInt {

    String query = null;
    String getQuery();
    String resultHash = null;
    String getResultHash();
    Integer resultCount = 0;
    Integer getResultCount();
    Timestamp executed = null;
    Timestamp getExecuted();
    List<SearchResultInt> results = null;
    List<SearchResultInt> getResults();

}
