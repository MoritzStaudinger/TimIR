# TimIR - Time Traveling through IR History

## Motivation
In some settings, the reproducibility of ranked lists is desirable, such as when extracting a subset of an evolving document corpus for research or in domains such as patent retrieval, in medical systematic reviews or when exploring the rich history of Information Retrieval. Currently the only reliable way of achieving reproducibility in Information Retrieval Settings is Boolean Retrieval, which has therefore became the standard retrieval strategy for such tasks.

We showcase a hybrid retrieval strategy which combines a fast traditional sparse retrieval engine (Lucene) for live queries and a slower columnstore retrieval engine (MonetDB) that keeps all historical changes to the term statistics and is able to recreate the document statistics for a given point in time.

To showcase our proposed system in a small real-world example, we indexed all abstracts of the IR Anthology until 2021 in yearly increments, this includes 52780 documents, 63882 terms in the dictionary, and 2701478 total used terms.
## Indexing Strategy
For indexing, we built upon the functionality by Lucene, and are indexing the IR Anthology by Lucene. Once it is processed, the term and document statistics are then mirrored and versioned in MonetDB. Therefore, we applied the RDA Dynamic Data Citation guidelines to our database, and extended each document in the corpora with a validity period. The validity period shows, when a document was added to the index and at which point it was deleted from the corpora. With this information it is then possible to calculate the relevant frequencies for a given point in time.
## Functionality
Our System supports a wide range of different tasks, as analyzing the performance of search queries over time, by travelling through the historical states of the index, recreating the index for a given point in time, and last but not least the citation of queries with variable subsets of data. To showcase that queries are reproducible, we further store at the time of execution a hash of the result list to allow the verification of the recreation of the ranked list.
