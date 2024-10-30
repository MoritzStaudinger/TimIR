package at.ac.tuwien.lucombonet.Service;

import at.ac.tuwien.lucombonet.Entity.QueryStore;

import java.util.List;

public interface IQueryService {
    List<QueryStore> getQueries();
}
