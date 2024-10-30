package at.ac.tuwien.lucombonet.Endpoint;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultInt;
import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultMeta;
import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultMetaInt;
import at.ac.tuwien.lucombonet.Entity.BatchEvaluation;
import at.ac.tuwien.lucombonet.Service.ISearchService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
public class SearchEndpoint {

    ISearchService searchService;

    @Autowired
    public SearchEndpoint(ISearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/searchLucene")
    public List<SearchResultInt> searchLucene(@RequestParam String searchstring, @RequestParam Integer resultnumber) {
        try {
            return searchService.searchLuceneContent(searchstring, resultnumber);
        } catch(IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch(ParseException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/searchMariaDB")
    public List<SearchResultInt> searchMariaDB(@RequestParam String searchstring, @RequestParam Integer resultnumber, @RequestParam Integer year) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date date = dateFormat.parse("01/01/" + year);
            long time = date.getTime();
            return searchService.searchMonetDB(searchstring, resultnumber, new Timestamp(time));
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/searchMariaDB-Metadata")
    public SearchResultMetaInt searchMariaDBMetadata(@RequestParam String searchstring, @RequestParam Integer resultnumber, @RequestParam Long timestamp) {
        try {
            Timestamp t = new Timestamp(timestamp);
            System.out.println(t.toString());
            return searchService.searchMonetDBVersionedWithVerificationData(searchstring, new Timestamp(timestamp), resultnumber);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Combined Search with MariaDB and Lucene, if query was already sent use MariaDB otherwise Lucene
     * @param searchstring
     * @param resultnumber
     * @return
     */
    @GetMapping("/search")
    public List<SearchResultInt> search(@RequestParam String searchstring, @RequestParam Integer resultnumber) {
        try {
            return searchService.search(searchstring, resultnumber);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Combined Search with MariaDB and Lucene, if query was already sent use MariaDB otherwise Lucene
     * @param searchstring
     * @param resultnumber
     * @return
     */
    @GetMapping("/searchVersion")
    public List<SearchResultInt> searchVersion(@RequestParam String searchstring, @RequestParam Integer resultnumber, @RequestParam Timestamp version) {
        try {
            return searchService.searchMonetDBVersioned(searchstring, version, resultnumber);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }



    @GetMapping("/searchEvaluation")
    public BatchEvaluation searchEvaluation() {
        try {
            return searchService.batchEvaluations("Wordlists/word_1.txt", 0);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
