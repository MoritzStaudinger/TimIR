CREATE TABLE IF NOT EXISTS dictionary (
  id bigint AUTO_INCREMENT ,
  term varchar(500),
  unique(term)
);


CREATE TABLE IF NOT EXISTS query_store (
  id bigint AUTO_INCREMENT,
  query varchar(255),
  executed timestamp,
  result_count bigint,
  result_hash varchar(255)
);

CREATE TABLE IF NOT EXISTS doc (
  id bigint AUTO_INCREMENT,
  approximated_length bigint ,
  wiki_id varchar(255),
  length bigint ,
  name varchar(500),
  added timestamp,
  removed timestamp
);

CREATE TABLE IF NOT EXISTS doc_terms (
  term_frequency bigint DEFAULT NULL,
  document_id bigint NOT NULL,
  dictionary_id bigint NOT NULL
);
