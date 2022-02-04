/*    */ package com.okta.saml;
/*    */ 
/*    */ import org.opensaml.core.config.InitializationService;
/*    */ import org.slf4j.Logger;
/*    */ import org.slf4j.LoggerFactory;
/*    */ 
/*    */ 
/*    */ public class OSGiSafeSAMLValidator
/*    */   extends AbstractSAMLValidator
/*    */ {
/* 11 */   private static final Logger LOGGER = LoggerFactory.getLogger(OSGiSafeSAMLValidator.class);
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public OSGiSafeSAMLValidator() throws SAMLException {
/* 21 */     Thread thread = Thread.currentThread();
/* 22 */     ClassLoader loader = thread.getContextClassLoader();
/* 23 */     thread.setContextClassLoader(InitializationService.class.getClassLoader());
/*    */     try {
/* 25 */       InitializationService.initialize();
/* 26 */     } catch (Exception e) {
/* 27 */       LOGGER.error(e.getMessage(), e);
/* 28 */       throw new SAMLException("Problem while bootstrapping openSAML library", e);
/*    */     } finally {
/* 30 */       thread.setContextClassLoader(loader);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\saml\OSGiSafeSAMLValidator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */