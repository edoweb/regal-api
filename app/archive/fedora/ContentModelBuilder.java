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

import java.util.List;

import archive.datatypes.Node;
import archive.datatypes.Transformer;

import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.request.AddDatastream;

/**
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
public class ContentModelBuilder {

    void createFedoraXMLForContentModels(Node node) {
	StringBuffer infoStream = new StringBuffer();
	List<Transformer> models = node.getContentModels();

	infoStream.append("<ContentModelInfoStream>");
	for (Transformer model : models) {
	    infoStream.append("<ContentModel>");
	    infoStream.append("<ContentModelPid>" + model.getContentModelPID()
		    + "</ContentModelPid>");
	    infoStream.append("<ServiceDefPid>"
		    + model.getServiceDefinitionPID() + "</ServiceDefPid>");
	    infoStream.append("<ServiceDepPid>"
		    + model.getServiceDeploymentPID() + "</ServiceDepPid>");
	    infoStream.append("<PrescribedDSs>");

	    List<String> psids = model.getPrescribedDSIds();
	    List<String> uris = model.getPrescribedDSformatURIs();
	    List<String> mimes = model.getPrescribedDSMimeTypes();
	    for (int i = 0; i < psids.size(); i++) {
		infoStream.append("<PrescribedDS>");
		infoStream.append("<dsid>" + psids.get(i) + "</dsid>");
		infoStream.append("<uri>" + uris.get(i) + "</uri>");
		infoStream.append("<mimeType>" + mimes.get(i) + "</mimeType>");
		infoStream.append("</PrescribedDS>");
	    }
	    infoStream.append("</PrescribedDSs>");

	    List<String> names = model.getMethodNames();
	    List<String> locs = model.getMethodLocations();
	    infoStream.append("<Methods>");
	    for (int i = 0; i < names.size(); i++) {
		infoStream.append("<Method>");
		infoStream.append("<name>" + names.get(i) + "</name>");
		infoStream.append("<serviceLocation>" + locs.get(i)
			+ "</serviceLocation>");
		infoStream.append("</Method>");
	    }
	    infoStream.append("</Methods>");

	    infoStream.append("</ContentModel>");
	}
	infoStream.append("</ContentModelInfoStream>");

	try {

	    new AddDatastream(node.getPID(), "HBZCMInfoStream")
		    .mimeType("text/xml")
		    .formatURI(
			    "info:hbz/hbz-system:HBZContentModelInfoStream1.0")
		    .versionable(true).content(infoStream.toString()).execute();
	}

	catch (FedoraClientException e) {
	    e.printStackTrace();
	    throw new ArchiveException(e.getMessage(), e);
	}

    }

    /**
     * @param cm
     *            A fedora-like content model
     * @return The fedora-ready content model as String.
     */
    String getDsCompositeModel(Transformer cm) {
	String start = " <dsCompositeModel xmlns=\"info:fedora/fedora-system:def/dsCompositeModel#\">";

	StringBuffer middle = new StringBuffer();
	List<String> prescribedDSIds = cm.getPrescribedDSIds();

	for (int i = 0; i < prescribedDSIds.size(); i++) {
	    String dsid = prescribedDSIds.get(i);
	    String furi = cm.getPrescribedDSformatURIs().get(i);
	    String mtype = cm.getPrescribedDSMimeTypes().get(i);

	    middle.append("<dsTypeModel ID=\"" + dsid + "\">"
		    + "<form FORMAT_URI=\"" + furi + "\" MIME=\"" + mtype
		    + "\"/>" + "</dsTypeModel>");
	}

	String end = "</dsCompositeModel>";

	return start + middle.toString() + end;
    }

    /**
     * @param cm
     *            A fedora-like content model
     * @return A fedora-like methodmap as string
     */
    String getMethodMap(Transformer cm) {
	String start = "<fmm:MethodMap name=\"MethodMap\" xmlns:fmm=\"http://fedora.comm.nsdlib.org/service/methodmap\">";

	StringBuffer middle = new StringBuffer();
	for (int i = 0; i < cm.getMethodNames().size(); i++) {
	    String methodName = cm.getMethodNames().get(i);
	    middle.append("<fmm:Method label=\"" + methodName
		    + "\" operationName=\"" + methodName + "\"/>");
	}

	String end = "</fmm:MethodMap>";

	return start + middle.toString() + end;
    }

    /**
     * @param cm
     *            a fedora-like content model
     * @return the fedora-like method map to wsdl mapping as string
     */
    String getMethodMapToWsdl(Transformer cm) {

	String start = "<fmm:MethodMap name=\"MethodMap\" xmlns:fmm=\"http://fedora.comm.nsdlib.org/service/methodmap\">";

	StringBuffer middle = new StringBuffer();
	for (int i = 0; i < cm.getMethodNames().size(); i++) {
	    String methodName = cm.getMethodNames().get(i);
	    middle.append("    <fmm:Method operationLabel=\""
		    + methodName
		    + "\" operationName=\""
		    + methodName
		    + "\" wsdlMsgName=\""
		    + methodName
		    + "Request\" wsdlMsgOutput=\"dissemResponse\">"
		    + "        <fmm:DatastreamInputParm defaultValue=\"\" label=\""
		    + methodName
		    + "\" parmName=\"DC\" passBy=\"URL_REF\" required=\"false\"/>"
		    + "<fmm:DefaultInputParm defaultValue=\"$pid\" parmName=\"pid\" passBy=\"VALUE\" required=\"true\"/>"
		    + "    </fmm:Method>");
	}

	String end = "</fmm:MethodMap>";

	return start + middle.toString() + end;
    }

    /**
     * @return the fedora-like input-spec as string
     */
    String getDSInputSpec() {
	return "<fbs:DSInputSpec label=\"Undefined\" xmlns:fbs=\"http://fedora.comm.nsdlib.org/service/bindspec\">"
		+ "<fbs:DSInput DSMax=\"1\" DSMin=\"1\" DSOrdinality=\"false\" wsdlMsgPartName=\"DC\">"
		+ "	    <fbs:DSInputLabel>DC Binding</fbs:DSInputLabel>"
		+ "	    <fbs:DSMIME>text/xml</fbs:DSMIME>"
		+ "	    <fbs:DSInputInstruction/>"
		+ "	  </fbs:DSInput>"
		+ "	</fbs:DSInputSpec>";

    }

    /**
     * @param cm
     *            a fedora-like content model
     * @return the fedora-like wsdl as string
     */
    String getWsdl(Transformer cm) {
	String start = "<wsdl:definitions name=\"Undefined\" targetNamespace=\"bmech\""
		+ "	    xmlns:http=\"http://schemas.xmlsoap.org/wsdl/http/\" xmlns:mime=\"http://schemas.xmlsoap.org/wsdl/mime/\""
		+ "	    xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap\" xmlns:soapenc=\"http://schemas.xmlsoap.org/wsdl/soap/encoding\""
		+ "	    xmlns:this=\"bmech\" xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">";
	StringBuffer middle = new StringBuffer();
	for (int i = 0; i < cm.getMethodNames().size(); i++) {
	    String methodName = cm.getMethodNames().get(i);
	    middle.append("<wsdl:message name=\"" + methodName + "Request\">"
		    + "</wsdl:message>");
	}
	middle.append("<wsdl:message name=\"dissemResponse\">"
		+ "<wsdl:part name=\"dissem\" type=\"xsd:base64Binary\"/>"
		+ "</wsdl:message>");
	middle.append("<wsdl:portType name=\"HBZContentModelPortType\">");
	for (int i = 0; i < cm.getMethodNames().size(); i++) {
	    String methodName = cm.getMethodNames().get(i);
	    middle.append("<wsdl:operation name=\"" + methodName + "\">"
		    + "<wsdl:input message=\"this:" + methodName
		    + "Request\"/>"
		    + "<wsdl:output message=\"this:dissemResponse\"/>"
		    + "</wsdl:operation>");
	}
	middle.append("</wsdl:portType>");
	middle.append("<wsdl:service name=\"HBZContentModelImpl\">");
	middle.append("<wsdl:port binding=\"this:HBZContentModelImpl_http\" name=\"HBZContentModelImpl_port\">"
		+ "<http:address location=\"LOCAL\"/>" + "</wsdl:port>");
	middle.append("</wsdl:service>");
	middle.append("<wsdl:binding name=\"HBZContentModelImpl_http\" type=\"this:HBZContentModelPortType\">"
		+ "<http:binding verb=\"GET\"/>");
	for (int i = 0; i < cm.getMethodNames().size(); i++) {
	    String methodName = cm.getMethodNames().get(i);
	    String methodLocation = cm.getMethodLocations().get(i);
	    middle.append("<wsdl:operation name=\"" + methodName + "\">"
		    + "<http:operation location=\"" + methodLocation + "\"/>"
		    + "<wsdl:input>" + "<http:urlReplacement/>"
		    + "</wsdl:input>" + "<wsdl:output>"
		    + "<mime:content type=\"text/xml\"/>" + "</wsdl:output>"
		    + "</wsdl:operation>");
	}
	String end = " </wsdl:binding>" + "</wsdl:definitions>";

	return start + middle.toString() + end;
    }

}
