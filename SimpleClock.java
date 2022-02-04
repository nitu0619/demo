/*    */ package com.okta.saml.util;
/*    */ 
/*    */ import org.joda.time.DateTime;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class SimpleClock
/*    */   implements Clock
/*    */ {
/*    */   public String instant() {
/* 12 */     return (new DateTime()).toInstant().toString();
/*    */   }
/*    */ 
/*    */   
/*    */   public DateTime dateTimeNow() {
/* 17 */     return new DateTime();
/*    */   }
/*    */ }


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\sam\\util\SimpleClock.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */