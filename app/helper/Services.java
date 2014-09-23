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

import static archive.fedora.FedoraVocabulary.IS_MEMBER_OF;
import static archive.fedora.FedoraVocabulary.ITEM_ID;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.DublinCoreData;
import models.Link;
import models.Node;

import org.antlr.runtime.RecognitionException;
import org.apache.commons.io.FileUtils;
import org.culturegraph.mf.Flux;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import archive.fedora.CopyUtils;
import archive.fedora.FedoraInterface;
import archive.fedora.RdfUtils;
import archive.fedora.XmlUtils;

/**
 * 
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 */
public class Services {

    @SuppressWarnings({ "serial", "javadoc" })
    public class MetadataNotFoundException extends RuntimeException {

	public MetadataNotFoundException() {
	    // TODO Auto-generated constructor stub
	}

	public MetadataNotFoundException(String arg0) {
	    super(arg0);
	    // TODO Auto-generated constructor stub
	}

	public MetadataNotFoundException(Throwable arg0) {
	    super(arg0);
	    // TODO Auto-generated constructor stub
	}

	public MetadataNotFoundException(String arg0, Throwable arg1) {
	    super(arg0, arg1);
	    // TODO Auto-generated constructor stub
	}

    }

    final static Logger logger = LoggerFactory.getLogger(Services.class);
    FedoraInterface fedora = null;
    String uriPrefix = null;

    /**
     * @param fedora
     *            a fedora connection
     * @param server
     *            the server where the app runs
     */
    public Services(FedoraInterface fedora, String server) {
	this.fedora = fedora;
	uriPrefix = server + "/" + "resource" + "/";
    }

    /**
     * @param node
     *            generate metadatafile with lobid data for this node
     * @return a short message
     */
    public Node lobidify(Node node) {

	String pid = node.getPid();

	List<String> identifier = node.getDublinCoreData().getIdentifier();
	String alephid = "";
	for (String id : identifier) {
	    if (id.startsWith("TT") || id.startsWith("HT")) {
		alephid = id;
		break;
	    }
	}
	if (alephid.isEmpty()) {
	    throw new HttpArchiveException(500, pid + " no Catalog-Id found");
	}

	String lobidUri = "http://lobid.org/resource/" + alephid;
	try {
	    URL lobidUrl = new URL("http://api.lobid.org/resource?id="
		    + alephid);

	    RDFFormat inFormat = RDFFormat.TURTLE;
	    String accept = "text/turtle";
	    String str = RdfUtils.readRdfToString(lobidUrl, inFormat,
		    RDFFormat.NTRIPLES, accept);

	    if (str.contains("http://www.w3.org/2002/07/owl#sameAs")) {
		str = RdfUtils.followSameAsAndInclude(lobidUrl, pid, inFormat,
			accept);
	    }
	    str = Pattern.compile(lobidUri).matcher(str)
		    .replaceAll(Matcher.quoteReplacement(pid))
		    + "<"
		    + pid
		    + "> <http://www.umbel.org/specifications/vocabulary#isLike> <"
		    + lobidUri + "> .";
	    File metadataFile = CopyUtils.copyStringToFile(str);

	    node.setMetadataFile(metadataFile.getAbsolutePath());
	    return node;
	} catch (MalformedURLException e) {
	    throw new HttpArchiveException(500, e);
	} catch (IOException e) {
	    throw new HttpArchiveException(500, e);
	}

    }

    /**
     * @param url
     *            the url the urn must point to
     * @param urn
     *            the urn
     * 
     * @return a epicur display for the pid
     */
    public String epicur(String url, String urn) {
	String status = "urn_new";
	String result = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<epicur xmlns=\"urn:nbn:de:1111-2004033116\" xsi:schemaLocation=\"urn:nbn:de:1111-2004033116 http://www.persistent-identifier.de/xepicur/version1.0/xepicur.xsd\">\n"
		+ "\t<administrative_data>\n"
		+ "\t\t<delivery>\n"
		+ "\t\t\t<update_status type=\""
		+ status
		+ "\"></update_status>\n"
		+ "\t\t\t<transfer type=\"oai\"></transfer>\n"
		+ "\t\t</delivery>\n"
		+ "\t</administrative_data>\n"
		+ "<record>\n"
		+ "\t<identifier scheme=\"urn:nbn:de\">"
		+ urn
		+ "</identifier>\n"
		+ "\t<resource>\n"
		+ "\t\t<identifier origin=\"original\" role=\"primary\" scheme=\"url\" type=\"frontpage\">"
		+ url
		+ "</identifier>\n"
		+ "\t\t<format scheme=\"imt\">text/html</format>\n"
		+ "\t</resource>" + "</record>\n" + "</epicur> ";
	return result;
    }

    /**
     * @param pid
     *            The pid of an object
     * @return The metadata a oaidc-xml
     */
    public String oaidc(String pid) {

	Node node = fedora.readNode(pid);
	if (node == null)
	    return "No node with pid " + pid + " found";

	String metadata = this.uriPrefix + pid + "/metadata";

	try {
	    File outfile = File.createTempFile("oaidc", "xml");
	    outfile.deleteOnExit();
	    File fluxFile = new File(Thread.currentThread()
		    .getContextClassLoader()
		    .getResource("morph-lobid-to-oaidc.flux").toURI());
	    Flux.main(new String[] { fluxFile.getAbsolutePath(),
		    "url=" + metadata, "out=" + outfile.getAbsolutePath() });
	    return FileUtils.readFileToString(outfile);
	} catch (IOException e) {
	    throw new HttpArchiveException(500, e);
	} catch (URISyntaxException e) {
	    throw new HttpArchiveException(500, e);
	} catch (RecognitionException e) {
	    throw new HttpArchiveException(500, e);
	}

    }

    /**
     * @param node
     *            the node with pdf data
     * @param fedoraExtern
     *            the fedora endpoint for external users
     * @return the plain text content of the pdf
     */
    public String pdfbox(Node node, String fedoraExtern) {
	String pid = node.getPid();

	String mimeType = node.getMimeType();
	if (mimeType == null)
	    throw new HttpArchiveException(
		    404,
		    "The node "
			    + pid
			    + " does not provide a mime type. It may not even contain data at all!");
	if (mimeType.compareTo("application/pdf") != 0)
	    throw new HttpArchiveException(406,
		    "Wrong mime type. Cannot extract text from " + mimeType);

	URL content = null;
	try {
	    content = new URL(fedoraExtern + "/objects/" + pid
		    + "/datastreams/data/content");

	    File pdfFile = CopyUtils.download(content);
	    PdfText pdf = new PdfText();
	    return pdf.toString(pdfFile);
	} catch (MalformedURLException e) {
	    throw new HttpArchiveException(500, e);
	} catch (IOException e) {
	    throw new HttpArchiveException(500, e);

	}

    }

    /**
     * @param pid
     *            the pid of a node that must be published on the oai interface
     * @param fedoraExtern
     *            the fedora endpoint for external users
     * @return A short message.
     */
    public String makeOAISet(String pid, String fedoraExtern) {

	Node node = fedora.readNode(pid);
	try {
	    URL metadata = new URL(fedoraExtern + "/objects/" + pid
		    + "/datastreams/metadata/content");

	    OaiSetBuilder oaiSetBuilder = new OaiSetBuilder();

	    RepositoryResult<Statement> statements = RdfUtils
		    .getStatements(metadata);

	    while (statements.hasNext()) {
		Statement st = statements.next();
		String subject = st.getSubject().stringValue();
		String predicate = st.getPredicate().stringValue();
		String object = st.getObject().stringValue();

		OaiSet set = oaiSetBuilder.getSet(subject, predicate, object);
		if (set == null) {
		    continue;
		}
		if (!fedora.nodeExists(set.getPid())) {
		    createOAISet(set.getName(), set.getSpec(), set.getPid());
		}
		linkObjectToOaiSet(node, set.getSpec(), set.getPid());
	    }
	    String name = "open_access";
	    String spec = "open_access";
	    String namespace = "oai";
	    String oaipid = namespace + ":" + "open_access";
	    if (!fedora.nodeExists(oaipid)) {
		createOAISet(name, spec, oaipid);
	    }
	    linkObjectToOaiSet(node, spec, oaipid);

	    return pid + " successfully created oai sets!";

	} catch (Exception e) {
	    throw new MetadataNotFoundException(e);
	}
    }

    /**
     * Generates a urn
     * 
     * @param niss
     *            usually the pid of an object
     * @param snid
     *            usually the namespace
     * @return the urn
     */
    public String generateUrn(String niss, String snid) {
	URN urn = new URN(snid, niss);
	return urn.toString();
    }

    /**
     * @param node
     *            the node with pdf data
     * @param fedoraExtern
     *            the fedora endpoint for external users
     * @return the plain text content of the pdf
     */
    public String itext(Node node, String fedoraExtern) {
	String pid = node.getPid();

	String mimeType = node.getMimeType();
	if (mimeType == null)
	    throw new HttpArchiveException(
		    404,
		    "The node "
			    + pid
			    + " does not provide a mime type. It may not even contain data at all!");
	if (mimeType.compareTo("application/pdf") != 0)
	    throw new HttpArchiveException(406,
		    "Wrong mime type. Cannot extract text from " + mimeType);

	URL content = null;
	try {
	    content = new URL(fedoraExtern + "/objects/" + pid
		    + "/datastreams/data/content");

	    File pdfFile = CopyUtils.download(content);
	    PdfText pdf = new PdfText();
	    return pdf.itext(pdfFile);
	} catch (MalformedURLException e) {
	    throw new HttpArchiveException(500, e);
	} catch (IOException e) {
	    throw new HttpArchiveException(500, e);

	}

    }

    private void linkObjectToOaiSet(Node node, String spec, String pid) {
	node.removeRelations(ITEM_ID);
	node.removeRelation(IS_MEMBER_OF, "info:fedora/" + pid);
	Link link = new Link();
	link.setPredicate(IS_MEMBER_OF);
	link.setObject("info:fedora/" + pid, false);
	node.addRelation(link);

	link = new Link();
	link.setPredicate(ITEM_ID);
	link.setObject(uriPrefix + node.getPid(), false);
	node.addRelation(link);

	fedora.updateNode(node);
    }

    private void createOAISet(String name, String spec, String pid) {
	String setSpecPred = "http://www.openarchives.org/OAI/2.0/setSpec";
	String setNamePred = "http://www.openarchives.org/OAI/2.0/setName";

	Link setSpecLink = new Link();
	setSpecLink.setPredicate(setSpecPred);

	Link setNameLink = new Link();
	setNameLink.setPredicate(setNamePred);

	String namespace = "oai";
	{
	    Node oaiset = new Node();
	    oaiset.setNamespace(namespace);
	    oaiset.setPID(pid);

	    setSpecLink.setObject(spec, true);
	    oaiset.addRelation(setSpecLink);

	    setNameLink.setObject(name, true);
	    oaiset.addRelation(setNameLink);

	    DublinCoreData dc = oaiset.getDublinCoreData();
	    dc.addTitle(name);

	    oaiset.setDublinCoreData(dc);
	    fedora.createNode(oaiset);
	}
    }

    /**
     * @param node
     *            to create a pdfa from.
     * @param fedoraExtern
     *            the url of the datastream
     * @return a url to the generated Pdfa
     */
    public String getPdfaUrl(Node node, String fedoraExtern) {
	String redirectUrl = null;
	try {
	    URL pdfaConverter = new URL(
		    "http://nyx.hbz-nrw.de/pdfa/api/convertFromUrl?inputFile="
			    + fedoraExtern + "/objects/" + node.getPid()
			    + "/datastreams/data/content");

	    HttpURLConnection connection = (HttpURLConnection) pdfaConverter
		    .openConnection();
	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Accept", "application/xml");
	    Element root = XmlUtils.getDocument(connection.getInputStream());
	    List<Element> elements = XmlUtils.getElements("//resultFileUrl",
		    root, null);
	    if (elements.size() != 1) {
		throw new HttpArchiveException(500,
			"PDFa conversion returns wrong numbers of resultFileUrls: "
				+ elements.size());
	    }
	    redirectUrl = elements.get(0).getTextContent();

	    return redirectUrl;

	} catch (MalformedURLException e) {
	    throw new HttpArchiveException(500, e);
	} catch (IOException e) {
	    throw new HttpArchiveException(500, e);
	}

    }
}
