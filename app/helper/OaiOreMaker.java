/*
 * Copyright 2014 hbz NRW (http://www.hbz-nrw.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.ObjectType;

import org.apache.commons.io.IOUtils;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicWriterSettings;
import org.openrdf.rio.helpers.JSONLDMode;
import org.openrdf.rio.helpers.JSONLDSettings;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;

import play.Play;
import archive.datatypes.Node;
import archive.datatypes.Transformer;

import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JSONUtils;

/**
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
@SuppressWarnings("serial")
public class OaiOreMaker {

    final static Logger logger = LoggerFactory.getLogger(OaiOreMaker.class);
    String server = null;
    String uriPrefix = null;

    Node node = null;
    String dcNamespace = "http://purl.org/dc/elements/1.1/";
    String dctermsNamespace = "http://purl.org/dc/terms/";
    String foafNamespace = "http://xmlns.com/foaf/0.1/";
    String oreNamespace = "http://www.openarchives.org/ore/terms/";
    String rdfNamespace = " http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    String rdfsNamespace = "http://www.w3.org/2000/01/rdf-schema#";
    String regalNamespace = "http://hbz-nrw.de/regal#";
    String fpNamespace = "http://downlode.org/Code/RDF/File_Properties/schema#";
    String wnNamespace = "http://xmlns.com/wordnet/1.6/";
    String hydraNamespace = "http://purl.org/hydra/core#";

    RepositoryConnection con = null;

    @SuppressWarnings("javadoc")
    public OaiOreMaker(Node node, String server, String uriPrefix) {
	this.server = server;
	this.uriPrefix = "/resource/";
	this.node = node;
	try {
	    con = createRdfRepository();
	} catch (RepositoryException e) {
	    throw new CreateRepositoryException(e);
	}
    }

    /**
     * @param format
     *            application/rdf+xml text/plain application/json
     * @param parents
     *            all parents of the pid
     * @param children
     *            all children of the pid
     * @param transformers
     *            transformers of the object
     * @return a oai_ore resource map
     */
    public String getReM(String format, List<String> parents,
	    List<String> children, List<Transformer> transformers) {

	String result = null;
	addDescriptiveMetadata();
	addStructuralData(parents, children, transformers);
	result = write(format);
	closeRdfRepository();
	return result;

    }

    private void addDescriptiveMetadata() {
	try {
	    con.add(new StringReader(node.getMetadata()), node.getPID(),
		    RDFFormat.N3);
	} catch (Exception e) {
	    logger.debug("", e);
	}
    }

    private String write(String format) {
	try {
	    if ("text/html".equals(format)) {
		return getHtml();
	    } else if ("application/json+compact".equals(format)) {
		InputStream contextUrl = Play.application().resourceAsStream(
			"edoweb-resources.json");
		StringWriter out = new StringWriter();
		RDFWriter writer = null;
		writer = configureWriter("application/json", out, writer);
		String jsonString = write(out, writer);
		Object json = JSONUtils.fromString(jsonString);
		@SuppressWarnings("rawtypes")
		Map context = (Map) JSONUtils.fromInputStream(contextUrl);
		JsonLdOptions options = new JsonLdOptions();
		Map<String, Object> normalized = (Map<String, Object>) JsonLdProcessor
			.compact(json, context, options);
		normalized.remove("@context");
		normalized.put("@context", server
			+ "/public/edoweb-resources.json");

		return JSONUtils.toPrettyString(normalized);
	    }
	    StringWriter out = new StringWriter();
	    RDFWriter writer = null;
	    writer = configureWriter(format, out, writer);
	    return write(out, writer);
	} catch (Exception e) {
	    throw new WriteRdfException(e);
	}
    }

    private String write(StringWriter out, RDFWriter writer)
	    throws RepositoryException {
	String result = null;
	try {

	    writer.startRDF();
	    RepositoryResult<Statement> statements = con.getStatements(null,
		    null, null, false);

	    while (statements.hasNext()) {
		Statement statement = statements.next();
		writer.handleStatement(statement);
	    }
	    writer.endRDF();
	    out.flush();
	    result = out.toString();

	} catch (RDFHandlerException e) {
	    logger.error(e.getMessage());
	} finally {
	    try {
		out.close();
	    } catch (IOException e) {
		logger.error(e.getMessage());
	    }
	}

	return result;
    }

    private RDFWriter configureWriter(String format, StringWriter out,
	    RDFWriter writer) {
	if ("application/rdf+xml".equals(format)) {
	    writer = Rio.createWriter(RDFFormat.RDFXML, out);
	} else if ("text/plain".equals(format)) {
	    writer = Rio.createWriter(RDFFormat.NTRIPLES, out);
	} else if ("application/json".equals(format)) {
	    writer = Rio.createWriter(RDFFormat.JSONLD, out);
	    writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE,
		    JSONLDMode.EXPAND);
	    writer.getWriterConfig()
		    .set(BasicWriterSettings.PRETTY_PRINT, true);
	} else {
	    throw new HttpArchiveException(406, format + " is not supported");
	}
	return writer;
    }

    private String getHtml() throws RDFHandlerException, RepositoryException {
	StringWriter out = new StringWriter();
	RDFWriter writer = null;
	String result = null;
	writer = Rio.createWriter(RDFFormat.NTRIPLES, out);
	writer.startRDF();
	RepositoryResult<Statement> statements = con.getStatements(null, null,
		null, false);
	while (statements.hasNext()) {
	    Statement statement = statements.next();
	    writer.handleStatement(statement);
	}
	writer.endRDF();
	result = out.toString();
	return getHtml(result, node.getMimeType(), node.getPID());
    }

    private RepositoryConnection createRdfRepository()
	    throws RepositoryException {
	RepositoryConnection mycon = null;
	SailRepository myRepository = new SailRepository(new MemoryStore());
	myRepository.initialize();
	mycon = myRepository.getConnection();
	return mycon;
    }

    private void closeRdfRepository() {
	try {
	    if (con != null)
		con.close();
	} catch (Exception e) {
	    throw new CreateRepositoryException(e);
	}
    }

    private void addStructuralData(List<String> parents, List<String> children,
	    List<Transformer> transformers) {
	try {
	    String pid = node.getPID();
	    Date lastModified = node.getLastModified();
	    Date creationDate = node.getCreationDate();
	    // Graph remGraph = new org.openrdf.model.impl.GraphImpl();
	    ValueFactory f = con.getValueFactory();

	    // Things
	    URI aggregation = f.createURI(/* uriPrefix + */pid);
	    URI rem = f.createURI(/* uriPrefix + */pid + ".rdf");
	    URI data = f.createURI(aggregation.stringValue() + "/data");
	    URI fulltext = f.createURI(aggregation.stringValue() + "/fulltext");
	    Literal cType = f.createLiteral(node.getContentType());
	    Literal lastTimeModified = f.createLiteral(lastModified);
	    Literal firstTimeCreated = f.createLiteral(creationDate);
	    String mime = node.getMimeType();
	    String label = node.getFileLabel();
	    String accessScheme = node.getAccessScheme();
	    String fileSize = null;
	    BigInteger fs = node.getFileSize();
	    if (fs != null)
		fileSize = fs.toString();
	    String fileChecksum = node.getChecksum();

	    // Predicates
	    // ore
	    URI describes = f.createURI(oreNamespace, "describes");
	    URI isDescribedBy = f.createURI(oreNamespace, "isDescribedBy");
	    URI aggregates = f.createURI(oreNamespace, "aggregates");
	    URI isAggregatedBy = f.createURI(oreNamespace, "isAggregatedBy");
	    URI similarTo = f.createURI(oreNamespace, "similarTo");
	    // dc
	    URI isPartOf = f.createURI(dctermsNamespace, "isPartOf");
	    URI hasPart = f.createURI(dctermsNamespace, "hasPart");
	    URI modified = f.createURI(dctermsNamespace, "modified");
	    URI created = f.createURI(dctermsNamespace, "created");
	    URI dcFormat = f.createURI(dctermsNamespace, "format");
	    URI dcHasFormat = f.createURI(dctermsNamespace, "hasFormat");
	    // rdfs
	    URI rdfsLabel = f.createURI(rdfsNamespace, "label");
	    URI rdfsType = f.createURI(rdfsNamespace, "type");
	    // regal
	    URI contentType = f.createURI(regalNamespace, "contentType");
	    URI hasData = f.createURI(regalNamespace, "hasData");
	    URI hasTransformer = f.createURI(regalNamespace, "hasTransformer");
	    URI hasAccessScheme = f.createURI(regalNamespace, "accessScheme");
	    // FileProperties
	    URI fpSize = f.createURI(fpNamespace, "size");
	    BNode theChecksumBlankNode = f.createBNode();
	    URI fpChecksum = f.createURI(fpNamespace, "checksum");
	    URI fpChecksumType = f.createURI(fpNamespace, "Checksum");
	    URI fpChecksumGenerator = f.createURI(fpNamespace, "generator");
	    URI fpChecksumAlgo = f.createURI(wnNamespace, "Algorithm");
	    URI md5Uri = f.createURI("http://en.wikipedia.org/wiki/MD5");
	    URI fpChecksumValue = f.createURI(fpNamespace, "checksumValue");

	    // Statements
	    if (mime != null && !mime.isEmpty()) {
		Literal dataMime = f.createLiteral(mime);
		con.add(data, dcFormat, dataMime);
		con.add(aggregation, aggregates, data);
		con.add(aggregation, hasData, data);
		if (dataMime.toString().compareTo("application/pdf") == 0) {
		    con.add(aggregation, aggregates, fulltext);
		    con.add(data, dcHasFormat, fulltext);
		}
	    }

	    if (accessScheme != null && !accessScheme.isEmpty()) {
		Literal a = f.createLiteral(accessScheme);
		con.add(aggregation, hasAccessScheme, a);
	    }

	    if (fileSize != null && !fileSize.isEmpty()) {
		Literal dataSize = f.createLiteral(fileSize);
		con.add(data, fpSize, dataSize);
	    }

	    if (fileChecksum != null && !fileChecksum.isEmpty()) {
		Literal dataChecksum = f.createLiteral(fileChecksum);
		con.add(theChecksumBlankNode, rdfsType, fpChecksumType);
		con.add(theChecksumBlankNode, fpChecksumGenerator, md5Uri);
		con.add(md5Uri, rdfsType, fpChecksumAlgo);
		con.add(theChecksumBlankNode, fpChecksumValue, dataChecksum);
		con.add(data, fpChecksum, theChecksumBlankNode);
	    }

	    if (label != null && !label.isEmpty()) {
		Literal labelLiteral = f.createLiteral(label);
		con.add(data, rdfsLabel, labelLiteral);
	    }

	    String str = getOriginalUri(pid);
	    if (str != null && !str.isEmpty()
		    && !cType.stringValue().equals(ObjectType.file.toString())) {
		URI originalObject = f.createURI(str);
		con.add(aggregation, similarTo, originalObject);

	    }

	    if (transformers != null && transformers.size() > 0) {
		for (Transformer t : transformers) {
		    Literal transformerId = f.createLiteral(t.getId());
		    con.add(aggregation, hasTransformer, transformerId);
		}
	    }

	    URI fedoraObject = f.createURI(server + "/fedora/objects/" + pid);

	    con.add(rem, describes, aggregation);
	    con.add(rem, modified, lastTimeModified);
	    con.add(rem, created, firstTimeCreated);

	    con.add(aggregation, isDescribedBy, rem);

	    con.add(aggregation, similarTo, fedoraObject);
	    con.add(aggregation, contentType, cType);

	    for (String relPid : parents) {
		URI relUrl = f.createURI(relPid);
		con.add(aggregation, isAggregatedBy, relUrl);
		con.add(aggregation, isPartOf, relUrl);
	    }

	    for (String relPid : children) {
		URI relUrl = f.createURI(relPid);
		con.add(aggregation, aggregates, relUrl);
		con.add(aggregation, hasPart, relUrl);

	    }
	} catch (Exception e) {
	    logger.debug("", e);
	}
    }

    private String getOriginalUri(String pid) {
	String pidWithoutNamespace = pid.substring(pid.indexOf(':') + 1);
	String originalUri = null;
	if (pid.contains("edoweb") || pid.contains("ellinet")) {
	    if (pid.length() <= 17) {
		originalUri = "http://klio.hbz-nrw.de:1801/webclient/MetadataManager?pid="
			+ pidWithoutNamespace;

	    }
	}
	if (pid.contains("dipp")) {
	    originalUri = "http://193.30.112.23:9280/fedora/get/" + pid
		    + "/QDC";

	}
	if (pid.contains("ubm")) {
	    originalUri = "http://ubm.opus.hbz-nrw.de/frontdoor.php?source_opus="
		    + pidWithoutNamespace + "&la=de";

	}
	if (pid.contains("fhdd")) {
	    originalUri = "http://fhdd.opus.hbz-nrw.de/frontdoor.php?source_opus="
		    + pidWithoutNamespace + "&la=de";

	}
	if (pid.contains("kola")) {
	    originalUri = "http://kola.opus.hbz-nrw.de/frontdoor.php?source_opus="
		    + pidWithoutNamespace + "&la=de";

	}
	return originalUri;
    }

    private String getHtml(String rdf, String mime, String pid) {

	String result = "";
	RepositoryConnection mycon = null;
	try {
	    java.net.URL fileLocation = Thread.currentThread()
		    .getContextClassLoader().getResource("html.html");

	    StringWriter writer = new StringWriter();
	    IOUtils.copy(fileLocation.openStream(), writer);
	    String data = writer.toString();

	    ST st = new ST(data, '$', '$');
	    st.add("serverRoot", "");

	    if (mime != null) {
		String dataLink = uriPrefix + pid + "/data";
		String logoLink = "";
		if (mime.compareTo("application/pdf") == 0) {
		    logoLink = "pdflogo.svg";
		} else if (mime.compareTo("application/zip") == 0) {
		    logoLink = "zip.png";
		} else {
		    logoLink = "data.png";
		}
		st.add("data", "<tr><td class=\"textlink\"><a	href=\""
			+ dataLink + "\"><img src=\"/public/images/" + logoLink
			+ "\" width=\"100\" /></a></td></tr>");
	    } else {
		st.add("data", "");
	    }

	    SailRepository myRepository = new SailRepository(new MemoryStore());

	    myRepository.initialize();
	    mycon = myRepository.getConnection();
	    String baseURI = "";
	    try {
		mycon.add(new StringReader(rdf), baseURI, RDFFormat.N3);
		RepositoryResult<Statement> statements = mycon.getStatements(
			null, null, null, false);
		while (statements.hasNext()) {
		    Statement statement = statements.next();
		    String subject = statement.getSubject().stringValue();
		    String predicate = statement.getPredicate().stringValue();
		    String object = statement.getObject().stringValue();

		    MyTriple triple = new MyTriple(subject, predicate, object,
			    pid);

		    if (predicate.compareTo("http://purl.org/dc/terms/hasPart") == 0
			    || predicate
				    .compareTo("http://purl.org/dc/terms/isPartOf") == 0) {
			st.add("relations", triple);
		    } else if (predicate
			    .compareTo("http://www.openarchives.org/ore/terms/aggregates") == 0
			    || predicate
				    .compareTo("http://www.openarchives.org/ore/terms/isAggregatedBy") == 0)

		    {
			// do nothing!;
		    } else if (predicate
			    .compareTo("http://www.openarchives.org/ore/terms/similarTo") == 0) {
			st.add("links", triple);
		    } else {
			st.add("statements", triple);
		    }

		}
		result = st.render();
	    } catch (Exception e) {
		logger.warn(e.getMessage());
	    }

	} catch (RepositoryException e) {

	    logger.error(e.getMessage());
	}

	catch (IOException e) {
	    logger.error(e.getMessage());
	}

	finally {
	    if (con != null) {
		try {
		    mycon.close();
		} catch (RepositoryException e) {
		    logger.error(e.getMessage());
		    e.printStackTrace();
		}
	    }
	}
	return result;

    }

    private class MyTriple {
	String subject;
	String predicate;
	String object;
	String pid;

	public MyTriple(String subject, String predicate, String object,
		String pid) {
	    this.subject = subject;
	    this.predicate = predicate;
	    this.object = object;
	    this.pid = pid;
	}

	public String toString() {
	    String subjectLink = null;
	    String objectLink = null;
	    String namespace = pid.substring(0, pid.indexOf(":"));
	    if (subject.startsWith(pid)) {
		subjectLink = uriPrefix + subject;
	    } else {
		subjectLink = subject;
	    }
	    if (object.startsWith(namespace)) {
		objectLink = uriPrefix + object;
	    } else if (object.startsWith("http")) {
		objectLink = object;
	    }
	    if (predicate.compareTo("http://hbz-nrw.de/regal#contentType") == 0) {
		objectLink = "/resource?type=" + object;
	    }
	    if (objectLink != null) {
		return "<tr><td><a href=\"" + subjectLink + "\">" + subject
			+ "</a></td><td><a href=\"" + predicate + "\">"
			+ predicate + "</a></td><td about=\"" + subject
			+ "\"><a property=\"" + predicate + "\" href=\""
			+ objectLink + "\">" + object + "</a></td></tr>";
	    } else {
		return "<tr><td><a href=\"" + subjectLink + "\">" + subject
			+ "</a></td><td><a href=\"" + predicate + "\">"
			+ predicate + "</a></td><td about=\"" + subject + "\">"
			+ object + "</td></tr>";
	    }
	}
    }

    private class CreateRepositoryException extends RuntimeException {
	public CreateRepositoryException(Throwable e) {
	    super(e);
	}
    }

    private class WriteRdfException extends RuntimeException {
	public WriteRdfException(Throwable e) {
	    super(e);
	}
    }

}
