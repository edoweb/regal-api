/*
 * Copyright 2012 hbz NRW (http://www.hbz-nrw.de/)
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
package archive.fedora;

import static archive.fedora.FedoraVocabulary.HAS_PART;
import static archive.fedora.FedoraVocabulary.IS_PART_OF;
import static archive.fedora.FedoraVocabulary.REL_HAS_MODEL;
import static archive.fedora.FedoraVocabulary.SIMPLE;
import static archive.fedora.Vocabulary.REL_ACCESS_SCHEME;
import static archive.fedora.Vocabulary.REL_CONTENT_TYPE;
import static archive.fedora.Vocabulary.REL_IS_NODE_TYPE;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import models.Link;
import models.Node;
import models.Transformer;

import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.FedoraCredentials;
import com.yourmediashelf.fedora.client.request.FedoraRequest;
import com.yourmediashelf.fedora.client.request.GetDatastream;
import com.yourmediashelf.fedora.client.request.GetDatastreamDissemination;
import com.yourmediashelf.fedora.client.request.GetNextPID;
import com.yourmediashelf.fedora.client.request.GetObjectProfile;
import com.yourmediashelf.fedora.client.request.Ingest;
import com.yourmediashelf.fedora.client.request.ListDatastreams;
import com.yourmediashelf.fedora.client.request.ModifyDatastream;
import com.yourmediashelf.fedora.client.request.PurgeObject;
import com.yourmediashelf.fedora.client.request.RiSearch;
import com.yourmediashelf.fedora.client.response.FedoraResponse;
import com.yourmediashelf.fedora.client.response.GetDatastreamResponse;
import com.yourmediashelf.fedora.client.response.GetNextPIDResponse;
import com.yourmediashelf.fedora.client.response.GetObjectProfileResponse;
import com.yourmediashelf.fedora.client.response.ListDatastreamsResponse;
import com.yourmediashelf.fedora.generated.access.DatastreamType;
import com.yourmediashelf.fedora.generated.management.PidList;

/**
 * The FedoraFacade implements all Fedora-Calls as a singleton
 * 
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 */
class FedoraFacade implements FedoraInterface {

    private class DeleteException extends ArchiveException {

	private static final long serialVersionUID = -7879667636793687166L;

	public DeleteException(final String message, final Throwable cause) {
	    super(message, cause);
	}

    }

    private class ReadNodeException extends ArchiveException {

	private static final long serialVersionUID = 7338818611992590876L;

	public ReadNodeException(final String message, final Throwable cause) {
	    super(message, cause);
	}

    }

    private class InitializeFedoraFacadeException extends ArchiveException {

	private static final long serialVersionUID = 5357635794214927895L;

	public InitializeFedoraFacadeException(final String message,
		final Throwable cause) {
	    super(message, cause);
	}

	public InitializeFedoraFacadeException(final Throwable cause) {
	    super(cause);
	}

    }

    class UpdateContentModel extends ArchiveException {

	private static final long serialVersionUID = 1794883693210840141L;

	public UpdateContentModel(final String message, final Throwable cause) {
	    super(message, cause);
	}
    }

    class DeleteDatastreamException extends ArchiveException {

	private static final long serialVersionUID = 128120359698836741L;

	public DeleteDatastreamException(final String message,
		final Throwable cause) {
	    super(message, cause);
	}
    }

    class GetPidException extends ArchiveException {

	private static final long serialVersionUID = 5316657644921457520L;

	public GetPidException(final String message, final Throwable cause) {
	    super(message, cause);
	}
    }

    class CreateNodeException extends ArchiveException {

	private static final long serialVersionUID = 8569995140758544941L;

	public CreateNodeException(final String message, final Throwable cause) {
	    super(message, cause);
	}

	public CreateNodeException(final Throwable cause) {
	    super(cause);
	}

    }

    class SearchException extends ArchiveException {

	private static final long serialVersionUID = -276889477323963368L;

	public SearchException(final String message, final Throwable cause) {
	    super(message, cause);
	}
    }

    class NodeNotFoundException extends ArchiveException {

	private static final long serialVersionUID = 8851350561350951329L;

	public NodeNotFoundException(String message, Throwable cause) {
	    super(message, cause);
	}

	public NodeNotFoundException(String message) {
	    super(message);
	}

    }

    final static Logger logger = LoggerFactory.getLogger(FedoraFacade.class);

    static FedoraFacade me = null;
    Utils utils = null;

    /**
     * @param host
     *            The url of the fedora web endpoint
     * @param aUser
     *            A valid fedora user
     * @param aPassword
     *            The password of the fedora user
     */
    private FedoraFacade(String host, String aUser, String aPassword) {
	utils = new Utils(host, aUser);
	try {
	    FedoraCredentials credentials = new FedoraCredentials(host, aUser,
		    aPassword);
	    FedoraClient fedora = new com.yourmediashelf.fedora.client.FedoraClient(
		    credentials);
	    FedoraRequest.setDefaultClient(fedora);

	} catch (MalformedURLException e) {
	    throw new InitializeFedoraFacadeException(e);
	}

    }

    /**
     * @param host
     *            The url of the fedora web endpoint
     * @param aUser
     *            A valid fedora user
     * @param aPassword
     *            The password of the fedora user
     * @return a instance of FedoraFacade singleton
     */
    public static FedoraFacade getInstance(String host, String aUser,
	    String aPassword) {
	if (me == null)
	    return new FedoraFacade(host, aUser, aPassword);
	else
	    return me;
    }

    @Override
    public void createNode(Node node) {
	try {
	    new Ingest(node.getPid()).label(node.getLabel()).execute();
	    DublinCoreHandler.updateDc(node);
	    List<Transformer> cms = node.getTransformer();
	    // utils.createContentModels(cms);
	    utils.linkContentModels(cms, node);
	    if (node.getUploadFile() != null) {
		utils.createManagedStream(node);
	    }
	    if (node.getMetadataFile() != null) {
		utils.createMetadataStream(node);
	    }
	    Link link = new Link();
	    link.setObject(node.getContentType(), true);
	    link.setPredicate(REL_CONTENT_TYPE);
	    node.addRelation(link);
	    link = new Link();
	    link.setObject(node.getNodeType(), true);
	    link.setPredicate(REL_IS_NODE_TYPE);
	    node.addRelation(link);
	    link = new Link();
	    link.setObject(node.getAccessScheme(), true);
	    link.setPredicate(REL_ACCESS_SCHEME);
	    node.addRelation(link);
	    utils.createRelsExt(node);
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new CreateNodeException(e);
	}
    }

    @Override
    public Node createRootObject(String namespace) {
	Node rootObject = null;
	String pid = getPid(namespace);
	rootObject = new Node();
	rootObject.setPID(pid);
	rootObject.setLabel("Default Object");
	rootObject.setNamespace(namespace);
	createNode(rootObject);
	return rootObject;
    }

    @Override
    public Node readNode(String pid) {
	if (!nodeExists(pid))
	    throw new NodeNotFoundException(pid);
	Node node = new Node();
	node.setPID(pid);
	node.setNamespace(pid.substring(0, pid.indexOf(':')));
	try {
	    DublinCoreHandler.readFedoraDcToNode(node);
	    utils.readRelsExt(node);
	    // utils.readContentModels(node);
	    GetObjectProfileResponse prof = new GetObjectProfile(pid).execute();
	    node.setLabel(prof.getLabel());
	    node.setLastModified(prof.getLastModifiedDate());
	    node.setCreationDate(prof.getCreateDate());
	} catch (FedoraClientException e) {
	    throw new ReadNodeException(pid, e);
	}
	try {
	    GetDatastreamResponse response = new GetDatastream(pid, "data")
		    .execute();
	    node.setMimeType(response.getDatastreamProfile().getDsMIME());
	    node.setFileLabel(response.getDatastreamProfile().getDsLabel());
	    node.setChecksum(response.getDatastreamProfile().getDsChecksum());
	    node.setFileSize(response.getDatastreamProfile().getDsSize());
	} catch (FedoraClientException e) {
	    // datastream with name data is optional
	}
	try {
	    FedoraResponse response = new GetDatastreamDissemination(pid,
		    "metadata").execute();
	    node.setMetadata(CopyUtils.copyToString(
		    response.getEntityInputStream(), "utf-8"));
	} catch (Exception e) {
	    // datastream with name metadata is optional
	}
	return node;
    }

    @Override
    public void updateNode(Node node) {
	DublinCoreHandler.updateDc(node);
	List<Transformer> models = node.getTransformer();
	// utils.updateContentModels(models);
	node.removeRelations(REL_HAS_MODEL);
	if (node.getUploadFile() != null) {
	    utils.updateManagedStream(node);
	}
	if (node.getMetadataFile() != null) {
	    utils.updateMetadataStream(node);
	}
	utils.linkContentModels(models, node);
	utils.updateRelsExt(node);
    }

    @Override
    public List<String> findPids(String rdfQuery, String queryFormat) {
	if (queryFormat.compareTo(FedoraVocabulary.SIMPLE) == 0) {
	    return utils.findPidsSimple(rdfQuery);
	} else {
	    return findPidsRdf(rdfQuery, queryFormat);
	}
    }

    @Override
    public String getPid(String namespace) {
	try {
	    GetNextPIDResponse response = new GetNextPID().namespace(namespace)
		    .execute();
	    return response.getPid();
	} catch (FedoraClientException e) {
	    throw new GetPidException(namespace, e);
	}
    }

    @Override
    public String[] getPids(String namespace, int number) {
	try {
	    GetNextPIDResponse response = new GetNextPID().namespace(namespace)
		    .numPIDs(number).execute();
	    PidList list = response.getPids();
	    String[] arr = new String[list.getPid().size()];
	    list.getPid().toArray(arr);
	    return arr;
	} catch (FedoraClientException e) {
	    throw new GetPidException(namespace, e);
	}
    }

    @Override
    public void deleteNode(String rootPID) {
	try {
	    unlinkParent(rootPID);
	    new PurgeObject(rootPID).execute();
	} catch (FedoraClientException e) {

	    throw new DeleteException(rootPID, e);
	}
    }

    @Override
    public void deleteDatastream(String pid, String datastream) {
	try {
	    new ModifyDatastream(pid, datastream).dsState("D").execute();
	} catch (FedoraClientException e) {
	    throw new DeleteDatastreamException(pid, e);
	}
    }

    @Override
    public boolean nodeExists(String pid) {
	return utils.nodeExists(pid);
    }

    @Override
    public void updateContentModels(List<Transformer> cms) {
	utils.updateContentModels(cms);
    }

    @Override
    public InputStream findTriples(String query, String queryFormat,
	    String outputformat) {
	try {
	    FedoraResponse response = new RiSearch(query).format(outputformat)
		    .lang(queryFormat).type("triples").execute();
	    return response.getEntityInputStream();
	} catch (Exception e) {
	    throw new SearchException(query, e);
	}
    }

    @Override
    public String removeUriPrefix(String pred) {
	return utils.removeUriPrefix(pred);
    }

    @Override
    public String addUriPrefix(String pid) {
	return utils.addUriPrefix(pid);
    }

    @Override
    public List<String> findNodes(String searchTerm) {
	return findPids(searchTerm, SIMPLE);
    }

    @Override
    public void readDcToNode(Node node, InputStream in, String dcNamespace) {
	DublinCoreHandler.readDcToNode(node, in, dcNamespace);
    }

    @Override
    public List<Node> deleteComplexObject(String rootPID) {
	if (!nodeExists(rootPID)) {
	    throw new NodeNotFoundException(rootPID);
	}
	List<Node> list = listComplexObject(rootPID);
	for (Node n : list) {
	    deleteNode(n.getPid());
	}
	return list;
    }

    @Override
    public List<Node> listComplexObject(String rootPID) {
	if (!nodeExists(rootPID)) {
	    throw new NodeNotFoundException(rootPID);
	}
	Node root = readNode(rootPID);
	List<Node> result = new ArrayList<Node>();
	result.add(root);
	result.addAll(listChildren(root));
	return result;
    }

    private List<Node> listChildren(Node root) {
	List<Node> result = new ArrayList<Node>();
	List<Link> rels = root.getRelsExt();
	for (Link r : rels) {
	    if (HAS_PART.equals(r.getPredicate())) {
		result.addAll(listComplexObject(r.getObject()));
	    }
	}
	return result;
    }

    @Override
    public String getNodeParent(Node node) {
	List<Link> links = node.getRelsExt();
	for (Link link : links) {
	    if (link.getPredicate().compareTo(IS_PART_OF) == 0) {
		return link.getObject();
	    }
	}
	return null;
    }

    void unlinkParent(String pid) {
	try {
	    Node node = readNode(pid);
	    Node parent = readNode(getNodeParent(node));
	    parent.removeRelation(HAS_PART, node.getPid());
	    updateNode(parent);
	} catch (NodeNotFoundException e) {
	    // Nothing to do
	    logger.debug(pid + " has no parent!");
	} catch (ReadNodeException e) {
	    // Nothing to do
	    logger.debug(pid + " has no parent!");
	}
    }

    @Override
    public void unlinkParent(Node node) {
	try {
	    Node parent = readNode(getNodeParent(node));
	    parent.removeRelation(HAS_PART, node.getPid());
	    updateNode(parent);
	} catch (NodeNotFoundException e) {
	    // Nothing to do
	    // logger.debug(node.getPID() + " has no parent!");
	}
    }

    @Override
    public void linkToParent(Node node, String parentPid) {
	node.removeRelations(IS_PART_OF);
	Link link = new Link();
	link.setPredicate(IS_PART_OF);
	link.setObject(parentPid, false);
	node.addRelation(link);
	updateNode(node);
    }

    @Override
    public void linkParentToNode(String parentPid, String pid) {
	try {
	    Node parent = readNode(parentPid);
	    Link link = new Link();
	    link.setPredicate(HAS_PART);
	    link.setObject(pid, false);
	    parent.addRelation(link);
	    updateNode(parent);
	} catch (NodeNotFoundException e) {
	    // Nothing to do
	    // logger.debug(pid +
	    // " has no parent! ParentPid: "+parentPid+" is not a valid pid.");
	}
    }

    @Override
    public boolean dataStreamExists(String pid, String datastreamId) {
	try {
	    ListDatastreamsResponse response = new ListDatastreams(pid)
		    .execute();
	    for (DatastreamType ds : response.getDatastreams()) {
		if (ds.getDsid().compareTo(datastreamId) == 0) {
		    GetDatastreamResponse r = new GetDatastream(pid,
			    datastreamId).execute();
		    if (r.getDatastreamProfile().getDsState().equals("D"))
			return false;
		    else
			return true;
		}
	    }
	} catch (FedoraClientException e) {
	    return false;
	}
	return false;
    }

    private List<String> findPidsRdf(String rdfQuery, String queryFormat) {
	InputStream stream = findTriples(rdfQuery, FedoraVocabulary.SPO,
		FedoraVocabulary.N3);
	List<String> resultVector = new Vector<String>();
	RepositoryConnection con = null;
	Repository myRepository = new SailRepository(new MemoryStore());
	try {
	    myRepository.initialize();
	    con = myRepository.getConnection();
	    String baseURI = "";
	    con.add(stream, baseURI, RDFFormat.N3);
	    RepositoryResult<Statement> statements = con.getStatements(null,
		    null, null, true);
	    while (statements.hasNext()) {
		Statement st = statements.next();
		String str = removeUriPrefix(st.getSubject().stringValue());
		resultVector.add(str);
	    }
	    return resultVector;
	} catch (RepositoryException e) {
	    throw new RdfException(rdfQuery, e);
	} catch (RDFParseException e) {
	    throw new RdfException(rdfQuery, e);
	} catch (IOException e) {
	    throw new RdfException(rdfQuery, e);
	} finally {
	    if (con != null) {
		try {
		    con.close();
		} catch (RepositoryException e) {
		    throw new RdfException(
			    rdfQuery + ". Can not close stream.", e);
		}
	    }
	}
    }
}