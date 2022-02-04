/*     */ package com.okta.saml.util;
/*     */ 
/*     */ import com.okta.saml.Application;
/*     */ import com.okta.saml.Configuration;
/*     */ import com.okta.saml.OSGiSafeSAMLValidator;
/*     */ import com.okta.saml.SAMLException;
/*     */ import com.okta.saml.SAMLRequest;
/*     */ import com.okta.saml.SAMLResponse;
/*     */ import com.okta.saml.SAMLValidator;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.net.URLEncoder;
/*     */ import java.nio.MappedByteBuffer;
/*     */ import java.nio.channels.FileChannel;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.security.Principal;
/*     */ import java.util.Collection;
/*     */ import java.util.List;
/*     */ import javax.servlet.http.HttpServletRequest;
/*     */ import javax.servlet.http.HttpSession;
/*     */ import org.apache.commons.codec.binary.Base64;
/*     */ import org.apache.commons.lang.StringUtils;
/*     */ import org.slf4j.Logger;
/*     */ import org.slf4j.LoggerFactory;
/*     */ 
/*     */ 
/*     */ public class OktaAuthPeer
/*     */ {
/*  31 */   private static final Logger LOGGER = LoggerFactory.getLogger(OktaAuthPeer.class);
/*     */   
/*     */   private String loggedInKey;
/*     */   private String loggedOutKey;
/*     */   private String configFilePath;
/*     */   private SAMLValidator validator;
/*     */   private Configuration configuration;
/*     */   private Application defaultApplication;
/*     */   private List<String> filteredHeaders;
/*     */   
/*     */   public OktaAuthPeer(String configFilePath, String loggedInKey, String loggedOutKey) throws SAMLException, IOException {
/*  42 */     init(configFilePath, loggedInKey, loggedOutKey);
/*     */   }
/*     */ 
/*     */   
/*     */   public OktaAuthPeer() {}
/*     */   
/*     */   public void init(String configFilePath, String loggedInKey, String loggedOutKey) throws SAMLException, IOException {
/*  49 */     this.loggedInKey = loggedInKey;
/*  50 */     this.loggedOutKey = loggedOutKey;
/*  51 */     this.configFilePath = configFilePath;
/*     */     
/*  53 */     String file = readFile(configFilePath);
/*  54 */     this.validator = (SAMLValidator)new OSGiSafeSAMLValidator();
/*  55 */     this.configuration = this.validator.getConfiguration(file);
/*     */     
/*  57 */     for (Application application : this.configuration.getApplications().values()) {
/*  58 */       if (this.defaultApplication == null) {
/*  59 */         this.defaultApplication = application;
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void putPrincipalInSessionContext(HttpServletRequest request, Principal principal) {
/*  66 */     HttpSession httpSession = request.getSession();
/*  67 */     httpSession.setAttribute(this.loggedInKey, principal);
/*  68 */     httpSession.setAttribute(this.loggedOutKey, null);
/*     */   }
/*     */   
/*     */   public boolean isPrincipalAlreadyInSessionContext(HttpServletRequest request, Principal principal) {
/*  72 */     Principal currentPrincipal = (Principal)request.getSession().getAttribute(this.loggedInKey);
/*  73 */     return (currentPrincipal != null && currentPrincipal.getName() != null && principal != null && currentPrincipal.getName().equals(principal.getName()));
/*     */   }
/*     */   
/*     */   public void removePrincipalFromSessionContext(HttpServletRequest request) {
/*  77 */     HttpSession httpSession = request.getSession();
/*  78 */     httpSession.setAttribute(this.loggedInKey, null);
/*  79 */     httpSession.setAttribute(this.loggedOutKey, Boolean.TRUE);
/*     */   }
/*     */   
/*     */   public Principal getUserPrincipal(final SAMLResponse response) {
/*  83 */     return new Principal() {
/*     */         public String getName() {
/*  85 */           return response.getUserID();
/*     */         }
/*     */       };
/*     */   }
/*     */   
/*     */   public String readFile(String path) throws IOException {
/*  91 */     FileInputStream stream = new FileInputStream(new File(path));
/*     */     try {
/*  93 */       FileChannel fc = stream.getChannel();
/*  94 */       MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0L, fc.size());
/*  95 */       return StandardCharsets.UTF_8.decode(bb).toString();
/*     */     } finally {
/*  97 */       stream.close();
/*     */     } 
/*     */   }
/*     */   
/*     */   public String getAuthRedirectUrl(String redirectUrl, String relayState) {
/* 102 */     if (!redirectUrl.contains("RelayState") && relayState != null && !relayState.isEmpty()) {
/*     */       try {
/* 104 */         relayState = URLEncoder.encode(relayState, "UTF-8");
/* 105 */       } catch (Exception e) {
/* 106 */         LOGGER.warn("Error while encoding relayState = " + relayState);
/*     */       } 
/*     */       
/* 109 */       if (redirectUrl.contains("?")) {
/* 110 */         redirectUrl = redirectUrl + "&";
/*     */       } else {
/* 112 */         redirectUrl = redirectUrl + "?";
/*     */       } 
/* 114 */       redirectUrl = redirectUrl + "RelayState=" + relayState;
/*     */     } 
/* 116 */     return redirectUrl;
/*     */   }
/*     */   
/*     */   public SAMLRequest getSAMLRequest() {
/* 120 */     return getValidator().getSAMLRequest(this.defaultApplication);
/*     */   }
/*     */   
/*     */   public SAMLResponse getSAMLResponse(String assertion) throws UnsupportedEncodingException, SAMLException {
/* 124 */     assertion = new String(Base64.decodeBase64(assertion.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
/* 125 */     return getValidator().getSAMLResponse(assertion, getConfiguration());
/*     */   }
/*     */   
/*     */   public SAMLValidator getValidator() {
/* 129 */     return this.validator;
/*     */   }
/*     */   
/*     */   public void setValidator(SAMLValidator validator) {
/* 133 */     this.validator = validator;
/*     */   }
/*     */   
/*     */   public Configuration getConfiguration() {
/* 137 */     return this.configuration;
/*     */   }
/*     */   
/*     */   public void setConfiguration(Configuration configuration) {
/* 141 */     this.configuration = configuration;
/*     */   }
/*     */   
/*     */   public Application getDefaultApplication() {
/* 145 */     return this.defaultApplication;
/*     */   }
/*     */   
/*     */   public void setDefaultApplication(Application defaultApplication) {
/* 149 */     this.defaultApplication = defaultApplication;
/*     */   }
/*     */   
/*     */   public Principal getPrincipalFromSession(HttpServletRequest request) {
/* 153 */     if (request.getSession().getAttribute(this.loggedOutKey) != null) {
/* 154 */       return null;
/*     */     }
/* 156 */     return (Principal)request.getSession().getAttribute(this.loggedInKey);
/*     */   }
/*     */   
/*     */   public List<String> getFilteredHeaders() {
/* 160 */     return this.filteredHeaders;
/*     */   }
/*     */   
/*     */   public void setFilteredHeaders(List<String> filteredHeaders) {
/* 164 */     this.filteredHeaders = filteredHeaders;
/*     */   }
/*     */   
/*     */   public boolean shouldHandleRequest(HttpServletRequest request) {
/* 168 */     if (this.filteredHeaders == null) {
/* 169 */       return true;
/*     */     }
/*     */     
/* 172 */     String reqUrl = request.getRequestURL().toString();
/* 173 */     if (reqUrl.contains("/ForgotLoginDetails/") || reqUrl.contains("/rest/")) {
/* 174 */       return false;
/*     */     }
/*     */ 
/*     */     
/* 178 */     for (String headerName : this.filteredHeaders) {
/* 179 */       String header = request.getHeader(headerName);
/* 180 */       if (header != null && !header.trim().isEmpty()) {
/* 181 */         return false;
/*     */       }
/*     */     } 
/* 184 */     return true;
/*     */   }
/*     */   
/*     */   public boolean isSPUser(HttpServletRequest request, String username, Collection<String> uGroups) {
/* 188 */     return (!isIpAllowedForOkta(request) || !isUsernameAllowedForOkta(username) || isInSPGroups(uGroups));
/*     */   }
/*     */   
/*     */   public boolean isIpAllowedForOkta(String ip) {
/* 192 */     return getConfiguration().isIpAllowedForOkta(ip);
/*     */   }
/*     */   
/*     */   public boolean isIpAllowedForOkta(HttpServletRequest request) {
/* 196 */     return isIpAllowedForOkta(request.getRemoteAddr());
/*     */   }
/*     */   
/*     */   public boolean isSPUserOrGroupNamesUsed() {
/* 200 */     return (getConfiguration().isSPUsernamesUsed() || getConfiguration().isSPGroupnamesUsed());
/*     */   }
/*     */   
/*     */   private boolean isUsernameAllowedForOkta(String username) {
/* 204 */     return getConfiguration().isUsernameAllowedForOkta(username);
/*     */   }
/*     */   
/*     */   private boolean isInSPGroups(Collection<String> userGroups) {
/* 208 */     return getConfiguration().isInSPGroups(userGroups);
/*     */   }
/*     */   
/*     */   public boolean isInSPUrls(String url) {
/* 212 */     if (StringUtils.isBlank(url)) {
/* 213 */       return false;
/*     */     }
/* 215 */     return getConfiguration().isInSPUrls(url);
/*     */   }
/*     */   
/*     */   public boolean isInOktaProtectedUrls(String url) {
/* 219 */     if (StringUtils.isBlank(url)) {
/* 220 */       return false;
/*     */     }
/* 222 */     return getConfiguration().isInOktaProdectedUrls(url);
/*     */   }
/*     */ }


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\sam\\util\OktaAuthPeer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */