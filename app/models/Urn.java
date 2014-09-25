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
package models;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jan Schnasse
 *
 */
@SuppressWarnings("javadoc")
public class Urn {
    String resolver = "http://nbn-resolving.org/";
    String urn = null;
    String resolvesTo = "NONE";
    int resolverStatus = 404;
    boolean success = false;

    /**
     * @param urn
     * @param httpUriOfResource
     */
    public Urn(String urn) {
	this.urn = urn;
    }

    public void init(String httpUriOfResource) {
	try {
	    URL url = new URL(resolver + urn);
	    HttpURLConnection con = (HttpURLConnection) url.openConnection();
	    HttpURLConnection.setFollowRedirects(true);
	    con.connect();
	    resolverStatus = con.getResponseCode();
	    resolvesTo = parseAdressFromHtml(con.getInputStream(),
		    httpUriOfResource);
	    if (resolvesTo.equals(httpUriOfResource))
		success = true;
	} catch (Exception e) {

	}
    }

    private String parseAdressFromHtml(InputStream inputStream,
	    String httpUriOfResource) {
	String pid = httpUriOfResource.substring(httpUriOfResource
		.lastIndexOf('/'));
	Scanner scn = new Scanner(inputStream, "UTF-8");
	try {
	    String content = scn.useDelimiter("\\A").next();
	    Pattern pattern = Pattern.compile("\"(http://.*" + pid + ")\"");
	    Matcher matcher = pattern.matcher(content);
	    matcher.find();
	    return matcher.group(1);
	} finally {
	    scn.close();
	}
    }

    public String getUrn() {
	return urn;
    }

    public String getResolvesTo() {
	return resolvesTo;
    }

    public void setResolvesTo(String resolvesTo) {
	this.resolvesTo = resolvesTo;
    }

    public void setUrn(String urn) {
	this.urn = urn;
    }

    public String getResolver() {
	return resolver;
    }

    public int getResolverStatus() {
	return resolverStatus;
    }

    public void setResolverStatus(int responseCode) {
	this.resolverStatus = responseCode;
    }

    public boolean getSuccess() {
	return success;
    }
}