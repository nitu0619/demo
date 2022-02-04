/*     */ package com.okta.saml;
/*     */ 
/*     */ import com.google.inject.Inject;
/*     */ import com.okta.saml.util.Clock;
/*     */ import com.okta.saml.util.SimpleClock;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.apache.commons.lang3.StringUtils;
/*     */ import org.opensaml.core.xml.XMLObject;
/*     */ import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
/*     */ import org.opensaml.core.xml.util.XMLObjectSupport;
/*     */ import org.opensaml.saml.saml2.core.Assertion;
/*     */ import org.opensaml.saml.saml2.core.Attribute;
/*     */ import org.opensaml.saml.saml2.core.AttributeStatement;
/*     */ import org.opensaml.saml.saml2.core.Audience;
/*     */ import org.opensaml.saml.saml2.core.AudienceRestriction;
/*     */ import org.opensaml.saml.saml2.core.Conditions;
/*     */ import org.opensaml.saml.saml2.core.Response;
/*     */ import org.opensaml.saml.saml2.core.SubjectConfirmation;
/*     */ import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
/*     */ import org.opensaml.xmlsec.signature.Signature;
/*     */ import org.opensaml.xmlsec.signature.support.SignatureException;
/*     */ import org.opensaml.xmlsec.signature.support.SignatureValidator;
/*     */ import org.slf4j.Logger;
/*     */ import org.slf4j.LoggerFactory;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class SAMLResponse
/*     */ {
/*  35 */   private static final Logger LOGGER = LoggerFactory.getLogger(SAMLResponse.class);
/*     */ 
/*     */   
/*     */   private final Clock clock;
/*     */ 
/*     */   
/*     */   private final Response response;
/*     */ 
/*     */   
/*     */   private final Assertion assertion;
/*     */   
/*     */   private final Configuration configuration;
/*     */   
/*     */   private final Application app;
/*     */   
/*     */   private final Map<String, List<String>> attributes;
/*     */ 
/*     */   
/*     */   public SAMLResponse(String responseString, Configuration configuration) throws SAMLException {
/*  54 */     this(responseString, configuration, (Clock)new SimpleClock());
/*     */   }
/*     */   
/*     */   @Inject
/*     */   private SAMLResponse(String responseString, Configuration configuration, Clock clock) throws SAMLException {
/*  59 */     this.clock = clock;
/*  60 */     this.configuration = configuration;
/*  61 */     this.response = validatedResponse(responseString);
/*  62 */     this.app = configuration.getApplication(getIssuer());
/*  63 */     this.assertion = validatedAssertion(this.response);
/*  64 */     this.attributes = loadedAttributes();
/*     */     
/*  66 */     validateSignature();
/*     */   }
/*     */   
/*     */   private HashMap<String, List<String>> loadedAttributes() {
/*  70 */     HashMap<String, List<String>> attributes = new HashMap<>();
/*  71 */     for (AttributeStatement attributeStatement : this.assertion.getAttributeStatements()) {
/*  72 */       for (Attribute attr : attributeStatement.getAttributes()) {
/*  73 */         if (attr.getAttributeValues().size() < 1) {
/*     */           continue;
/*     */         }
/*     */         
/*  77 */         List<String> values = new ArrayList<>();
/*  78 */         for (XMLObject value : attr.getAttributeValues()) {
/*  79 */           values.add(value.getDOM().getTextContent());
/*     */         }
/*  81 */         attributes.put(attr.getName(), values);
/*     */       } 
/*     */     } 
/*  84 */     return attributes;
/*     */   }
/*     */   
/*     */   private void validateSignature() throws SAMLException {
/*  88 */     SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
/*  89 */     Signature signature = this.response.getSignature();
/*     */     
/*  91 */     if (signature == null) {
/*  92 */       throw new SAMLException("No signature present");
/*     */     }
/*     */     
/*     */     try {
/*  96 */       profileValidator.validate(signature);
/*  97 */       SignatureValidator.validate(signature, this.app.getCertificate().getCredential());
/*  98 */     } catch (SignatureException e) {
/*  99 */       LOGGER.debug(e.getMessage(), (Throwable)e);
/* 100 */       throw new SAMLException("Invalid signature");
/*     */     } 
/*     */   }
/*     */   
/*     */   private Response validatedResponse(String assertion) throws SAMLException {
/* 105 */     XMLObject parsedResponse = parseSAML(assertion);
/* 106 */     if (!(parsedResponse instanceof Response)) {
/* 107 */       LOGGER.debug("Parsed response did not result in a Response node: " + parsedResponse.getElementQName());
/* 108 */       throw new SAMLException("Malformatted response");
/*     */     } 
/*     */     
/* 111 */     Response response = (Response)parsedResponse;
/*     */     
/* 113 */     String issuer = response.getIssuer().getValue();
/* 114 */     if (this.configuration.getApplication(issuer) == null) {
/* 115 */       LOGGER.debug("Configuration does not contain issuer: " + issuer);
/* 116 */       throw new SAMLException("Configuration does not have a matching issuer");
/*     */     } 
/*     */     
/* 119 */     String statusCode = response.getStatus().getStatusCode().getValue();
/* 120 */     if (!StringUtils.equals(statusCode, "urn:oasis:names:tc:SAML:2.0:status:Success")) {
/* 121 */       LOGGER.debug("StatusCode was not a success: " + statusCode);
/* 122 */       throw new SAMLException("StatusCode was not a success");
/*     */     } 
/*     */     
/* 125 */     return response;
/*     */   }
/*     */   
/*     */   private Assertion validatedAssertion(Response response) throws SAMLException {
/* 129 */     List<Assertion> assertionList = response.getAssertions();
/* 130 */     if (assertionList.isEmpty())
/* 131 */       throw new SAMLException("No assertions found"); 
/* 132 */     if (assertionList.size() > 1) {
/* 133 */       throw new SAMLException("More than one assertion was found");
/*     */     }
/* 135 */     Assertion assertion = assertionList.get(0);
/*     */ 
/*     */     
/* 138 */     if (!StringUtils.equals(assertion.getIssuer().getValue(), this.app.getIssuer())) {
/* 139 */       throw new SAMLException("Assertion issuer did not match the entity ID");
/*     */     }
/*     */     
/* 142 */     Conditions conditions = assertion.getConditions();
/*     */ 
/*     */     
/* 145 */     Date now = this.clock.dateTimeNow().toDate();
/* 146 */     Date conditionNotBefore = conditions.getNotBefore().toDate();
/* 147 */     Date conditionNotOnOrAfter = conditions.getNotOnOrAfter().toDate();
/* 148 */     if (now.before(conditionNotBefore)) {
/* 149 */       LOGGER.debug("Current time: [" + now + "] NotBefore: [" + conditionNotBefore + "]");
/* 150 */       throw new SAMLException("Conditions are not yet active");
/* 151 */     }  if (now.after(conditionNotOnOrAfter) || now.equals(conditionNotOnOrAfter)) {
/* 152 */       LOGGER.debug("Current time: [" + now + "] NotOnOrAfter: [" + conditionNotOnOrAfter + "]");
/* 153 */       throw new SAMLException("Conditions have expired");
/*     */     } 
/*     */     
/* 156 */     return assertion;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String getUserID() {
/* 163 */     return this.assertion.getSubject().getNameID().getValue();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String getIssuer() {
/* 170 */     return this.response.getIssuer().getValue();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Map<String, List<String>> getAttributes() {
/* 179 */     return this.attributes;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String getDestination() {
/* 186 */     return this.response.getDestination();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String getRecipient() {
/* 193 */     return ((SubjectConfirmation)this.assertion.getSubject().getSubjectConfirmations().get(0)).getSubjectConfirmationData().getRecipient();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String getAudience() {
/* 200 */     return ((Audience)((AudienceRestriction)this.assertion.getConditions().getAudienceRestrictions().get(0)).getAudiences().get(0)).getAudienceURI();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Date getIssueInstant() {
/* 207 */     return this.response.getIssueInstant().toDate();
/*     */   }
/*     */   
/*     */   private XMLObject parseSAML(String response) throws SAMLException {
/*     */     try {
/* 212 */       ByteArrayInputStream bais = new ByteArrayInputStream(response.getBytes("UTF-8"));
/*     */       
/* 214 */       return XMLObjectSupport.unmarshallFromInputStream(XMLObjectProviderRegistrySupport.getParserPool(), bais);
/* 215 */     } catch (Exception e) {
/* 216 */       LOGGER.debug(e.getMessage(), e);
/* 217 */       throw new SAMLException("Problem parsing the response.");
/*     */     } 
/*     */   }
/*     */ }


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\saml\SAMLResponse.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */