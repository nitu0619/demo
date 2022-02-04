/*    */ package com.okta.saml;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.io.FileInputStream;
/*    */ import java.io.FileNotFoundException;
/*    */ import java.io.IOException;
/*    */ import java.nio.MappedByteBuffer;
/*    */ import java.nio.channels.FileChannel;
/*    */ import java.nio.charset.StandardCharsets;
/*    */ import org.slf4j.Logger;
/*    */ import org.slf4j.LoggerFactory;
/*    */ 
/*    */ 
/*    */ 
/*    */ public abstract class AbstractSAMLValidator
/*    */   implements SAMLValidator
/*    */ {
/* 18 */   private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSAMLValidator.class);
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public Configuration getConfiguration(String config) throws SAMLException {
/*    */     try {
/* 29 */       return new Configuration(config);
/* 30 */     } catch (Exception e) {
/* 31 */       LOGGER.error("Failed to create new Configuration instance", e);
/* 32 */       throw new SAMLException("Problem parsing the configuration.");
/*    */     } 
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public Configuration getConfigurationFrom(String path) throws SAMLException, IOException {
/* 45 */     try (FileInputStream stream = new FileInputStream(new File(path))) {
/* 46 */       FileChannel channel = stream.getChannel();
/* 47 */       MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0L, channel.size());
/* 48 */       String config = StandardCharsets.UTF_8.decode(buffer).toString();
/* 49 */       return getConfiguration(config);
/* 50 */     } catch (FileNotFoundException e) {
/* 51 */       LOGGER.error("File not found. Current path: " + (new File(".")).getAbsolutePath());
/* 52 */       throw e;
/*    */     } 
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public SAMLRequest getSAMLRequest(Application application) {
/* 64 */     return new SAMLRequest(application);
/*    */   }
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
/*    */   public SAMLResponse getSAMLResponse(String responseString, Configuration configuration) throws SAMLException {
/* 77 */     return new SAMLResponse(responseString, configuration);
/*    */   }
/*    */ }


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\saml\AbstractSAMLValidator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */