/*    */ package com.okta.saml;
/*    */ 
/*    */ import java.security.cert.X509Certificate;
/*    */ import java.util.ArrayList;
/*    */ import org.opensaml.security.credential.Credential;
/*    */ import org.opensaml.security.x509.BasicX509Credential;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Certificate
/*    */ {
/*    */   private final X509Certificate certificate;
/*    */   
/*    */   public Certificate(X509Certificate x509Cert) {
/* 17 */     this.certificate = x509Cert;
/*    */   }
/*    */   
/*    */   public X509Certificate getX509Cert() {
/* 21 */     return this.certificate;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public Credential getCredential() {
/* 29 */     BasicX509Credential credential = new BasicX509Credential(this.certificate);
/* 30 */     credential.setCRLs(new ArrayList());
/* 31 */     return (Credential)credential;
/*    */   }
/*    */ }


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\saml\Certificate.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */