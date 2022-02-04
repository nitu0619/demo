/*    */ package com.okta.saml.util;
/*    */ 
/*    */ import org.apache.commons.lang3.StringUtils;
/*    */ import org.apache.commons.lang3.text.StrTokenizer;
/*    */ import org.slf4j.Logger;
/*    */ import org.slf4j.LoggerFactory;
/*    */ import sun.net.util.IPAddressUtil;
/*    */ 
/*    */ public class IPRange
/*    */ {
/*    */   private String[] startAddress;
/*    */   private String[] endAddress;
/* 13 */   private static final Logger LOGGER = LoggerFactory.getLogger(IPRange.class);
/*    */   
/*    */   public IPRange(String startAddress, String endAddress) {
/* 16 */     validateIp(startAddress);
/* 17 */     this.startAddress = parseAddressString(startAddress);
/*    */     
/* 19 */     if (endAddress != null && !endAddress.trim().isEmpty()) {
/* 20 */       validateIp(endAddress);
/* 21 */       this.endAddress = parseAddressString(endAddress);
/*    */     } 
/*    */   }
/*    */   
/*    */   public void validateIp(String ip) {
/* 26 */     boolean valid = true;
/* 27 */     String[] parts = parseAddressString(ip);
/* 28 */     if (parts == null) {
/* 29 */       throw new NumberFormatException(String.format("Failed to parse address %s", new Object[] { ip }));
/*    */     }
/* 31 */     for (String item : parts) {
/* 32 */       if (StringUtils.isNumeric(item)) {
/* 33 */         int val = Integer.parseInt(item);
/* 34 */         if (val < 0 || val > 255) {
/* 35 */           valid = false;
/*    */         }
/* 37 */       } else if (!StringUtils.equals(item, "*")) {
/* 38 */         valid = false;
/*    */       } 
/*    */     } 
/* 41 */     if (!valid) {
/* 42 */       throw new NumberFormatException(String.format("All IP address segments should be from 0 to 255 or *, %s provided", new Object[] { ip }));
/*    */     }
/*    */   }
/*    */   
/*    */   public String[] parseAddressString(String address) {
/* 47 */     String[] retVal = new String[4];
/* 48 */     if (IPAddressUtil.isIPv6LiteralAddress(address)) {
/* 49 */       LOGGER.error(String.format("IPv6 address provided: %s, IP range mechanism will not be available.", new Object[] { address }));
/* 50 */       return null;
/*    */     } 
/* 52 */     StrTokenizer tok = new StrTokenizer(address, ".");
/* 53 */     int i = 0;
/* 54 */     while (tok.hasNext()) {
/* 55 */       retVal[i++] = tok.next();
/*    */     }
/* 57 */     return retVal;
/*    */   }
/*    */   
/*    */   public boolean isAddressInRange(String address) {
/* 61 */     String[] startAddress = getStartAddress();
/* 62 */     String[] endAddress = getEndAddress();
/* 63 */     String[] testAddress = parseAddressString(address);
/*    */     
/* 65 */     validateIp(address);
/*    */     
/* 67 */     if (null == endAddress) {
/* 68 */       for (int iPos = 0; iPos < 4; iPos++) {
/* 69 */         if (!StringUtils.equals(startAddress[iPos], "*") && !StringUtils.equals(startAddress[iPos], testAddress[iPos])) {
/* 70 */           return false;
/*    */         }
/*    */       } 
/*    */     } else {
/* 74 */       for (int iPos = 0; iPos < 4; iPos++) {
/* 75 */         int startValue = StringUtils.equals(startAddress[iPos], "*") ? 0 : Integer.parseInt(startAddress[iPos]);
/* 76 */         int endValue = StringUtils.equals(endAddress[iPos], "*") ? 255 : Integer.parseInt(endAddress[iPos]);
/* 77 */         int testAddressValue = Integer.parseInt(testAddress[iPos]);
/* 78 */         if (testAddressValue < startValue || testAddressValue > endValue) {
/* 79 */           return false;
/*    */         }
/*    */       } 
/*    */     } 
/* 83 */     return true;
/*    */   }
/*    */   
/*    */   public String[] getStartAddress() {
/* 87 */     return this.startAddress;
/*    */   }
/*    */   
/*    */   public void setStartAddress(String[] startAddress) {
/* 91 */     this.startAddress = startAddress;
/*    */   }
/*    */   
/*    */   public String[] getEndAddress() {
/* 95 */     return this.endAddress;
/*    */   }
/*    */   
/*    */   public void setEndAddress(String[] endAddress) {
/* 99 */     this.endAddress = endAddress;
/*    */   }
/*    */ }


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\sam\\util\IPRange.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */