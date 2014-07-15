package archive.fedora;
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

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Graph;
import org.openrdf.rio.RDFFormat;

import archive.fedora.RdfUtils;
import archive.fedora.XmlUtils;

/**
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
@SuppressWarnings("javadoc")
public class RdfUtilsTest {

    @Test
    public void testInputStreamToGraph() throws URISyntaxException {
	InputStream in = Thread.currentThread().getContextClassLoader()
		.getResourceAsStream("HT015954381.txt");
	File expected = new File(Thread.currentThread().getContextClassLoader()
		.getResource("HT015954381_expected.txt").toURI().getPath());
	Graph graph = RdfUtils.readRdfToGraph(in, RDFFormat.NTRIPLES, "");
	String actual = RdfUtils.graphToString(graph, RDFFormat.NTRIPLES);
	System.out.println(actual);
	Assert.assertEquals(XmlUtils.fileToString(expected), actual);
    }
}
