/*    */ package com.okta.saml;
/*    */ 
/*    */ import org.opensaml.core.config.InitializationService;
/*    */ import org.slf4j.Logger;
/*    */ import org.slf4j.LoggerFactory;
/*    */ 
/*    */ 
/*    */ 
/*    */ public class RegularSAMLValidator
/*    */   extends AbstractSAMLValidator
/*    */ {
/* 12 */   private static final Logger LOGGER = LoggerFactory.getLogger(RegularSAMLValidator.class);
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public RegularSAMLValidator() throws SAMLException {
/*    */     try {
/* 20 */       InitializationService.initialize();
/* 21 */     } catch (Exception e) {
/* 22 */       LOGGER.error(e.getMessage(), e);
/* 23 */       throw new SAMLException("Problem while bootstrapping openSAML library");
/*    */     } 
/*    */   }
/*    */ }


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\saml\RegularSAMLValidator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */