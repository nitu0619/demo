/*    */ package com.okta.saml;
/*    */ 
/*    */ import java.util.Collections;
/*    */ import java.util.HashMap;
/*    */ import java.util.Iterator;
/*    */ import java.util.Map;
/*    */ import javax.xml.namespace.NamespaceContext;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class MetadataNamespaceContext
/*    */   implements NamespaceContext
/*    */ {
/*    */   private static Map<String, String> objectNamespaces;
/*    */   
/*    */   static {
/* 20 */     Map<String, String> namespaces = new HashMap<>();
/* 21 */     namespaces.put("saml2p", "urn:oasis:names:tc:SAML:2.0:protocol");
/* 22 */     namespaces.put("saml2", "urn:oasis:names:tc:SAML:2.0:assertion");
/* 23 */     namespaces.put("md", "urn:oasis:names:tc:SAML:2.0:metadata");
/* 24 */     namespaces.put("ds", "http://www.w3.org/2000/09/xmldsig#");
/* 25 */     namespaces.put("xs", "http://www.w3.org/2001/XMLSchema");
/* 26 */     namespaces.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
/* 27 */     namespaces.put("ec", "http://www.w3.org/2001/10/xml-exc-c14n#");
/* 28 */     namespaces.put("xml", "http://www.w3.org/XML/1998/namespace");
/* 29 */     objectNamespaces = Collections.unmodifiableMap(namespaces);
/*    */   }
/*    */   
/*    */   public String getNamespaceURI(String prefix) {
/* 33 */     String namespace = objectNamespaces.get(prefix);
/*    */     
/* 35 */     if (namespace == null) {
/* 36 */       return "";
/*    */     }
/* 38 */     return namespace;
/*    */   }
/*    */ 
/*    */   
/*    */   public String getPrefix(String uri) {
/* 43 */     throw new UnsupportedOperationException();
/*    */   }
/*    */   
/*    */   public Iterator getPrefixes(String uri) {
/* 47 */     throw new UnsupportedOperationException();
/*    */   }
/*    */ }


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\saml\MetadataNamespaceContext.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */