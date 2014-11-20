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

import java.util.List;

import models.Globals;
import models.Node;
import models.RegalObject;
import models.Transformer;

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
	new Index().remove(node.getPid(), node.getNamespace(),
		node.getContentType());
	overrideNodeMembers(node, object);
	Globals.fedora.updateNode(node);
	updateIndexAndCache(node);
	return node;
    }

    /**
     * @param node
     * @param object
     * @return the updated node
     */
    public Node patchResource(Node node, RegalObject object) {
	new Index().remove(node.getPid(), node.getNamespace(),
		node.getContentType());
	setNodeMembers(node, object);
	Globals.fedora.updateNode(node);
	updateIndexAndCache(node);
	return node;
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
	if (object.getTransformer() != null)
	    updateTransformer(object.getTransformer(), node);
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
    }

    private void overrideNodeMembers(Node node, RegalObject object) {
	setNodeType(object.getContentType(), node);
	linkWithParent(object.getParentPid(), node);
	updateTransformer(object.getTransformer(), node);
	node.setAccessScheme(object.getAccessScheme());
	node.setPublishScheme(object.getPublishScheme());
	node.setCreatedBy(object.getIsDescribedBy().getCreatedBy());
	node.setImportedFrom(object.getIsDescribedBy().getImportedFrom());
	node.setLegacyId(object.getIsDescribedBy().getLegacyId());
    }

    private void setNodeType(String type, Node node) {
	node.setType(TYPE_OBJECT);
	node.setContentType(type);
    }

    private void linkWithParent(String parentPid, Node node) {
	Globals.fedora.unlinkParent(node);
	Globals.fedora.linkToParent(node, parentPid);
	Globals.fedora.linkParentToNode(parentPid, node.getPid());
	updateIndexAndCache(node);
    }

    private void updateTransformer(List<String> transformers, Node node) {
	node.removeAllContentModels();
	if (transformers != null) {
	    for (String t : transformers) {
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

}