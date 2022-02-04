/*    */ package com.okta.saml;
/*    */ 
/*    */ import java.security.cert.CertificateException;
/*    */ import org.opensaml.core.xml.XMLObject;
/*    */ import org.opensaml.core.xml.io.UnmarshallingException;
/*    */ import org.opensaml.core.xml.util.XMLObjectSupport;
/*    */ import org.opensaml.saml.saml2.metadata.EntityDescriptor;
/*    */ import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
/*    */ import org.opensaml.saml.saml2.metadata.KeyDescriptor;
/*    */ import org.opensaml.saml.saml2.metadata.SingleSignOnService;
/*    */ import org.opensaml.security.credential.UsageType;
/*    */ import org.opensaml.xmlsec.keyinfo.KeyInfoSupport;
/*    */ import org.opensaml.xmlsec.signature.X509Certificate;
/*    */ import org.opensaml.xmlsec.signature.X509Data;
/*    */ import org.slf4j.Logger;
/*    */ import org.slf4j.LoggerFactory;
/*    */ import org.w3c.dom.Element;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Application
/*    */ {
/* 26 */   private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
/*    */ 
/*    */   
/*    */   private EntityDescriptor descriptor;
/*    */ 
/*    */   
/*    */   private SingleSignOnService ssoPost;
/*    */ 
/*    */   
/*    */   private Certificate certificate;
/*    */ 
/*    */   
/*    */   public Application(Element application) throws SAMLException {
/*    */     try {
/* 40 */       Element entity = (Element)application.getElementsByTagName("md:EntityDescriptor").item(0);
/*    */       
/* 42 */       XMLObject root = XMLObjectSupport.getUnmarshaller(entity).unmarshall(entity);
/* 43 */       EntityDescriptor descriptor = (EntityDescriptor)root;
/* 44 */       this.descriptor = descriptor;
/*    */       
/* 46 */       IDPSSODescriptor idpSSO = descriptor.getIDPSSODescriptor("urn:oasis:names:tc:SAML:2.0:protocol");
/* 47 */       for (SingleSignOnService sso : idpSSO.getSingleSignOnServices()) {
/* 48 */         if (sso.getBinding().equals("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST")) {
/* 49 */           this.ssoPost = sso;
/*    */         }
/*    */       } 
/*    */       
/* 53 */       for (KeyDescriptor keyDescriptor : idpSSO.getKeyDescriptors()) {
/* 54 */         if (keyDescriptor.getUse().equals(UsageType.SIGNING)) {
/*    */           try {
/* 56 */             X509Certificate x509Cert = ((X509Data)keyDescriptor.getKeyInfo().getX509Datas().get(0)).getX509Certificates().get(0);
/* 57 */             this.certificate = new Certificate(KeyInfoSupport.getCertificate(x509Cert));
/* 58 */           } catch (NullPointerException e) {
/* 59 */             throw new SAMLException("X509Certificate field is missing from the configuration file");
/*    */           } 
/*    */           break;
/*    */         } 
/*    */       } 
/* 64 */     } catch (UnmarshallingException e) {
/* 65 */       LOGGER.debug(e.getMessage());
/* 66 */       throw new SAMLException("There was a problem while parsing EntityDescriptor from the configuration file");
/* 67 */     } catch (CertificateException e) {
/* 68 */       LOGGER.debug(e.getMessage());
/* 69 */       throw new SAMLException("There's a problem with the certificate");
/*    */     } 
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public String getIssuer() {
/* 77 */     return this.descriptor.getEntityID();
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public String getAuthenticationURL() {
/* 84 */     return this.ssoPost.getLocation();
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public Certificate getCertificate() {
/* 91 */     return this.certificate;
/*    */   }
/*    */ }


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\saml\Application.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */