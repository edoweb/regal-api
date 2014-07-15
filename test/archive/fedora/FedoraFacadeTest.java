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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import archive.datatypes.Link;
import archive.datatypes.Node;
import archive.datatypes.Transformer;
import archive.datatypes.Vocabulary;

/**
 * 
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
@SuppressWarnings("javadoc")
public class FedoraFacadeTest {
    final static Logger logger = LoggerFactory
	    .getLogger(FedoraFacadeTest.class);
    FedoraInterface facade = null;
    Node object = null;
    String server = null;

    @Before
    public void setUp() throws IOException {
	Properties properties = new Properties();
	properties.load(getClass().getResourceAsStream("/test.properties"));

	facade = FedoraFactory.getFedoraImpl(
		properties.getProperty("fedoraUrl"),
		properties.getProperty("user"),
		properties.getProperty("password"));
	server = properties.getProperty("apiUrl");

	object = new Node().setNamespace("test").setPID("test:234")
		.setLabel("Ein Testobjekt").setFileLabel("test")
		.setType(Vocabulary.TYPE_OBJECT);
	object.setContentType("monograph");
	object.dublinCoreData.addTitle("Ein Testtitel");
	object.dublinCoreData.addCreator("Jan Schnasse");
	object.setMetadataFile(Thread.currentThread().getContextClassLoader()
		.getResource("test.nt").getFile());

	object.addTransformer(new Transformer("testepicur", "epicur", server
		+ "/resource/(pid).epicur"));
	object.addTransformer(new Transformer("testoaidc", "oaidc", server
		+ "/resource/(pid).oaidc"));
	object.addTransformer(new Transformer("testpdfa", "pdfa", server
		+ "/resource/(pid).pdfa"));

	URL url = this.getClass().getResource("/test.pdf");
	object.setUploadData(url.getPath(), "application/pdf");
	cleanUp();
    }

    @Test
    public void createNode() {
	facade.createNode(object);
	Assert.assertTrue(facade.nodeExists(object.getPID()));
    }

    @Test(expected = FedoraFacade.NodeNotFoundException.class)
    public void testNodeNotFoundException() {
	facade.readNode(object.getPID());
    }

    @Test
    public void readNode() {

	facade.createNode(object);
	Node node = facade.readNode(object.getPID());

	Assert.assertEquals(0,
		node.getNodeType().compareTo(object.getNodeType()));
	Assert.assertEquals("test:234", node.getPID());
	Assert.assertEquals("test", node.getNamespace());
	Assert.assertEquals("Jan Schnasse",
		node.dublinCoreData.getFirstCreator());
	Assert.assertEquals("Ein Testobjekt", node.getLabel());
	Assert.assertEquals("test", node.getFileLabel());
	Assert.assertEquals("Ein Testtitel",
		node.dublinCoreData.getFirstTitle());
	Assert.assertEquals("application/pdf", node.getMimeType());

    }

    @Test
    public void updateNode() {

	// Object Creation
	facade.createNode(object);
	object = facade.readNode(object.getPID());
	Assert.assertEquals("Ein Testtitel",
		object.dublinCoreData.getFirstTitle());
	Assert.assertEquals("test", object.getFileLabel());
	// Assert.assertEquals("application/pdf", object.getMimeType());

	// Object update modifying the local instance
	Vector<String> newTitle = new Vector<String>();
	newTitle.add("Neuer Titel");
	object.dublinCoreData.setTitle(newTitle);
	URL url = Thread.currentThread().getContextClassLoader()
		.getResource("logback-test.xml");
	object.setUploadData(url.getPath(), "text/xml");
	object.setFileLabel("logback-test.xml");
	facade.updateNode(object);

	object = facade.readNode(object.getPID());
	Assert.assertEquals("Neuer Titel",
		object.dublinCoreData.getFirstTitle());
	Assert.assertEquals("logback-test.xml", object.getFileLabel());
	// Assert.assertEquals("text/xml", object.getMimeType());

	// Object update on the reread object
	object.setUploadData(url.getPath(), "application/pdf");
	facade.updateNode(object);
	object = facade.readNode(object.getPID());
	// Assert.assertEquals("application/pdf", object.getMimeType());
    }

    @Test(expected = org.junit.ComparisonFailure.class)
    public void updateNode_fails_because_of_fedora_bug_in_mime_type_handling() {

	// Object Creation
	facade.createNode(object);
	object = facade.readNode(object.getPID());
	Assert.assertEquals("Ein Testtitel",
		object.dublinCoreData.getFirstTitle());
	Assert.assertEquals("test", object.getFileLabel());
	Assert.assertEquals("application/pdf", object.getMimeType());

	// Object update modifying the local instance
	Vector<String> newTitle = new Vector<String>();
	newTitle.add("Neuer Titel");
	object.dublinCoreData.setTitle(newTitle);
	URL url = Thread.currentThread().getContextClassLoader()
		.getResource("logback-test.xml");
	object.setUploadData(url.getPath(), "text/xml");
	object.setFileLabel("logback-test.xml");
	facade.updateNode(object);

	object = facade.readNode(object.getPID());
	Assert.assertEquals("Neuer Titel",
		object.dublinCoreData.getFirstTitle());
	Assert.assertEquals("logback-test.xml", object.getFileLabel());
	Assert.assertEquals("text/xml", object.getMimeType());

	// Object update on the reread object
	object.setUploadData(url.getPath(), "application/pdf");
	facade.updateNode(object);
	object = facade.readNode(object.getPID());
	Assert.assertEquals("application/pdf", object.getMimeType());
    }

    @Test
    public void findObjects() {
	List<String> result = facade
		.findPids("test:*", FedoraVocabulary.SIMPLE);
	for (String pid : result)
	    facade.deleteNode(pid);
	Assert.assertEquals(0,
		facade.findPids("test:*", FedoraVocabulary.SIMPLE).size());
	if (!facade.nodeExists(object.getPID()))

	    facade.createNode(object);

	result = facade.findPids("test:*", FedoraVocabulary.SIMPLE);
	Assert.assertEquals(1, result.size());
    }

    @Test
    public void deleteNode() {
	if (!facade.nodeExists(object.getPID()))

	    facade.createNode(object);

	facade.deleteNode(object.getPID());
	Assert.assertFalse(facade.nodeExists(object.getPID()));
    }

    @Test
    public void createTransformer() {
	facade.createNode(object);
	List<Transformer> transformers = new Vector<Transformer>();
	transformers.add(new Transformer("testepicur", "epicur", server
		+ "/resource/(pid).epicur"));
	transformers.add(new Transformer("testoaidc", "oaidc", server
		+ "/resource/(pid).oaidc"));
	transformers.add(new Transformer("testpdfa", "pdfa", server
		+ "/resource/(pid).pdfa"));
	facade.updateContentModels(transformers);
    }

    @Test
    public void removeNodesTransformer() {
	facade.createNode(object);
	object.removeTransformer("testepicur");

	List<Transformer> ts = object.getContentModels();
	Assert.assertEquals(2, ts.size());
	for (Transformer t : ts) {
	    Assert.assertFalse(t.getId().equals("testepicur"));
	}
	facade.updateNode(object);
	object = facade.readNode(object.getPID());

	HashMap<String, String> map = new HashMap<String, String>();
	map.put("testoaidc", "testoaidc");
	map.put("testpdfa", "testpdfa");
	ts = object.getContentModels();
	Assert.assertEquals(2, ts.size());
	for (Transformer t : ts) {
	    Assert.assertTrue(map.containsKey(t.getId()));
	}
	for (Transformer t : ts) {
	    Assert.assertFalse(t.getId().equals("testepicur"));
	}
    }

    @Test
    public void readNodesTransformer() {
	facade.createNode(object);
	object = facade.readNode(object.getPID());
	List<Transformer> ts = object.getContentModels();
	HashMap<String, String> map = new HashMap<String, String>();
	map.put("testepicur", "testepicur");
	map.put("testoaidc", "testoaidc");
	map.put("testpdfa", "testpdfa");
	Assert.assertEquals(3, ts.size());
	for (Transformer t : ts) {
	    System.out.println(t.getId());
	    Assert.assertTrue(map.containsKey(t.getId()));
	}
    }

    @Test
    public void nodeExists() {
	Assert.assertTrue(!facade.nodeExists(object.getPID()));
	facade.createNode(object);
	Assert.assertTrue(facade.nodeExists(object.getPID()));
    }

    @Test
    public void createHierarchy() {
	Node node = new Node();
	node.setPID(facade.getPid("test"));
	Node parent = facade.createRootObject("test");
	node = facade.createNode(parent, node);
	for (Link link : node.getRelsExt()) {
	    if (link.getPredicate().equals(FedoraVocabulary.IS_PART_OF)) {
		System.out.println(link.getObject());
		Assert.assertTrue(link.getObject().equals(
			"info:fedora/" + parent.getPID()));
	    }
	}
	for (Link link : parent.getRelsExt()) {
	    if (link.getPredicate().equals(FedoraVocabulary.HAS_PART)) {
		System.out.println(link.getObject());
		Assert.assertTrue(link.getObject().equals(
			"info:fedora/" + node.getPID()));
	    }
	}
    }

    @After
    public void tearDown() {
	// cleanUp();
    }

    private void cleanUp() {

	List<String> result = facade.findPids("CM:test*",
		FedoraVocabulary.SIMPLE);
	for (String pid : result) {
	    facade.deleteNode(pid);
	}
	result = facade.findPids("test:*", FedoraVocabulary.SIMPLE);
	for (String pid : result) {
	    facade.deleteNode(pid);
	}
    }

}
