
/*
 * Copyright 2017 hbz NRW (http://www.hbz-nrw.de/)
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
package helper.oai;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.DublinCoreData;
import models.Globals;
import models.Node;

/**
 * @author Jan Schnasse
 *
 */
public class WglMapper {

	Node node;
	String uri;

	public WglMapper(Node node, String uri) {
		this.node = node;
		this.uri = uri;
	}

	public DublinCoreData getData() {
		DublinCoreData data = new DublinCoreData();
		if (node == null)
			return data;

		JsonNode n = new ObjectMapper().valueToTree(node.getLd());
		data.setWglContributor(getWglContributor(n));
		data.setWglSubject(getWglSubject(n));
		data.setCreator(getCreator(n));
		data.setDescription(getList(n, "/abstractText"));
		data.setTitle(getList(n, "/title"));
		data.setDate(getList(n, "/publicationYear"));
		data.setPublisher(getList(n, "/publisher"));
		data.setSource(getSource(n));
		data.setSubject(getComplexList(n, "/subject", "/prefLabel"));
		data.addSubjects(getList(n, "/subjectName"));
		data.setLanguage(getComplexList(n, "/language", "/prefLabel"));
		data.setRights(getComplexList(n, "/license", "/prefLabel"));
		data.addIdentifier(uri);
		data.addIdentifier(getComplexList(n, "/publisherVersion", "/@id"));
		data.addIdentifier(getComplexList(n, "/additionalMaterial", "/@id"));
		data.addIdentifier(getString(n, "/urn"));
		data.addIdentifier(getString(n, "/doi"));
		return data;
	}

	private List<String> getSource(JsonNode n) {
		List<String> a = getComplexList(n, "/containedIn", "/prefLabel");
		List<String> b = getList(n, "/bibliographicCitation");

		if (a.size() != b.size()) {
			return a;
		}

		List<String> result = new ArrayList<>();
		for (int i = 0; i < a.size(); i++) {
			result.add(a.get(i) + ", " + parseBibliographicCitation(b.get(i)));
		}

		return result;
	}

	private String parseBibliographicCitation(String citation) {
		String[] parts = citation.split(":");
		if (parts.length != 2)
			return citation;

		if (parts[0] == null || parts[0].isEmpty()) {
			if (parts[1] != null && !parts[1].isEmpty()) {
				return findArticleNumberOrPages(parts[1]);
			} else {
				return "";
			}
		}

		if (parts[1] == null || parts[1].isEmpty()) {
			if (parts[0] != null && !parts[0].isEmpty()) {
				return "Volume " + parts[0];
			} else {
				return "";
			}
		}

		return "Volume " + parts[0] + ", " + findArticleNumberOrPages(parts[1]);
	}

	private String findArticleNumberOrPages(String in) {
		if (in.contains("-")) {
			return "Pages " + in;
		}
		return "Articlenumber " + in;
	}

	private List<String> getCreator(JsonNode n) {
		List<String> result = getList(n, "/contributorLabel");
		if (result.isEmpty()) {
			result.addAll(getComplexList(n, "/creator", "/prefLabel"));
			result.addAll(getList(n, "/creatorName"));
		}
		return result;
	}

	private List<String> getWglSubject(JsonNode n) {
		List<String> result = new ArrayList<>();
		JsonNode a = n.at("/ddc");
		for (JsonNode item : a) {
			String ddcId = item.at("/@id").asText("no Value found");
			if (ddcId.endsWith("570/")) {
				result.add("Biowissenschaften/Biologie");
			} else if (ddcId.endsWith("580/")) {
				result.add("Biowissenschaften/Biologie");
			} else if (ddcId.endsWith("590/")) {
				result.add("Biowissenschaften/Biologie");
			} else if (ddcId.endsWith("540/")) {
				result.add("Chemie");
			} else if (ddcId.endsWith("550/")) {
				result.add("Geowissenschaften");
			} else if (ddcId.endsWith("940/")) {
				result.add("Geschichte");
			} else if (ddcId.endsWith("943/")) {
				result.add("Geschichte");
			} else if (ddcId.endsWith("020/")) {
				result.add("Informatik");
			} else if (ddcId.endsWith("630/")) {
				result.add("Landwirtschaft");
			} else if (ddcId.endsWith("610/")) {
				result.add("Medizin, Gesundheit");
			} else if (ddcId.endsWith("530/")) {
				result.add("Physik");
			} else if (ddcId.endsWith("150/")) {
				result.add("Psychologie");
			}
		}

		a = n.at("/professionalGroup");
		for (JsonNode item : a) {
			String label = item.at("/prefLabel").asText("no Value found");
			if ("Ernährung".equals(label)) {
				result.add("Ernährungswissenschaft");
			} else if ("Bibliotheks- und Informationswissenschaft".equals(label)) {
				result.add("Informatik");
			} else if ("Agrar".equals(label)) {
				result.add("Landwirtschaft");
			} else if ("Medizin, Gesundheit".equals(label)) {
				result.add("Medizin, Gesundheit");
			}
		}
		return result;
	}

	private List<String> getComplexList(JsonNode n, String string,
			String string2) {
		List<String> result = new ArrayList<>();
		JsonNode a = n.at(string);
		for (JsonNode item : a) {
			String str = item.at(string2).asText("no Value found");
			result.add(str);
		}
		return result;
	}

	private List<String> getWglContributor(JsonNode n) {
		List<String> result = new ArrayList<>();
		JsonNode collectionOneArray = n.at("/collectionOne");
		for (JsonNode item : collectionOneArray) {
			String id = item.at("/@id").asText("no Value found");
			result.add(Globals.wglContributor.acronymOf(id));
		}
		if (result.isEmpty()) {
			JsonNode institutionArray = n.at("/institution");
			for (JsonNode item : institutionArray) {
				String id = item.at("/@id").asText("no Value found");
				result.add(Globals.wglContributor.acronymOf(id));
			}
		}
		return result;
	}

	private List<String> getList(JsonNode n, String string) {
		List<String> result = new ArrayList<>();
		JsonNode a = n.at(string);
		for (JsonNode item : a) {
			String str = item.asText("no Value found");
			result.add(str);
		}
		return result;
	}

	private String getString(JsonNode n, String string) {
		StringBuffer result = new StringBuffer();
		JsonNode a = n.at(string);
		for (JsonNode item : a) {
			String str = item.asText("no Value found");
			result.append(str + " ,");
		}
		if (result.length() == 0)
			return null;
		return result.subSequence(0, result.length() - 2).toString();
	}

}
