package at.ac.tuwien.lucombonet.Service.Implementation;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultInt;
import at.ac.tuwien.lucombonet.Entity.*;
import at.ac.tuwien.lucombonet.Entity.Dictionary;
import at.ac.tuwien.lucombonet.Entity.JSON.DocumentJson;
import at.ac.tuwien.lucombonet.Entity.JSON.OriginalDocument;
import at.ac.tuwien.lucombonet.Entity.XML.Page;
import at.ac.tuwien.lucombonet.Entity.XML.Wiki;
import at.ac.tuwien.lucombonet.Persistence.IDictionaryDao;
import at.ac.tuwien.lucombonet.Persistence.IDocTermDao;
import at.ac.tuwien.lucombonet.Persistence.IDocumentDao;
import at.ac.tuwien.lucombonet.Persistence.IVersionDao;
import at.ac.tuwien.lucombonet.Service.IFileService;
import at.ac.tuwien.lucombonet.Service.ISearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class FileService implements IFileService {

    SmallFloat smallFloat;
    ISearchService searchService;
    LuceneConfig luceneConfig;
    IVersionDao versionDao;
    IDictionaryDao dictionaryDao;
    IDocumentDao documentDao;
    IDocTermDao docTermDao;
    private static final int batchSize = 20000;
    private int batchcounter=0;
    private Timestamp readingStart;
    private Timestamp luceneIndexingStart;
    private Timestamp luceneIndexingEnd;
    private Timestamp MonetDBIndexingEnd;

    HashSet<Dictionary> terms = new HashSet<>();
    List<DocTermTemp> docTermsTemp = new ArrayList<>();

    @Autowired
    public FileService(
                       SmallFloat smallFloat,
                       ISearchService searchService,
                       LuceneConfig luceneConfig,
                       IVersionDao versionDao,
                       IDictionaryDao dictionaryDao,
                       IDocumentDao documentDao,
                       IDocTermDao docTermDao) {
        this.smallFloat = smallFloat;
        this.searchService = searchService;
        this.luceneConfig = luceneConfig;
        this.versionDao = versionDao;
        this.dictionaryDao = dictionaryDao;
        this.documentDao = documentDao;
        this.docTermDao = docTermDao;
    }

    @Override
    public String createIndex(String docname) throws IOException, ParseException {
        readingStart = new Timestamp(System.currentTimeMillis());
        File f = new File(docname);
        HashSet<String> ids = new HashSet<>();

        if(f.exists() && docname.contains("xml")) {
            XmlMapper xmlMapper = new XmlMapper();
            luceneConfig.open();
            String readContent = Files.readString(Paths.get(docname));
            Wiki wiki = xmlMapper.readValue(readContent.toString(), Wiki.class);
            System.out.println("number of pages: "+wiki.getPages().size());
            luceneIndexingStart = new Timestamp((System.currentTimeMillis()));
            for(Page page: wiki.getPages()) {
                ids.add(indexPageLucene(page));
            }
            luceneConfig.close();
            System.out.println("Index Lucene finished");
            luceneIndexingEnd = new Timestamp(System.currentTimeMillis());
            indexMariaDB(ids, null, true);
            return "successful";
        }
        else {

            List<DocumentJson> parsedDocuments = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                String line;
                ObjectMapper objectMapper = new ObjectMapper();

                // To handle field naming differences like "abstract" -> "abstractText"
                //objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

                while ((line = reader.readLine()) != null) {
                    // Parse each line as a Document object
                    DocumentJson document = objectMapper.readValue(line, DocumentJson.class);
                    parsedDocuments.add(document);
                }

                luceneIndexingStart = new Timestamp((System.currentTimeMillis()));
                for (DocumentJson document : parsedDocuments) {
                    if(document.getOriginal_document().getTitle().length() > 300 || document.getOriginal_document().getTitle().toLowerCase().contains("workshop") || document.getOriginal_document().getTitle().toLowerCase().contains("editor's note")) {
                        continue;
                    }
                    ids.add(indexDocJsonLucene(document));
                }
                luceneConfig.close();
                System.out.println("Index Lucene finished");
                System.out.println("elements in id list: " + ids.size());
                luceneIndexingEnd = new Timestamp(System.currentTimeMillis());
                indexMariaDB(ids, parsedDocuments, false);
                return "successful";
            }
        }
    }



    private String indexPageLucene(Page page) throws IOException, ParseException {
        //Check if Page is already in the Index, then only flag as delete and save new document
        if(DirectoryReader.indexExists(luceneConfig.getIndexDirectory())) {
            luceneConfig.setReader(DirectoryReader.open(luceneConfig.getIndexDirectory()));
            //System.out.println("trying to delete " + page.getTitle() + " - " + page.getTitle().hashCode());

            //QueryParser q = new QueryParser("id", luceneConfig.getAnalyzer());
            //luceneConfig.getWriter().deleteDocuments(q.parse(QueryParser.escape(page.getId()+"")));
        }
        Document document = getDocumentLucene(page);
        //System.out.println("Add " + document.getField("title").stringValue());
        luceneConfig.getWriter().updateDocument(new Term("id", page.getId()+""),document);
        return document.getField("id").stringValue();
    }

    private String indexDocJsonLucene(DocumentJson doc) throws IOException, ParseException {
        //Check if Page is already in the Index, then only flag as delete and save new document
        if(DirectoryReader.indexExists(luceneConfig.getIndexDirectory())) {
            luceneConfig.setReader(DirectoryReader.open(luceneConfig.getIndexDirectory()));
            //System.out.println("trying to delete " + page.getTitle() + " - " + page.getTitle().hashCode());

            //QueryParser q = new QueryParser("id", luceneConfig.getAnalyzer());
            //luceneConfig.getWriter().deleteDocuments(q.parse(QueryParser.escape(page.getId()+"")));
        }
        Document document = getDocumentIRAnthologyLucene(doc);
        //System.out.println("Add " + document.getField("title").stringValue());
        luceneConfig.getWriter().updateDocument(new Term("id", doc.getDocno()),document);
        return document.getField("id").stringValue();
    }

    private Document getDocumentIRAnthologyLucene(DocumentJson doc) {
        Document document = new Document();
        FieldType ft = new FieldType(TextField.TYPE_STORED);
        ft.setStoreTermVectors(true);
        ft.setStored(true);
        document.add(new Field("content", doc.getText(), ft));
        document.add(new Field("title", doc.getOriginal_document().getTitle(), ft));
        document.add(new Field("id", doc.getDocno(), ft));
        return document;
    }

    private Document getDocumentLucene(Page page) {
        Document document = new Document();
        FieldType ft = new FieldType(TextField.TYPE_STORED);
        ft.setStoreTermVectors(true);
        ft.setStored(true);
        document.add(new Field("content", page.getRevision().getContent(), ft));
        document.add(new Field("title", page.getTitle(), ft));
        document.add(new Field("id", ""+page.getId(), ft));
        return document;
    }

    /**
     * Initialize the index at the first start
     * @throws IOException
     */
    private void indexMariaDB(HashSet<String> ids, List<DocumentJson> docs, boolean wiki) throws IOException {
        luceneConfig.setReader(DirectoryReader.open(luceneConfig.getIndexDirectory()));
        luceneConfig.setSearcher(new IndexSearcher(luceneConfig.getReader()));
        System.out.println("Elements indexed in Lucene " +luceneConfig.getReader().numDocs());
        Timestamp t = new Timestamp(System.currentTimeMillis());

        BufferedWriter writer = new BufferedWriter(new FileWriter("data/doc.txt"));
        Long index = documentDao.getMaxId();
        int count = 0;
        for(int i = 0; i < luceneConfig.getReader().maxDoc(); i++) {
            Document doc = luceneConfig.getReader().document(i);
            if(ids.contains(doc.getField("id").stringValue())) {
                Terms termVector = luceneConfig.getSearcher().getIndexReader().getTermVector(i, "content");
                Long length = 0L;
                if(termVector != null) {
                    length = termVector.getSumTotalTermFreq();
                }
                Long approxLength = (long) smallFloat.byte4ToInt(smallFloat.intToByte4(Integer.parseInt(length.toString().trim())));
                String title = doc.getField("title").stringValue();
                String wiki_id = doc.getField("id").stringValue();
                Doc d = documentDao.findByWikiId(wiki_id);
                if (!wiki) {
                    String year = docs.stream().filter(documentJson -> documentJson.getDocno().equals(wiki_id)).findFirst().get().getOriginal_document().getYear();
                    t = Timestamp.valueOf(year + "-01-01 00:00:00");
                    ;
                }
                if (d != null) {
                    documentDao.markAsDeleted(d, t);
                }
                Doc dc = Doc.builder().id(++index).name(title).length(length).approximatedLength(approxLength).added(t).wiki_id(wiki_id).build();
                writer.write((index) + "|"+dc.getApproximatedLength() + "|"+ dc.getWiki_id() +"|"+ dc.getLength() +"|"+ dc.getName().replace("|", "") +"|" +dc.getAdded() + "|" +null +"\n");
                if (termVector != null) {
                    addToBatch(dc, termVector);
                }
                count++;
                if(count == batchSize) {
                    writer.close();
                    System.out.println("Batch " + batchcounter++ + " written");
                    File f = new File("data/doc.txt");
                    String filename = f.getAbsolutePath().replace("\\", "\\\\");
                    documentDao.saveAll("\'"+filename+"\'");
                    //f.delete();
                    addTermsToDB();
                    count = 0;
                    writer = new BufferedWriter(new FileWriter("data/doc.txt"));
                }
            }
        }
        //add the last elements;
        writer.close();
        File f = new File("data/doc.txt");
        String filename = f.getAbsolutePath().replace("\\", "\\\\");
        documentDao.saveAll("\'"+filename+"\'");
        //f.delete();
        addTermsToDB();
        MonetDBIndexingEnd = new Timestamp(System.currentTimeMillis());

        StringBuilder sb = new StringBuilder();
        sb.append("reading start: " + readingStart.toLocalDateTime().toString() +"\n");
        sb.append("lucene start: " + luceneIndexingStart.toLocalDateTime().toString()+"\n");
        sb.append("lucene end: " + luceneIndexingEnd.toLocalDateTime().toString()+"\n");
        sb.append("lucene time: " + (luceneIndexingEnd.getTime() - luceneIndexingStart.getTime())+"\n");
        sb.append("monetdb end: " + MonetDBIndexingEnd.toLocalDateTime().toString()+"\n");
        sb.append("monetdb time: " + (MonetDBIndexingEnd.getTime() - luceneIndexingEnd.getTime())+"\n\n");
        //Files.write(Paths.get("data/indexing.txt"), sb.toString().getBytes(), StandardOpenOption.APPEND);
    }

    private void addToBatch(Doc dc, Terms termVector) throws IOException {
        BytesRef term = null;
        TermsEnum  itr = termVector.iterator();
        while((term = itr.next()) != null) {
            terms.add(Dictionary.builder().term(term.utf8ToString()).build());
            docTermsTemp.add(DocTermTemp.builder().term(term.utf8ToString()).document(dc).termFrequency(itr.totalTermFreq()).build());
        }
    }

    private void addTermsToDB() throws IOException {
        Set<String> dics = dictionaryDao.getAll().parallelStream().map(dictionary -> dictionary.getTerm()).collect(Collectors.toSet());
        List<Dictionary> dicUpdated = terms.parallelStream()
                .filter(d ->!dics.contains(d.getTerm()))
                .collect(Collectors.toList());
        if(dicUpdated.size() > 1) {
            BufferedWriter writer = new BufferedWriter(new FileWriter("data/dictionaries.txt"));
            Long i = dictionaryDao.getMaxId();

            for(Dictionary dictionary : dicUpdated) {
                try {
                    writer.write((++i) + "|"+dictionary.getTerm()+"\n");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writer.close();
            File f = new File("data/dictionaries.txt");
            String filename = f.getAbsolutePath().replace("\\", "\\\\");
            dictionaryDao.saveAll("\'"+filename+"\'");
            f.delete();
        }
        HashMap<String, Dictionary> dicMap = dictionaryDao.getAllMap();

        BufferedWriter writer = new BufferedWriter(new FileWriter("data/docterms.txt"));
        docTermsTemp.parallelStream().forEach(d -> {
            if(dicMap.get(d.getTerm()) != null ) {
                try {
                    writer.write(d.getTermFrequency()  +"|"+d.getDocument().getId()+ "|" +dicMap.get(d.getTerm()).getId() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        writer.close();
        File f = new File("data/docterms.txt");
        String filename = f.getAbsolutePath().replace("\\", "\\\\");
        docTermDao.saveAll("\'"+filename+"\'");
        f.delete();
        terms.clear();
        docTermsTemp.clear();
    }



}
