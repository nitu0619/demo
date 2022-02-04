/*     */ package com.okta.saml;
/*     */ 
/*     */ import com.okta.saml.util.IPRange;
/*     */ import com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl;
/*     */ import java.io.StringReader;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.security.cert.CertificateException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Optional;
/*     */ import java.util.regex.Pattern;
/*     */ import java.util.regex.PatternSyntaxException;
/*     */ import javax.xml.xpath.XPath;
/*     */ import javax.xml.xpath.XPathConstants;
/*     */ import javax.xml.xpath.XPathExpression;
/*     */ import javax.xml.xpath.XPathExpressionException;
/*     */ import javax.xml.xpath.XPathFactory;
/*     */ import org.apache.commons.lang.StringUtils;
/*     */ import org.slf4j.Logger;
/*     */ import org.slf4j.LoggerFactory;
/*     */ import org.w3c.dom.Element;
/*     */ import org.w3c.dom.Node;
/*     */ import org.w3c.dom.NodeList;
/*     */ import org.xml.sax.InputSource;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Configuration
/*     */ {
/*     */   private static XPath xPath;
/*     */   private static XPathExpression configurationRootXPath;
/*     */   private static XPathExpression applicationXPath;
/*     */   private static XPathExpression entityIdXPath;
/*     */   private static XPathExpression defaultAppXPath;
/*     */   private static XPathExpression addressXPath;
/*     */   private static XPathExpression spUsernamesXPath;
/*     */   private static XPathExpression spGroupsXPath;
/*     */   private static XPathExpression suppressErrorsXPath;
/*     */   private static XPathExpression loginUriXPath;
/*     */   private static XPathExpression spUrlsXPath;
/*     */   private static XPathExpression oktaProtectedUrlsXPath;
/*     */   private static XPathExpression fireLoginEventXPath;
/*     */   private static XPathExpression fireUserAuthenticatedEventXPath;
/*     */   private static XPathExpression disableVersionReportingXPath;
/*     */   private static XPathExpression useRememberMeCookieXPath;
/*     */   private boolean suppressErrors;
/*     */   private boolean fireLoginEvent;
/*     */   private boolean fireUserAuthenticatedEvent;
/*     */   private boolean disableVersionReporting;
/*     */   private boolean useRememberMeCookie;
/*     */   private String loginUri;
/*     */   private String spLoginUrl;
/*  64 */   private String defaultEntityID = null;
/*     */   
/*     */   private IPRange oktaUsersIps;
/*     */   
/*     */   private IPRange spUsersIps;
/*     */   
/*     */   private List<String> spUsernames;
/*     */   private List<String> spGroupnames;
/*     */   private List<Pattern> spUrlPatterns;
/*     */   private List<String> spUrls;
/*     */   private List<Pattern> oktaProtectedUrlPatterns;
/*     */   private Map<String, Application> applications;
/*     */   public static final String SAML_RESPONSE_FORM_NAME = "SAMLResponse";
/*     */   public static final String CONFIGURATION_KEY = "okta.config.file";
/*     */   public static final String DEFAULT_ENTITY_ID = "okta.config.default_entity_id";
/*     */   public static final String REDIR_PARAM = "os_destination";
/*     */   public static final String RELAY_STATE_PARAM = "RelayState";
/*  81 */   private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
/*     */   
/*     */   static {
/*     */     try {
/*  85 */       XPathFactory xPathFactory = new XPathFactoryImpl();
/*  86 */       xPath = xPathFactory.newXPath();
/*  87 */       xPath.setNamespaceContext(new MetadataNamespaceContext());
/*     */       
/*  89 */       configurationRootXPath = xPath.compile("configuration");
/*  90 */       applicationXPath = xPath.compile("applications/application");
/*  91 */       addressXPath = xPath.compile("allowedAddresses");
/*  92 */       spUsernamesXPath = xPath.compile("spUsers/username");
/*  93 */       spGroupsXPath = xPath.compile("spGroups/groupname");
/*  94 */       entityIdXPath = xPath.compile("md:EntityDescriptor/@entityID");
/*  95 */       defaultAppXPath = xPath.compile("default");
/*  96 */       suppressErrorsXPath = xPath.compile("suppressErrors");
/*  97 */       loginUriXPath = xPath.compile("loginUri");
/*  98 */       spUrlsXPath = xPath.compile("spUrls/url");
/*  99 */       oktaProtectedUrlsXPath = xPath.compile("oktaProtectedUrls/url");
/* 100 */       fireLoginEventXPath = xPath.compile("fireLoginEvent");
/* 101 */       fireUserAuthenticatedEventXPath = xPath.compile("fireUserAuthenticatedEvent");
/* 102 */       disableVersionReportingXPath = xPath.compile("disableVersionReporting");
/* 103 */       useRememberMeCookieXPath = xPath.compile("useRememberMeCookie");
/*     */     }
/* 105 */     catch (XPathExpressionException e) {
/* 106 */       LOGGER.error("Failed to create XPathFactory instance", e);
/*     */     } 
/*     */   }
/*     */   
/*     */   public Configuration(String configuration) throws XPathExpressionException, CertificateException, UnsupportedEncodingException, SAMLException {
/* 111 */     InputSource source = new InputSource(new StringReader(configuration));
/*     */     
/* 113 */     Node root = (Node)configurationRootXPath.evaluate(source, XPathConstants.NODE);
/* 114 */     NodeList applicationNodes = (NodeList)applicationXPath.evaluate(root, XPathConstants.NODESET);
/*     */     
/* 116 */     this.defaultEntityID = defaultAppXPath.evaluate(root);
/* 117 */     this.applications = new HashMap<>();
/* 118 */     this.spUsernames = new ArrayList<>();
/* 119 */     this.spGroupnames = new ArrayList<>();
/* 120 */     this.spUrlPatterns = new ArrayList<>();
/* 121 */     this.spUrls = new ArrayList<>();
/* 122 */     this.oktaProtectedUrlPatterns = new ArrayList<>();
/*     */     
/* 124 */     for (int i = 0; i < applicationNodes.getLength(); i++) {
/* 125 */       Element applicationNode = (Element)applicationNodes.item(i);
/* 126 */       String entityID = entityIdXPath.evaluate(applicationNode);
/* 127 */       Application application = new Application(applicationNode);
/* 128 */       this.applications.put(entityID, application);
/*     */     } 
/*     */     
/* 131 */     Element allowedAddresses = (Element)addressXPath.evaluate(root, XPathConstants.NODE);
/* 132 */     if (allowedAddresses != null) {
/* 133 */       String oktaFrom = (String)xPath.compile("oktaUsers/ipFrom").evaluate(allowedAddresses, XPathConstants.STRING);
/* 134 */       String oktaTo = (String)xPath.compile("oktaUsers/ipTo").evaluate(allowedAddresses, XPathConstants.STRING);
/*     */       
/* 136 */       String spFrom = (String)xPath.compile("spUsers/ipFrom").evaluate(allowedAddresses, XPathConstants.STRING);
/* 137 */       String spTo = (String)xPath.compile("spUsers/ipTo").evaluate(allowedAddresses, XPathConstants.STRING);
/*     */       
/* 139 */       if (oktaFrom != null) {
/*     */         try {
/* 141 */           this.oktaUsersIps = new IPRange(oktaFrom, oktaTo);
/* 142 */         } catch (NumberFormatException e) {
/* 143 */           LOGGER.error("Invalid IP specified for Okta users addresses: " + e.getMessage());
/*     */         } 
/*     */       }
/*     */       
/* 147 */       if (spFrom != null) {
/*     */         try {
/* 149 */           this.spUsersIps = new IPRange(spFrom, spTo);
/* 150 */         } catch (NumberFormatException e) {
/* 151 */           LOGGER.error("Invalid IP specified for Service Provider users addresses: " + e.getMessage());
/*     */         } 
/*     */       }
/*     */     } 
/*     */     
/* 156 */     String suppress = suppressErrorsXPath.evaluate(root);
/* 157 */     if (suppress != null) {
/* 158 */       suppress = suppress.trim();
/*     */     }
/* 160 */     this.suppressErrors = !StringUtils.isBlank(suppress) ? Boolean.parseBoolean(suppress) : true;
/*     */     
/* 162 */     this.loginUri = loginUriXPath.evaluate(root);
/* 163 */     if (this.loginUri != null) {
/* 164 */       this.loginUri = this.loginUri.trim();
/*     */     }
/*     */     
/* 167 */     this.spUsernames = getOptionsByXPath(root, spUsernamesXPath);
/*     */     
/* 169 */     this.spGroupnames = getOptionsByXPath(root, spGroupsXPath);
/*     */     
/* 171 */     for (String rawPattern : getOptionsByXPath(root, spUrlsXPath)) {
/*     */       try {
/* 173 */         this.spUrls.add(rawPattern);
/* 174 */         this.spUrlPatterns.add(Pattern.compile(rawPattern));
/* 175 */       } catch (PatternSyntaxException e) {
/* 176 */         LOGGER.warn("Invalid url pattern: {}. Skipping", rawPattern, e);
/*     */       } 
/*     */     } 
/*     */     
/* 180 */     for (String rawPattern : getOptionsByXPath(root, oktaProtectedUrlsXPath)) {
/*     */       try {
/* 182 */         this.oktaProtectedUrlPatterns.add(Pattern.compile(rawPattern));
/* 183 */       } catch (PatternSyntaxException e) {
/* 184 */         LOGGER.warn("Invalid Okta protected url pattern: {}. Skipping", rawPattern, e);
/*     */       } 
/*     */     } 
/*     */     
/* 188 */     String loginEvent = Optional.<String>ofNullable(fireLoginEventXPath.evaluate(root)).orElse("false");
/* 189 */     this.fireLoginEvent = Boolean.parseBoolean(loginEvent.trim());
/*     */ 
/*     */     
/* 192 */     String userAuthenticatedEvent = Optional.<String>ofNullable(fireUserAuthenticatedEventXPath.evaluate(root)).orElse("false");
/* 193 */     this.fireUserAuthenticatedEvent = Boolean.parseBoolean(userAuthenticatedEvent.trim());
/*     */ 
/*     */     
/* 196 */     String versionReporting = Optional.<String>ofNullable(disableVersionReportingXPath.evaluate(root)).orElse("false");
/* 197 */     this.disableVersionReporting = Boolean.parseBoolean(versionReporting.trim());
/*     */ 
/*     */     
/* 200 */     String rememberMeCookie = Optional.<String>ofNullable(useRememberMeCookieXPath.evaluate(root)).orElse("false");
/* 201 */     this.useRememberMeCookie = Boolean.parseBoolean(rememberMeCookie.trim());
/*     */   }
/*     */   
/*     */   private List<String> getOptionsByXPath(Node root, XPathExpression xPath) throws XPathExpressionException {
/* 205 */     return getStringsFrom(getElementsByXPath(root, xPath));
/*     */   }
/*     */   
/*     */   private List<String> getStringsFrom(Collection<Element> rawElements) {
/* 209 */     List<String> options = new ArrayList<>();
/* 210 */     for (Element rawElement : rawElements) {
/* 211 */       String option = rawElement.getTextContent();
/* 212 */       if (StringUtils.isNotBlank(option)) {
/* 213 */         options.add(option.trim());
/*     */       }
/*     */     } 
/*     */     
/* 217 */     return options;
/*     */   }
/*     */   
/*     */   private List<Element> getElementsByXPath(Node root, XPathExpression xPath) throws XPathExpressionException {
/* 221 */     NodeList nodes = (NodeList)xPath.evaluate(root, XPathConstants.NODESET);
/* 222 */     List<Element> elements = new ArrayList<>();
/* 223 */     if (nodes != null) {
/* 224 */       for (int i = 0; i < nodes.getLength(); i++) {
/* 225 */         elements.add((Element)nodes.item(i));
/*     */       }
/*     */     }
/* 228 */     return elements;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Map<String, Application> getApplications() {
/* 237 */     return this.applications;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Application getApplication(String entityID) {
/* 245 */     return this.applications.get(entityID);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Application getDefaultApplication() {
/* 252 */     if (StringUtils.isBlank(this.defaultEntityID)) {
/* 253 */       return null;
/*     */     }
/* 255 */     return this.applications.get(this.defaultEntityID);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String getDefaultEntityID() {
/* 262 */     return this.defaultEntityID;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean isIpAllowedForOkta(String ip) {
/*     */     try {
/* 270 */       boolean isRejectedForOkta = (this.oktaUsersIps != null && !this.oktaUsersIps.isAddressInRange(ip));
/* 271 */       boolean isRejectedForSP = (this.spUsersIps != null && !this.spUsersIps.isAddressInRange(ip));
/*     */ 
/*     */       
/* 274 */       if ((isRejectedForOkta && isRejectedForSP) || (!isRejectedForOkta && !isRejectedForSP) || (this.oktaUsersIps == null && isRejectedForSP) || (this.spUsersIps == null && this.oktaUsersIps == null))
/*     */       {
/*     */ 
/*     */         
/* 278 */         return true;
/*     */       }
/*     */       
/* 281 */       if ((this.oktaUsersIps == null && !isRejectedForSP) || (isRejectedForOkta && !isRejectedForSP))
/*     */       {
/* 283 */         return false;
/*     */       }
/* 285 */     } catch (Exception e) {
/* 286 */       LOGGER.error(e.getClass().getSimpleName() + ": " + e.getMessage());
/*     */       
/* 288 */       return true;
/*     */     } 
/*     */     
/* 291 */     return true;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean isUsernameAllowedForOkta(String username) {
/* 298 */     if (StringUtils.isBlank(username)) {
/* 299 */       return true;
/*     */     }
/* 301 */     return !this.spUsernames.contains(username);
/*     */   }
/*     */   
/*     */   public boolean isInSPGroups(Collection<String> userGroups) {
/* 305 */     if (userGroups == null || userGroups.isEmpty() || this.spGroupnames.isEmpty()) {
/* 306 */       return false;
/*     */     }
/*     */     
/* 309 */     for (String atlGroup : this.spGroupnames) {
/* 310 */       for (String userGroup : userGroups) {
/* 311 */         if (userGroup.trim().equalsIgnoreCase(atlGroup.trim())) {
/* 312 */           return true;
/*     */         }
/*     */       } 
/*     */     } 
/* 316 */     return false;
/*     */   }
/*     */   
/*     */   public boolean isSPUsernamesUsed() {
/* 320 */     return !this.spUsernames.isEmpty();
/*     */   }
/*     */   
/*     */   public boolean isSPGroupnamesUsed() {
/* 324 */     return !this.spGroupnames.isEmpty();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean isInSPUrls(String url) {
/* 331 */     for (Pattern pattern : this.spUrlPatterns) {
/* 332 */       if (pattern.matcher(url).find()) {
/* 333 */         return true;
/*     */       }
/*     */     } 
/* 336 */     return false;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean isInOktaProdectedUrls(String url) {
/* 343 */     for (Pattern pattern : this.oktaProtectedUrlPatterns) {
/* 344 */       if (pattern.matcher(url).find()) {
/* 345 */         return true;
/*     */       }
/*     */     } 
/* 348 */     return false;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean isIpAllowedForSP(String ip) {
/* 355 */     return !isIpAllowedForOkta(ip);
/*     */   }
/*     */   
/*     */   public boolean suppressingErrors() {
/* 359 */     return this.suppressErrors;
/*     */   }
/*     */   
/*     */   public String getLoginUri() {
/* 363 */     return this.loginUri;
/*     */   }
/*     */   
/*     */   public boolean isFireLoginEvent() {
/* 367 */     return this.fireLoginEvent;
/*     */   }
/*     */   
/*     */   public boolean isFireUserAuthenticatedEvent() {
/* 371 */     return this.fireUserAuthenticatedEvent;
/*     */   }
/*     */   
/*     */   public void setFireLoginEvent(boolean fireLoginEvent) {
/* 375 */     this.fireLoginEvent = fireLoginEvent;
/*     */   }
/*     */   
/*     */   public void setFireUserAuthenticatedEvent(boolean fireUserAuthenticatedEvent) {
/* 379 */     this.fireUserAuthenticatedEvent = fireUserAuthenticatedEvent;
/*     */   }
/*     */   
/*     */   public boolean isDisableVersionReporting() {
/* 383 */     return this.disableVersionReporting;
/*     */   }
/*     */   
/*     */   public void setDisableVersionReporting(boolean disableVersionReporting) {
/* 387 */     this.disableVersionReporting = disableVersionReporting;
/*     */   }
/*     */   
/*     */   public boolean isUseRememberMeCookie() {
/* 391 */     return this.useRememberMeCookie;
/*     */   }
/*     */   
/*     */   public void setUseRememberMeCookie(boolean useRememberMeCookie) {
/* 395 */     this.useRememberMeCookie = useRememberMeCookie;
/*     */   }
/*     */ }


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\saml\Configuration.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */