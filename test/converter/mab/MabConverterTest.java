package converter.mab;
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


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import converter.mab.MabConverter;
import archive.fedora.XmlUtils;

/**
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
@SuppressWarnings("javadoc")
public class MabConverterTest {

    @Test
    public void transformTestfilesAndCompareXmlToExample() throws IOException {

	transformTestfileAndCompareXmlToExample("HT015954381", "edoweb:3025500");
	transformTestfileAndCompareXmlToExample("HT014997894", "edoweb:1750745");
	transformTestfileAndCompareXmlToExample("HT015381429", "edoweb:2238512");
	transformTestfileAndCompareXmlToExample("HT015780155", "edoweb:2708089");
	transformTestfileAndCompareXmlToExample("HT017091204", "edoweb:4390058");
	transformTestfileAndCompareXmlToExample("HT017297166", "edoweb:4575674");
	transformTestfileAndCompareXmlToExample("HT015004325", "edoweb:1750774");
    }

    public void transformTestfileAndCompareXmlToExample(String id,
	    String recordId) throws IOException {
	InputStream in = getResourceAsStream(id + ".nt");

	ByteArrayOutputStream os = transformTestFile(in, recordId);
	String str = os.toString();
	System.out.println(str);
	File output = File.createTempFile("mabconverterOut", "xml");
	XmlUtils.stringToFile(output, str);
	File expected = File.createTempFile("mabconverter", "xml");
	FileOutputStream out = new FileOutputStream(expected);
	IOUtils.copy(getResourceAsStream(id + ".xml"), out);

	// xmlCompare(output, expected);
    }

    @SuppressWarnings("unused")
    private void xmlCompare(File output, File expected)
	    throws FileNotFoundException, SAXException, IOException {
	XMLUnit.setIgnoreWhitespace(true);
	XMLUnit.setIgnoreAttributeOrder(true);

	DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(new FileReader(
		expected), new FileReader(output)));

	List<?> allDifferences = diff.getAllDifferences();
	Assert.assertEquals("Differences found: " + diff.toString(), 0,
		allDifferences.size());

    }

    private InputStream getResourceAsStream(String name) {
	return Thread.currentThread().getContextClassLoader()
		.getResourceAsStream(name);
    }

    @SuppressWarnings("unused")
    private File initOutputFile(String output) throws IOException {
	File file = new File(output);
	if (file.exists()) {
	    file.delete();
	}
	if (!file.exists()) {
	    file.createNewFile();
	}
	return file;
    }

    private ByteArrayOutputStream transformTestFile(InputStream input,
	    String topic) throws IOException {
	MabConverter converter = new MabConverter(topic);
	return converter.convert(input);
    }
}
