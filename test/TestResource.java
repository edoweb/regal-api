
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
public class TestResource {
    //
    // Properties properties;
    // Client c;
    // String apiUrl;
    //
    // @Before
    // public void setUp() throws IOException {
    // properties = new Properties();
    // properties.load(getClass().getResourceAsStream("/test.properties"));
    // ClientConfig cc = new DefaultClientConfig();
    // cc.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
    // cc.getFeatures().put(ClientConfig.FEATURE_DISABLE_XML_SECURITY, true);
    // c = Client.create(cc);
    // c.addFilter(new HTTPBasicAuthFilter(properties.getProperty("user"),
    // properties.getProperty("password")));
    //
    // apiUrl = properties.getProperty("apiUrl");
    // cleanUp();
    // }
    //
    // @Test
    // public void delete() throws FileNotFoundException, IOException,
    // URISyntaxException {
    // for (ObjectType type : ObjectType.values()) {
    //
    // delete(type);
    // deleteWS(type);
    // }
    // }
    //
    // public void deleteWS(ObjectType type) throws FileNotFoundException,
    // IOException {
    // String pid = "test:d20c7a72-bb6c-40aa-bf12-ad38f7e0cb9c";
    //
    // createWS(pid, type);
    // deleteWS(pid);
    //
    // }
    //
    // public void createWS(ObjectType type) throws FileNotFoundException,
    // IOException {
    // String pid = "test:d20c7a72-bb6c-40aa-bf12-ad38f7e0cb9c";
    // createWS(pid, type);
    // }
    //
    // public void delete(ObjectType type) throws FileNotFoundException,
    // IOException {
    // String namespace = "test";
    // String pid = "d20c7a72-bb6c-40aa-bf12-ad38f7e0cb9c";
    // create(pid, namespace, type);
    // delete(pid, namespace);
    //
    // }
    //
    // public void create(ObjectType type) throws FileNotFoundException,
    // IOException {
    // String namespace = "test";
    // String pid = "d20c7a72-bb6c-40aa-bf12-ad38f7e0cb9c";
    // create(pid, namespace, type);
    // }
    //
    // private void delete(String pid, String namespace) throws IOException {
    // Resource resource = new Resource();
    // resource.deleteResource(namespace + ":" + pid);
    // resource = new Resource();
    // try {
    // resource.getReMAsJson(pid, namespace);
    // Assert.fail();
    // } catch (ArchiveException e) {
    // // TODO this looks a bit clumsy
    // }
    // }
    //
    // @Test
    // public void listAll() throws IOException {
    // Resource resource = new Resource();
    // resource.getAll("monograph", "test", 0, 10, "repo");
    //
    // }
    //
    // private void deleteWS(String pid) {
    // WebResource resource = c.resource(properties.getProperty("apiUrl")
    // + "/resource/" + pid);
    // resource.delete();
    // try {
    // resource.get(String.class);
    // Assert.fail();
    // } catch (UniformInterfaceException e) {
    // // TODO uses expected annotation instead?
    // }
    // }
    //
    // private void create(String pid, String namespace, ObjectType type)
    // throws IOException {
    // createResource(pid, namespace, type);
    // // FIXME uploadData doesn't work, see below
    // uploadDataWs(namespace + ":" + pid);
    // uploadMetadata(pid, namespace);
    // uploadDublinCore(pid, namespace);
    // testDublinCore(pid, namespace);
    // }
    //
    // @Test
    // public void create() throws IOException, InterruptedException {
    // create("123", "test", ObjectType.monograph);
    // addTransformer("123", "test", "testepicur");
    // Thread.sleep(10000);
    // List<String> pids = list("monograph", "test", 0, 10, "repo");
    // Assert.assertEquals(1, pids.size());
    // pids = list("transformer", "CM", 0, 10, "repo");
    // List<String> cms = getTransformers("123", "test");
    // Assert.assertEquals(1, cms.size());
    // Assert.assertEquals("testepicur", cms.get(0));
    // }
    //
    // private List<String> getTransformers(String pid, String namespace)
    // throws IOException {
    // Resource resource = new Resource();
    // CreateObjectBean b = (CreateObjectBean) resource.getObjectAsJson(pid,
    // namespace).getEntity();
    // return b.getTransformer();
    // }
    //
    // private List<String> list(String type, String namespace, int from,
    // int until, String source) throws IOException {
    // Resource resource = new Resource();
    // ObjectList list = (ObjectList) resource.getAll(type, namespace, from,
    // until, source).getEntity();
    // return list.list;
    // }
    //
    // private void addTransformer(String p, String namespace, String
    // transformerId)
    // throws IOException {
    // Resource resource = new Resource();
    // CreateObjectBean input = (CreateObjectBean) resource.getObjectAsJson(p,
    // namespace).getEntity();
    // List<String> ts = input.getTransformer();
    // ts.add(transformerId);
    // input.setTransformer(ts);
    // resource.create(p, namespace, input);
    //
    // }
    //
    // private void testDublinCore(String pid, String namespace)
    // throws IOException {
    // Resource resource = new Resource();
    // DCBeanAnnotated dc = resource.readDC(pid, namespace);
    // Assert.assertEquals("Test", dc.getCreator().get(0));
    // }
    //
    // private void uploadDublinCore(String pid, String namespace)
    // throws IOException {
    // Resource resource = new Resource();
    // DCBeanAnnotated dc = new DCBeanAnnotated();
    // dc.addCreator("Test");
    // resource.updateDC(pid, namespace, dc);
    // }
    //
    // private void uploadMetadata(String pid, String namespace)
    // throws IOException {
    // Resource resource = new Resource();
    // String content = CopyUtils.copyToString(Thread.currentThread()
    // .getContextClassLoader().getResourceAsStream("test.nt"),
    // "utf-8");
    // resource.updateMetadata(pid, namespace, content);
    //
    // }
    //
    // /**
    // * FIXME see below
    // */
    // @SuppressWarnings("unused")
    // private void uploadData(String pid, String namespace) throws IOException
    // {
    // Resource Resource = new Resource();
    // MultiPart multiPart = new MultiPart();
    // multiPart.bodyPart(new StreamDataBodyPart("InputStream", Thread
    // .currentThread().getContextClassLoader()
    // .getResourceAsStream("test.pdf")));
    // multiPart.bodyPart(new BodyPart("application/pdf",
    // MediaType.TEXT_PLAIN_TYPE));
    // multiPart.bodyPart(new BodyPart("test.pdf", MediaType.TEXT_PLAIN_TYPE));
    // Resource.updateData(pid, namespace, multiPart);
    //
    // }
    //
    // private void createResource(String pid, String namespace, ObjectType
    // type)
    // throws IOException {
    // Resource resource = new Resource();
    // resource.create(pid, namespace, new CreateObjectBean(type));
    // }
    //
    // private void createWS(String pid, ObjectType type) throws IOException {
    //
    // createResourceWs(pid, type);
    // uploadDataWs(pid);
    // uploadMetadataWs(pid);
    // uploadDublinCoreWs(pid);
    // testDublinCoreWs(pid);
    // }
    //
    // private void testDublinCoreWs(String pid) {
    // WebResource dc = c.resource(apiUrl + "/resource/" + pid + "/dc");
    // DCBeanAnnotated content = dc.get(DCBeanAnnotated.class);
    // Assert.assertEquals("Test", content.getCreator().get(0));
    // }
    //
    // private void uploadDublinCoreWs(String pid) {
    // WebResource dc = c.resource(apiUrl + "/resource/" + pid + "/dc");
    // DCBeanAnnotated content = dc.get(DCBeanAnnotated.class);
    // Vector<String> v = new Vector<String>();
    // v.add("Test");
    // content.setCreator(v);
    // dc.put(content);
    // }
    //
    // private void uploadMetadataWs(String pid) throws IOException {
    // WebResource metadata = c.resource(apiUrl + "/resource/" + pid
    // + "/metadata");
    // byte[] content = IOUtils.toByteArray(Thread.currentThread()
    // .getContextClassLoader().getResourceAsStream("test.nt"));
    // metadata.type("text/plain").post(content);
    // }
    //
    // private void uploadDataWs(String pid) {
    // WebResource data = c.resource(apiUrl + "/resource/" + pid + "/data");
    // MultiPart multiPart = new MultiPart();
    // multiPart.bodyPart(new StreamDataBodyPart("InputStream", Thread
    // .currentThread().getContextClassLoader()
    // .getResourceAsStream("test.pdf")));
    // multiPart.bodyPart(new BodyPart("application/pdf",
    // MediaType.TEXT_PLAIN_TYPE));
    // multiPart.bodyPart(new BodyPart("test.pdf", MediaType.TEXT_PLAIN_TYPE));
    // data.type("multipart/mixed").post(multiPart);
    // }
    //
    // private WebResource createResourceWs(String pid, ObjectType type) {
    // WebResource dc = c.resource(apiUrl + "/resource/" + pid);
    // String response = dc.put(String.class, new CreateObjectBean(type));
    // System.out.println(response);
    // return dc;
    // }
    //
    // @Test
    // public void deleteData() throws IOException {
    // Resource resource = new Resource();
    // create("123", "test", ObjectType.monograph);
    // resource.readData("123", "test");
    // resource.deleteMetadata("123", "test");
    // resource.deleteData("123", "test");
    // try {
    // resource.readData("123", "test");
    // } catch (HttpArchiveException e) {
    // Assert.assertEquals(404, e.getResponse().getStatus());
    // }
    // }
    //
    // @Test
    // public void deleteMetadata() throws IOException {
    // Resource resource = new Resource();
    // create("123", "test", ObjectType.monograph);
    // resource.readMetadata("123", "test");
    // resource.deleteMetadata("123", "test");
    // try {
    // resource.readMetadata("123", "test");
    // } catch (HttpArchiveException e) {
    // Assert.assertEquals(404, e.getResponse().getStatus());
    // }
    // }
    //
    // public void createTransformer() throws IOException {
    // Utils utils = new Utils();
    // create("123", "test", ObjectType.monograph);
    // List<Transformer> transformers = new Vector<Transformer>();
    // transformers.add(new Transformer("testepicur", "epicur", apiUrl
    // + "/resource/(pid).epicur"));
    // transformers.add(new Transformer("testoaidc", "oaidc", apiUrl
    // + "/resource/(pid).oaidc"));
    // transformers.add(new Transformer("testpdfa", "pdfa", apiUrl
    // + "/resource/(pid).pdfa"));
    //
    // utils.initContentModels("test");
    // }
    //
    // @Test
    // public void removeNodesTransformer() throws IOException {
    // createTransformer();
    // create("123", "test", ObjectType.monograph);
    // addTransformer("123", "test", "testepicur");
    // addTransformer("123", "test", "testoaidc");
    // addTransformer("123", "test", "testpdfa");
    // CreateObjectBean input = readRegalJson("123", "test");
    // List<String> ts = new Vector<String>();
    // ts.add("testoaidc");
    // ts.add("testpdfa");
    // input.setTransformer(ts);
    // createResource(input, "123", "test");
    // input = readRegalJson("123", "test");
    //
    // ts = input.getTransformer();
    // Assert.assertEquals(2, ts.size());
    // for (int i = 0; i < ts.size(); i++) {
    // Assert.assertFalse(ts.get(i).equals("testepicur"));
    // }
    //
    // input = new CreateObjectBean();
    // input.setTransformer(ts);
    // input.setType("monograph");
    // input.setParentPid(null);
    // createResource(input, "123", "test");
    // input = readRegalJson("123", "test");
    //
    // HashMap<String, String> map = new HashMap<String, String>();
    // map.put("testoaidc", "testoaidc");
    // map.put("testpdfa", "testpdfa");
    // ts = input.getTransformer();
    // Assert.assertEquals(2, ts.size());
    // for (int i = 0; i < ts.size(); i++) {
    // Assert.assertTrue(map.containsKey(ts.get(i)));
    // }
    // for (int i = 0; i < ts.size(); i++) {
    // Assert.assertFalse(ts.get(i).equals("testepicur"));
    // }
    // }
    //
    // private void createResource(RegalObject input, String pid, String
    // namespace)
    // throws IOException {
    // Resource resource = new Resource();
    // resource.create(pid, namespace, input);
    //
    // }
    //
    // @Test
    // public void readNodesTransformer() throws IOException {
    // createTransformer();
    // create("123", "test", ObjectType.monograph);
    // addTransformer("123", "test", "testepicur");
    // addTransformer("123", "test", "testoaidc");
    // addTransformer("123", "test", "testpdfa");
    //
    // CreateObjectBean input = readRegalJson("123", "test");
    // List<String> ts = input.getTransformer();
    // HashMap<String, String> map = new HashMap<String, String>();
    // map.put("testepicur", "testepicur");
    // map.put("testoaidc", "testoaidc");
    // map.put("testpdfa", "testpdfa");
    // Assert.assertEquals(3, ts.size());
    // for (int i = 0; i < ts.size(); i++) {
    // System.out.println(ts.get(i));
    // Assert.assertTrue(map.containsKey(ts.get(i)));
    // }
    // }
    //
    // private RegalObject readRegalJson(String pid, String namespace)
    // throws IOException {
    // Resource resource = new Resource();
    // return (RegalObject) resource.getObjectAsJson(pid, namespace)
    // .getEntity();
    // }
    //
    // @After
    // public void tearDown() {
    // // cleanUp();
    // }
    //
    // public void cleanUp() {
    // try {
    // WebResource deleteNs = c.resource(properties.getProperty("apiUrl")
    // + "/utils/deleteNamespace/test");
    // deleteNs.delete();
    //
    // WebResource deleteTestCM = c.resource(properties
    // .getProperty("apiUrl") + "/utils/deleteByQuery/CM:test*");
    // deleteTestCM.delete();
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
}