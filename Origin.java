/*    */ package com.okta.saml.util;
/*    */ 
/*    */ import java.net.MalformedURLException;
/*    */ import java.net.URL;
/*    */ import org.apache.commons.lang.StringUtils;
/*    */ import org.apache.commons.lang.builder.EqualsBuilder;
/*    */ import org.apache.commons.lang.builder.HashCodeBuilder;
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Origin
/*    */ {
/*    */   private final String rawValue;
/*    */   private final URL urlValue;
/*    */   
/*    */   public Origin(String rawValue) {
/* 17 */     if (StringUtils.isEmpty(rawValue)) {
/* 18 */       throw new NullPointerException("Origin rawValue cannot be empty");
/*    */     }
/*    */     try {
/* 21 */       this.urlValue = new URL(rawValue);
/* 22 */     } catch (MalformedURLException e) {
/* 23 */       throw new IllegalArgumentException("Provided URL is invalid!", e);
/*    */     } 
/* 25 */     this.rawValue = rawValue;
/*    */   }
/*    */   
/*    */   public URL getURLValue() {
/* 29 */     return this.urlValue;
/*    */   }
/*    */   
/*    */   public String getRawValue() {
/* 33 */     return this.rawValue;
/*    */   }
/*    */   
/*    */   public String getOriginAsString() {
/* 37 */     if (this.urlValue.getPort() == -1) {
/* 38 */       return String.format("%s://%s", new Object[] { this.urlValue.getProtocol(), this.urlValue.getHost() });
/*    */     }
/* 40 */     return String.format("%s://%s:%d", new Object[] { this.urlValue.getProtocol(), this.urlValue.getHost(), Integer.valueOf(this.urlValue.getPort()) });
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public int hashCode() {
/* 46 */     if (this.urlValue != null) {
/* 47 */       return (new HashCodeBuilder(23, 631)).append(this.urlValue.getProtocol()).append(this.urlValue.getHost()).append(this.urlValue.getPort()).toHashCode();
/*    */     }
/* 49 */     return (new HashCodeBuilder(23, 631)).toHashCode();
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public boolean equals(Object o) {
/* 55 */     if (o == null) {
/* 56 */       return false;
/*    */     }
/*    */     
/* 59 */     if (o == this) {
/* 60 */       return true;
/*    */     }
/*    */     
/* 63 */     if (o instanceof Origin) {
/* 64 */       Origin other = (Origin)o;
/*    */       
/* 66 */       if (getURLValue() == null)
/* 67 */         return (other.getURLValue() == null); 
/* 68 */       if (other.getURLValue() == null) {
/* 69 */         return false;
/*    */       }
/*    */       
/* 72 */       EqualsBuilder bldr = new EqualsBuilder();
/* 73 */       bldr.append(getURLValue().getProtocol(), other.getURLValue().getProtocol());
/* 74 */       bldr.append(getURLValue().getHost(), other.getURLValue().getHost());
/* 75 */       bldr.append(getURLValue().getPort(), other.getURLValue().getPort());
/* 76 */       return bldr.isEquals();
/*    */     } 
/*    */     
/* 79 */     return false;
/*    */   }
/*    */ 
/*    */   
/*    */   public String toString() {
/* 84 */     return this.rawValue;
/*    */   }
/*    */ }


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\sam\\util\Origin.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */