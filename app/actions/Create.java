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
package actions;

import static archive.fedora.Vocabulary.TYPE_OBJECT;
import helper.HttpArchiveException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import models.Gatherconf;
import models.Globals;
import models.Node;
import models.RegalObject;
import models.Transformer;
import models.RegalObject.Provenience;

/**
 * @author Jan Schnasse
 *
 */
public class Create extends RegalAction {

    /**
     * @param node
     * @param object
     * @return the updated node
     */
    public Node updateResource(Node node, RegalObject object) {
	new Index().remove(node);
	overrideNodeMembers(node, object);
	return updateResource(node);
    }

    private Node updateResource(Node node) {
	Globals.fedora.updateNode(node);
	updateIndex(node.getPid());
	return node;
    }

    /**
     * @param node
     * @param object
     * @return the updated node
     */
    public Node patchResource(Node node, RegalObject object) {
	new Index().remove(node);
	setNodeMembers(node, object);
	return updateResource(node);
    }

    /**
     * @param nodes
     *            nodes to set new properties for
     * @param object
     *            the RegalObject contains props that will be applied to all
     *            nodes in the list
     * @return a message
     */
    public String patchResources(List<Node> nodes, RegalObject object) {
	return apply(nodes, n -> patchResource(n, object).getPid());
    }

    /**
     * @param namespace
     * @param object
     * @return the updated node
     */
    public Node createResource(String namespace, RegalObject object) {
	String pid = pid(namespace);
	return createResource(pid.split(":")[1], namespace, object);
    }

    /**
     * @param id
     * @param namespace
     * @param object
     * @return the updated node
     */
    public Node createResource(String id, String namespace, RegalObject object) {
	Node node = initNode(id, namespace, object);
	updateResource(node, object);
	updateIndex(node.getPid());
	return node;
    }

    private Node initNode(String id, String namespace, RegalObject object) {
	Node node = new Node();
	node.setNamespace(namespace).setPID(namespace + ":" + id);
	node.setContentType(object.getContentType());
	node.setAccessScheme(object.getAccessScheme());
	node.setPublishScheme(object.getPublishScheme());
	node.setAggregationUri(createAggregationUri(node.getPid()));
	node.setRemUri(node.getAggregationUri() + ".rdf");
	node.setDataUri(node.getAggregationUri() + "/data");
	node.setContextDocumentUri("http://" + Globals.server
		+ "/public/edoweb-resources.json");
	Globals.fedora.createNode(node);
	return node;
    }

    private void setNodeMembers(Node node, RegalObject object) {
	if (object.getContentType() != null)
	    setNodeType(object.getContentType(), node);
	if (object.getParentPid() != null)
	    linkWithParent(object.getParentPid(), node);
	if (object.getAccessScheme() != null)
	    node.setAccessScheme(object.getAccessScheme());
	if (object.getPublishScheme() != null)
	    node.setPublishScheme(object.getPublishScheme());
	if (object.getIsDescribedBy().getCreatedBy() != null)
	    node.setCreatedBy(object.getIsDescribedBy().getCreatedBy());
	if (object.getIsDescribedBy().getImportedFrom() != null)
	    node.setImportedFrom(object.getIsDescribedBy().getImportedFrom());
	if (object.getIsDescribedBy().getLegacyId() != null)
	    node.setLegacyId(object.getIsDescribedBy().getLegacyId());
	if (object.getIsDescribedBy().getName() != null)
	    node.setName(object.getIsDescribedBy().getName());
	if (object.getTransformer() != null)
	    updateTransformer(object.getTransformer(), node);
    }

    private void overrideNodeMembers(Node node, RegalObject object) {
	setNodeType(object.getContentType(), node);
	linkWithParent(object.getParentPid(), node);
	node.setAccessScheme(object.getAccessScheme());
	node.setPublishScheme(object.getPublishScheme());
	node.setCreatedBy(object.getIsDescribedBy().getCreatedBy());
	node.setImportedFrom(object.getIsDescribedBy().getImportedFrom());
	node.setLegacyId(object.getIsDescribedBy().getLegacyId());
	node.setName(object.getIsDescribedBy().getName());
	updateTransformer(object.getTransformer(), node);
    }

    private void setNodeType(String type, Node node) {
	node.setType(TYPE_OBJECT);
	node.setContentType(type);
    }

    private void linkWithParent(String parentPid, Node node) {
	try {
	    String pp = node.getParentPid();
	    if (pp != null && !pp.isEmpty()) {
		try {
		    Globals.fedora.unlinkParent(node);
		    updateIndex(pp);
		} catch (HttpArchiveException e) {
		    play.Logger.debug("", e);
		}
	    }
	    Globals.fedora.linkToParent(node, parentPid);
	    Globals.fedora.linkParentToNode(parentPid, node.getPid());
	    updateIndex(node.getPid());
	    updateIndex(parentPid);
	} catch (Exception e) {
	    play.Logger.debug("", e);
	}
    }

    private void updateTransformer(List<String> transformers, Node node) {
	node.removeAllContentModels();
	if ("public".equals(node.getPublishScheme())) {
	    node.addTransformer(new Transformer("oaidc"));
	}
	if (node.hasUrn()) {
	    node.addTransformer(new Transformer("epicur"));
	    node.addTransformer(new Transformer("aleph"));
	}

	if (transformers != null) {
	    for (String t : transformers) {
		if ("oaidc".equals(t))
		    continue; // implicitly added - or not allowed to set
		if ("epicur".equals(t))
		    continue; // implicitly added - or not allowed to set
		if ("aleph".equals(t))
		    continue; // implicitly added - or not allowed to set
		node.addTransformer(new Transformer(t));
	    }
	}
    }

    /**
     * 
     * @param cms
     *            a List of Transformers
     * @return a message
     */
    public String contentModelsInit(List<Transformer> cms) {
	Globals.fedora.updateContentModels(cms);
	return "Success!";
    }

    /**
     * @param namespace
     *            a namespace in fedora , corresponds to an index in
     *            elasticsearch
     * @return a new pid in the namespace
     */
    public String pid(String namespace) {
	return Globals.fedora.getPid(namespace);
    }

    /**
     * @param n
     *            must be of type webpage
     * @return a new version pointing to a heritrix crawl
     */
    public Node createWebpageVersion(Node n) {
	try {
	    if (!"webpage".equals(n.getContentType())) {
		throw new HttpArchiveException(
			400,
			n.getContentType()
				+ " is not supported. Operation works only on regalType:\"webpage\"");
	    }
	    play.Logger.debug("Create webpageVersion " + n.getPid());
	    Gatherconf conf = Gatherconf.create(n.getConf());
	    play.Logger.debug("Create webpageVersi " + conf.toString());
	    // execute heritrix job
	    if (!Globals.heritrix.jobExists(conf.getName())) {
		Globals.heritrix.createJob(conf);
	    }
	    boolean success = Globals.heritrix.teardown(conf.getName());
	    play.Logger.debug("Teardown " + conf.getName() + " " + success);
	    Globals.heritrix.launch(conf.getName());

	    Thread.currentThread().sleep(10000);

	    Globals.heritrix.unpause(conf.getName());

	    Thread.currentThread().sleep(10000);

	    String localpath = Globals.heritrixData + "/heritrix-data"
		    + Globals.heritrix.findLatestWarc(conf.getName());
	    play.Logger.debug("Path to WARC " + localpath);

	    // create fedora object with unmanaged content pointing to
	    // the respective warc container
	    RegalObject regalObject = new RegalObject();
	    regalObject.setContentType("version");
	    Provenience prov = regalObject.getIsDescribedBy();
	    prov.setCreatedBy("webgatherer");
	    prov.setName(conf.getName());
	    prov.setImportedFrom(conf.getUrl());
	    regalObject.setIsDescribedBy(prov);
	    regalObject.setParentPid(n.getPid());
	    Node webpageVersion = createResource(n.getNamespace(), regalObject);
	    webpageVersion.setLocalData(localpath);
	    webpageVersion.setMimeType("application/warc");
	    webpageVersion.setFileLabel(new SimpleDateFormat("yyyy-MM-dd")
		    .format(new Date()));
	    return updateResource(webpageVersion);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }
}