public class TestUtils {

    public static String prettyPrintXml(String xml) {
        try {
            javax.xml.transform.Source xmlInput = new javax.xml.transform.stream.StreamSource(
                    new java.io.StringReader(xml));
            javax.xml.transform.stream.StreamResult xmlOutput =
                    new javax.xml.transform.stream.StreamResult(new java.io.StringWriter());
            javax.xml.transform.Transformer transformer =
                    javax.xml.transform.TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            return xml;
        }
    }
}