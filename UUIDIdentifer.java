/*    */ package com.okta.saml.util;
/*    */ 
/*    */ import java.util.UUID;
/*    */ 
/*    */ 
/*    */ 
/*    */ public class UUIDIdentifer
/*    */   implements Identifier
/*    */ {
/*    */   public String getId() {
/* 11 */     return UUID.randomUUID().toString();
/*    */   }
/*    */ }


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\sam\\util\UUIDIdentifer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */