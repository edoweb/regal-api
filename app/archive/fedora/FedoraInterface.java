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

import java.io.InputStream;
import java.util.List;

import models.Node;
import models.Transformer;

/**
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 */
public interface FedoraInterface {
    /**
     * @param node
     *            the object as Fedora object
     */
    public void createNode(Node node);

    /**
     * The corresponding fedora object will be read into a new node
     * 
     * @param pid
     *            The pid of an existing node.
     * @return The node as java object
     * 
     */
    public Node readNode(String pid);

    /**
     * 
     * @param node
     *            the local java representation of the remote fedora object
     */
    public void updateNode(Node node);

    /**
     * The fedora object will be deleted
     * 
     * @param rootPID
     *            identifier of a fedora object
     */
    public void deleteNode(String rootPID);

    /**
     * @param pid
     *            the pid of the object
     * @param datastream
     *            the datastream that must be deleted
     */
    public void deleteDatastream(String pid, String datastream);

    /**
     * @param namespace
     *            The namespace to generate a pid .
     * @return A new generated id.
     */
    public String getPid(String namespace);

    /**
     * @param namespace
     *            The namespace of the pids
     * @param number
     *            the number of pids
     * @return An array of empty pids.
     */
    public String[] getPids(String namespace, int number);

    /**
     * @param pid
     *            the pid of the node
     * @return true if exists and false if not
     */
    public boolean nodeExists(String pid);

    /**
     * Description: If the object 'pid' has a datastream with 'datastreamId' the
     * method returns true.
     * 
     * @param pid
     *            of the object
     * @param datastreamId
     *            of the objects datastream
     * @return true if datastream exists
     */
    public boolean dataStreamExists(String pid, String datastreamId);

    /**
     * @param simpleQuery
     *            the query
     * @param queryType
     *            if equals "simple" a simple search is done if equals "rdf" it
     *            is the same like findTriples(rdfQuery, FedoraVocabulary.SPO,
     *            FedoraVocabulary.N3);
     * @return A List of pids
     */
    public List<String> findPids(String simpleQuery, String queryType);

    /**
     * @param rdfQuery
     *            A query
     * @param queryType
     *            The type of the query
     * @param outputFormat
     *            the format of the returned stream
     * @return The query result as stream
     */
    public InputStream findTriples(String rdfQuery, String queryType,
	    String outputFormat);

    /**
     * @param pid
     *            a pid
     * @return , e.g info:fedora.
     */
    public String addUriPrefix(String pid);

    /**
     * @param pred
     *            a predicate
     * @return , e.g info:fedora.
     */
    public String removeUriPrefix(String pred);

    /**
     * Updates the list of ContentModel objects. If a ContentModel does't exist
     * it will be created.
     * 
     * @param cms
     *            list of ContentModels
     */
    public void updateContentModels(List<Transformer> cms);

    /**
     * @param node
     *            dc stream will be added to this node
     * @param in
     *            stream containing xml dc data
     * @param dcNamespace
     *            namespace of the dc
     */
    public void readDcToNode(Node node, InputStream in, String dcNamespace);

    /**
     * @param searchTerm
     *            A search term
     * @return A list of pids
     */
    public List<String> findNodes(String searchTerm);

    /**
     * @param pid
     *            The pid that must be deleted
     * @return The pid that has been deleted
     */
    public List<Node> deleteComplexObject(String pid);

    /**
     * @param namespace
     *            The namespace of the new object
     * @return The new created object.
     */
    public Node createRootObject(String namespace);

    /**
     * @param node
     *            a node connected to a parent
     * @return the pid of the parent connected via isPartOf
     */
    public String getNodeParent(Node node);

    /**
     * Finds the parent (isPartOf) and removes the hasPart statement that points
     * to the node.
     * 
     * @param node
     *            all Parents will be removed
     */
    public void unlinkParent(Node node);

    /**
     * Add a isPartOf statement to the node.
     * 
     * @param node
     *            the node is a child of parent
     * @param parentPid
     *            the pid of the parent
     */
    public void linkToParent(Node node, String parentPid);

    /**
     * Add a hasPart statement to the parent
     * 
     * @param parentPid
     *            the parent
     * @param pid
     *            the nodes pid
     */
    public void linkParentToNode(String parentPid, String pid);

    /**
     * @param pid
     *            pid of root object
     * @return list of all children and children of children...
     */
    public List<Node> listComplexObject(String pid);

}
