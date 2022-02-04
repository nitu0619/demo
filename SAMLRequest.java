/*    */ package com.okta.saml;
/*    */ 
/*    */ import com.google.inject.Inject;
/*    */ import com.okta.saml.util.Clock;
/*    */ import com.okta.saml.util.Identifier;
/*    */ import com.okta.saml.util.SimpleClock;
/*    */ import com.okta.saml.util.UUIDIdentifer;
/*    */ import java.io.ByteArrayOutputStream;
/*    */ import java.nio.charset.StandardCharsets;
/*    */ import javax.xml.namespace.QName;
/*    */ import org.opensaml.core.xml.XMLObject;
/*    */ import org.opensaml.core.xml.util.XMLObjectSupport;
/*    */ import org.opensaml.saml.common.SAMLVersion;
/*    */ import org.opensaml.saml.saml2.core.AuthnContextClassRef;
/*    */ import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
/*    */ import org.opensaml.saml.saml2.core.AuthnRequest;
/*    */ import org.opensaml.saml.saml2.core.Issuer;
/*    */ import org.opensaml.saml.saml2.core.NameIDPolicy;
/*    */ import org.opensaml.saml.saml2.core.RequestedAuthnContext;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class SAMLRequest
/*    */ {
/*    */   private final AuthnRequest request;
/*    */   
/*    */   public SAMLRequest(Application application) {
/* 37 */     this(application, (Identifier)new UUIDIdentifer(), (Clock)new SimpleClock());
/*    */   }
/*    */   
/*    */   @Inject
/*    */   private SAMLRequest(Application application, Identifier identifier, Clock clock) {
/* 42 */     this.request = build(AuthnRequest.DEFAULT_ELEMENT_NAME);
/* 43 */     this.request.setID(identifier.getId());
/* 44 */     this.request.setVersion(SAMLVersion.VERSION_20);
/* 45 */     this.request.setIssueInstant(clock.dateTimeNow());
/* 46 */     this.request.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
/* 47 */     this.request.setAssertionConsumerServiceURL(application.getAuthenticationURL());
/*    */     
/* 49 */     Issuer issuer = build(Issuer.DEFAULT_ELEMENT_NAME);
/* 50 */     issuer.setValue(application.getIssuer());
/* 51 */     this.request.setIssuer(issuer);
/*    */     
/* 53 */     NameIDPolicy nameIDPolicy = build(NameIDPolicy.DEFAULT_ELEMENT_NAME);
/* 54 */     nameIDPolicy.setFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
/* 55 */     this.request.setNameIDPolicy(nameIDPolicy);
/*    */     
/* 57 */     RequestedAuthnContext requestedAuthnContext = build(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
/* 58 */     requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
/* 59 */     this.request.setRequestedAuthnContext(requestedAuthnContext);
/*    */     
/* 61 */     AuthnContextClassRef authnContextClassRef = build(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
/* 62 */     authnContextClassRef.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
/* 63 */     requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);
/*    */   }
/*    */ 
/*    */   
/*    */   private <T extends org.opensaml.saml.common.SAMLObject> T build(QName qName) {
/* 68 */     return (T)XMLObjectSupport.getBuilder(qName).buildObject(qName);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public String toString() {
/*    */     try {
/* 76 */       ByteArrayOutputStream baos = new ByteArrayOutputStream();
/* 77 */       XMLObjectSupport.marshallToOutputStream((XMLObject)this.request, baos);
/*    */       
/* 79 */       return baos.toString(StandardCharsets.UTF_8.name());
/* 80 */     } catch (Exception e) {
/* 81 */       return null;
/*    */     } 
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public AuthnRequest getAuthnRequest() {
/* 89 */     return this.request;
/*    */   }
/*    */ }


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\saml\SAMLRequest.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */