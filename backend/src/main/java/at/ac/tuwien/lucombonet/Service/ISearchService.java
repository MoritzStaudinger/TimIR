package at.ac.tuwien.lucombonet.Service;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultInt;
import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultMetaInt;
import at.ac.tuwien.lucombonet.Entity.BatchEvaluation;
import at.ac.tuwien.lucombonet.Entity.Version;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.List;

public interface ISearchService {

    List<SearchResultInt> searchLuceneContent(String query, int resultnumber) throws IOException, ParseException;
    List<SearchResultInt> searchLuceneWikiId(String query) throws IOException, ParseException;

    List<SearchResultInt> searchMonetDB(String query, int resultnumber, Timestamp time) throws ParseException;


    List<SearchResultInt> searchMonetDBVersioned(String query, Timestamp version, int resultnumber) throws ParseException;

    SearchResultMetaInt searchMonetDBVersionedWithVerificationData(String query, Timestamp version, int resultnumber) throws ParseException, NoSuchAlgorithmException;

    List<SearchResultInt> search(String searchstring, Integer resultnumber) throws ParseException, IOException;

    BatchEvaluation batchEvaluations(String file, int batchnumber) throws IOException, ParseException;
}
