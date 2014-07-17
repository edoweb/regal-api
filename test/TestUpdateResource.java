
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

/**
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 */
public class TestUpdateResource {

    // Properties properties;
    // Client client;
    // String apiUrl;
    //
    // @Before
    // public void setUp() throws IOException {
    //
    // properties = new Properties();
    // properties.load(getClass().getResourceAsStream("/test.properties"));
    //
    // ClientConfig cc = new DefaultClientConfig();
    // cc.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
    // cc.getFeatures().put(ClientConfig.FEATURE_DISABLE_XML_SECURITY, true);
    // client = Client.create(cc);
    // client.addFilter(new HTTPBasicAuthFilter(
    // properties.getProperty("user"), properties
    // .getProperty("password")));
    // apiUrl = properties.getProperty("apiUrl");
    // cleanUp();
    //
    // }
    //
    // @After
    // public void tearDown() {
    // cleanUp();
    // }
    //
    // @Test
    // public void testRestUpdate() {
    // createRestMonograph();
    // createRestJournal();
    // moveRestMonographAsArticleToJournal();
    //
    // }
    //
    // @Test
    // public void testResourceUpdate() throws FileNotFoundException,
    // IOException {
    // createMonograph();
    // createJournal();
    // moveMonographAsArticleToJournal();
    //
    // }
    //
    // private void moveRestMonographAsArticleToJournal() {
    // WebResource mRes = client.resource(apiUrl + "/resource/test:123");
    // RegalObject input = new RegalObject();
    // input.setType("article");
    // input.setParentPid("test:234");
    // String response = mRes.accept("application/json")
    // .type("application/json").put(String.class, input);
    // System.out.println("Request PUT " + mRes.toString() + "\nResponse: "
    // + response);
    // }
    //
    // private void createRestJournal() {
    // WebResource mRes = client.resource(apiUrl + "/resource/test:234");
    // RegalObject input = new RegalObject();
    // input.setType("journal");
    // String response = mRes.accept("application/json")
    // .type("application/json").put(String.class, input);
    // System.out.println("Request PUT " + mRes.toString() + "\nResponse: "
    // + response);
    // }
    //
    // private void createRestMonograph() {
    // WebResource mRes = client.resource(apiUrl + "/resource/test:123");
    // RegalObject input = new RegalObject();
    // input.setType("monograph");
    // String response = mRes.accept("application/json")
    // .type("application/json").put(String.class, input);
    // System.out.println("Request PUT " + mRes.toString() + "\nResponse: "
    // + response);
    // }
    //
    // private void moveMonographAsArticleToJournal() throws IOException {
    //
    // RegalObject input = new RegalObject();
    // input.setType("article");
    // input.setParentPid("test:234");
    //
    // Resource resource = new Resource();
    // String response = resource.updateResource("test:123");
    // System.out.println(response);
    // }
    //
    // private void createJournal() throws IOException {
    // RegalObject input = new RegalObject();
    // input.setType("journal");
    // Resource resource = new Resource();
    // String response = resource.create("234", "test", input);
    // System.out.println(response);
    // }
    //
    // private void createMonograph() throws IOException {
    // RegalObject input = new RegalObject();
    // input.setType("monograph");
    // Resource resource = new Resource();
    // String response = resource.updateResource(pid).create("123", "test",
    // input);
    // System.out.println(response);
    // }
    //
    // public void cleanUp() {
    // try {
    // WebResource deleteNs = client.resource(properties
    // .getProperty("apiUrl") + "/utils/deleteNamespace/test");
    // deleteNs.delete();
    //
    // WebResource deleteTestCM = client.resource(properties
    // .getProperty("apiUrl") + "/utils/deleteNamespace/testCM");
    // deleteTestCM.delete();
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
}