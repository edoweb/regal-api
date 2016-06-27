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
package models;

import java.util.List;
import java.util.Vector;

/**
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 */
public class Transformer {

	private String id = null;
	private List<String> prescribedDSIds = null;
	private List<String> prescribedDSformatURIs = null;
	private List<String> prescribedDSMimeTypes = null;

	private String contentModelPID = null;
	private String serviceDefinitionPID = null;
	private String serviceDeploymentPID = null;

	private List<String> methodNames = null;
	private List<String> methodLocations = null;

	/**
	 * Creates a new Transformer. The passed Id will be used to create Objects
	 * according to a standard naming schema: setContentModelPID("CM:" + id);
	 * setServiceDefinitionPID("CM:" + id + "ServiceDefinition");
	 * setServiceDeploymentPID("CM:" + id + "ServiceDeployment");
	 * 
	 * @param id an Transformer-Id
	 * @param methodname a name for the initial method
	 * @param methodLocation a Method
	 */
	public Transformer(String id, String methodname, String methodLocation) {
		this.id = id;
		prescribedDSIds = new Vector<String>();
		prescribedDSformatURIs = new Vector<String>();
		prescribedDSMimeTypes = new Vector<String>();
		methodNames = new Vector<String>();
		methodLocations = new Vector<String>();
		setContentModelPID("CM:" + id);
		setServiceDefinitionPID("CM:" + id + "ServiceDefinition");
		setServiceDeploymentPID("CM:" + id + "ServiceDeployment");
		addMethod(methodname, methodLocation);
	}

	/**
	 * Create a Transformer without methods
	 * 
	 * @param id an id for the transformer
	 */
	public Transformer(String id) {
		this.id = id;
		prescribedDSIds = new Vector<String>();
		prescribedDSformatURIs = new Vector<String>();
		prescribedDSMimeTypes = new Vector<String>();
		methodNames = new Vector<String>();
		methodLocations = new Vector<String>();
		setContentModelPID("CM:" + id);
		setServiceDefinitionPID("CM:" + id + "ServiceDefinition");
		setServiceDeploymentPID("CM:" + id + "ServiceDeployment");
	}

	/**
	 * @param methodName The name of the method.
	 * @param methodLocation The webaddress of the method.
	 */
	public void addMethod(String methodName, String methodLocation) {
		methodNames.add(methodName);
		methodLocations.add(methodLocation);
	}

	/**
	 * @return the pid of the contentModel
	 */
	public String getContentModelPID() {
		return contentModelPID;
	}

	/**
	 * @return the pid of the service definition object
	 */
	public String getServiceDefinitionPID() {
		return serviceDefinitionPID;
	}

	/**
	 * @return the pid of the service deployment object.
	 */
	public String getServiceDeploymentPID() {
		return serviceDeploymentPID;
	}

	/**
	 * @param contentModelPID the pid of the content model.
	 */
	public void setContentModelPID(String contentModelPID) {
		this.contentModelPID = contentModelPID;
	}

	/**
	 * @param serviceDefinitionPID The pid of the service definition.
	 */
	public void setServiceDefinitionPID(String serviceDefinitionPID) {
		this.serviceDefinitionPID = serviceDefinitionPID;
	}

	/**
	 * @param serviceDeploymentPID the pid of the service deployment pid.
	 */
	public void setServiceDeploymentPID(String serviceDeploymentPID) {
		this.serviceDeploymentPID = serviceDeploymentPID;
	}

	/**
	 * @param dsid required data stream id
	 * @param formatUri the format of the required datastream
	 * @param mimeType the mime of the datastream
	 */
	public void addPrescribedDs(String dsid, String formatUri, String mimeType) {
		prescribedDSIds.add(dsid);
		prescribedDSformatURIs.add(formatUri);
		prescribedDSMimeTypes.add(mimeType);
	}

	/**
	 * @return the prescribedDSIds
	 */
	public List<String> getPrescribedDSIds() {
		return prescribedDSIds;
	}

	/**
	 * @return the prescribedDSformatURIs
	 */
	public List<String> getPrescribedDSformatURIs() {
		return prescribedDSformatURIs;
	}

	/**
	 * @return the prescribedDSMimeTypes
	 */
	public List<String> getPrescribedDSMimeTypes() {
		return prescribedDSMimeTypes;
	}

	/**
	 * @return the methodNames
	 */
	public List<String> getMethodNames() {
		return methodNames;
	}

	/**
	 * @return the methodLocations
	 */
	public List<String> getMethodLocations() {
		return methodLocations;
	}

	/**
	 * @return the plain id of the transformer
	 */
	public String getId() {
		return id;
	}

}
