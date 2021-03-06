package core;

import cacm.CacmDocument;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

public class Utils {

	public static Query normalizeQuery(String query, String field, Analyzer analyzer) throws ParseException {
		QueryParser queryParser = new QueryParser(Version.LUCENE_29, field, analyzer);
		String pattern = "[!|@|#|$|%|^|&|*|(|)|\\{|\\}|\\-|\\+|;|:|\'|\"|<|>|,|.|/|?|\\\\]";
		Set<String> termSet = new HashSet<String>(Arrays.asList(query.replaceAll(pattern, " ").split("\\s+")));
		StringBuilder terms = new StringBuilder(termSet.size());
		for (String term : termSet) {
			terms.append(' ').append(term);
		}
		return queryParser.parse(terms.toString());
	}

	public static Document convertToLDoc(CacmDocument cacmDocument) {
		Document document = new Document();
		document.add(new Field(CacmDocument.Fields.ABSTRACT,
				       cacmDocument.getAbstractInfo(),
				       Field.Store.YES, Field.Index.NO,
				       Field.TermVector.NO));
		document.add(new Field(CacmDocument.Fields.AUTHORS,
				       cacmDocument.getAuthors().toString(),
				       Field.Store.YES, Field.Index.NO,
				       Field.TermVector.NO));
		document.add(new Field(CacmDocument.Fields.DATE,
				       cacmDocument.getDate(),
				       Field.Store.YES, Field.Index.NO,
				       Field.TermVector.NO));
		document.add(new Field(CacmDocument.Fields.ENTRYDATE,
				       cacmDocument.getEntrydate(),
				       Field.Store.YES, Field.Index.NO,
				       Field.TermVector.NO));
		document.add(new Field(CacmDocument.Fields.ID,
				       String.valueOf(cacmDocument.getId()),
				       Field.Store.YES, Field.Index.NO,
				       Field.TermVector.NO));
		document.add(new Field(CacmDocument.Fields.REFERENCE,
				       cacmDocument.getReferences().toString(),
				       Field.Store.YES, Field.Index.NO,
				       Field.TermVector.NO));
		document.add(new Field(CacmDocument.Fields.KEYWORDS,
				       cacmDocument.getKeywords().toString(),
				       Field.Store.YES, Field.Index.NO,
				       Field.TermVector.NO));
		document.add(new Field(CacmDocument.Fields.TITLE,
				       cacmDocument.getTitle(),
				       Field.Store.YES, Field.Index.ANALYZED,
				       Field.TermVector.YES));
		return document;
	}

	public static float getScore(Directory index, String term) throws CorruptIndexException, IOException {
		float tfidf = 0;
		IndexReader idxreader = IndexReader.open(index, true);
		TermEnum termEnum = idxreader.terms();
		TermDocs termDocs = idxreader.termDocs();
		int docsnum = idxreader.numDocs();
		while (termEnum.next()) {
			if (termEnum.term().text().equalsIgnoreCase(term)) {
				termDocs.seek(termEnum);
				if (termDocs.next()) {
					int tf = termDocs.freq();
					int df = termEnum.docFreq();
					float idf = Similarity.getDefault().idf(df, docsnum);
					tfidf = tf * idf;
					break;
				}
			}
		}
		idxreader.close();
		return tfidf;
	}
}
