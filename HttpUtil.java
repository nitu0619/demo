/*     */ package com.okta.saml.util;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URI;
/*     */ import java.net.URL;
/*     */ import java.util.regex.Pattern;
/*     */ import javax.servlet.http.HttpServletRequest;
/*     */ import javax.servlet.http.HttpServletResponse;
/*     */ import org.apache.commons.lang.StringUtils;
/*     */ import org.slf4j.Logger;
/*     */ import org.slf4j.LoggerFactory;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class HttpUtil
/*     */ {
/*  18 */   private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);
/*  19 */   private static final Pattern STATIC_URL_PATTERN = Pattern.compile("([^\\s]+(\\.(?i)(jpg|jpeg|png|gif|bmp|css|js)))");
/*     */   
/*     */   public static void forceRedirect(HttpServletRequest request, HttpServletResponse response, String redirUrl) {
/*  22 */     if (response == null) {
/*     */       return;
/*     */     }
/*     */     
/*  26 */     String reqUrl = request.getRequestURL().toString();
/*  27 */     String contentType = response.getContentType();
/*     */     
/*  29 */     if ((contentType != null && !contentType.contains("html")) || STATIC_URL_PATTERN.matcher(reqUrl).matches() || reqUrl
/*  30 */       .endsWith(redirUrl) || reqUrl.equals(redirUrl)) {
/*     */       return;
/*     */     }
/*     */     
/*     */     try {
/*  35 */       response.reset();
/*  36 */       response.setStatus(302);
/*  37 */       response.setHeader("Location", redirUrl);
/*  38 */       response.setHeader("Connection", "close");
/*  39 */       response.flushBuffer();
/*  40 */     } catch (IOException e) {
/*  41 */       LOGGER.error("IO Error: " + e.getMessage());
/*     */     } 
/*     */   }
/*     */   
/*     */   public static String getCurrentUrl(HttpServletRequest request) {
/*  46 */     if (request == null) {
/*  47 */       return "";
/*     */     }
/*     */     
/*  50 */     StringBuffer url = request.getRequestURL();
/*  51 */     if (request.getQueryString() != null) {
/*  52 */       url.append('?');
/*  53 */       url.append(request.getQueryString());
/*     */     } 
/*  55 */     return url.toString();
/*     */   }
/*     */   
/*     */   public static String createRedirectUrlWithRelay(HttpServletRequest request, String url, String relayStateParam) {
/*  59 */     String curUrl = getCurrentUrl(request);
/*  60 */     String redirectUrl = url;
/*  61 */     if (!curUrl.contains(relayStateParam) && !curUrl.endsWith(url)) {
/*  62 */       redirectUrl = String.format("%s" + (url.contains("?") ? "&" : "?") + "%s=%s", new Object[] { url, relayStateParam, curUrl });
/*     */     }
/*     */     
/*  65 */     return redirectUrl;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void forceRedirectWithRelayState(HttpServletRequest request, HttpServletResponse response, String redirectUrl, String relayStateParam) throws IOException {
/*  71 */     forceRedirect(request, response, createRedirectUrlWithRelay(request, redirectUrl, relayStateParam));
/*     */   }
/*     */   
/*     */   public static void redirect(HttpServletResponse response, String url) {
/*     */     try {
/*  76 */       response.sendRedirect(url);
/*  77 */     } catch (IOException ioException) {
/*  78 */       LOGGER.info("Redirection failed using sendRedirect() method, trying setting Location header");
/*  79 */       response.setStatus(302);
/*  80 */       response.setHeader("Location", url);
/*     */     } 
/*     */   }
/*     */   
/*     */   public static String getRedirectParam(HttpServletRequest request) {
/*  85 */     String redirectUrl = request.getParameter("os_destination");
/*  86 */     if (StringUtils.isBlank(redirectUrl)) {
/*  87 */       return null;
/*     */     }
/*     */     
/*  90 */     redirectUrl = redirectUrl.trim().replaceAll("[\\\\/]+$", "");
/*     */     try {
/*  92 */       String baseUrl = getBaseUrl(getCurrentUrl(request)).replaceAll("[\\\\/]+$", "");
/*  93 */       if (baseUrl.equalsIgnoreCase(redirectUrl)) {
/*  94 */         return null;
/*     */       }
/*  96 */     } catch (MalformedURLException malformedURLException) {}
/*     */ 
/*     */     
/*  99 */     return redirectUrl;
/*     */   }
/*     */   
/*     */   public static String getBaseUrl(String fullUrl) throws MalformedURLException {
/* 103 */     URL url = new URL(fullUrl);
/*     */     
/* 105 */     String baseUrl = String.format("%s://%s", new Object[] { url.getProtocol(), url.getHost() });
/* 106 */     if (url.getPort() > 0) {
/* 107 */       baseUrl = baseUrl + ":" + url.getPort();
/*     */     }
/*     */     
/* 110 */     return baseUrl;
/*     */   }
/*     */   
/*     */   public static void commitResponse(HttpServletResponse response) {
/* 114 */     if (response != null && !response.isCommitted()) {
/*     */       try {
/* 116 */         if (response.getWriter() != null) {
/* 117 */           response.getWriter().flush();
/*     */         }
/* 119 */       } catch (IOException e) {
/* 120 */         LOGGER.warn("Failed to commit response", e);
/*     */       } 
/*     */     }
/*     */   }
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
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static boolean isInvalidRelayStateUrl(String relayState, HttpServletRequest request) {
/*     */     try {
/* 141 */       if (StringUtils.isBlank(relayState) || isRelativeUrl(relayState)) {
/* 142 */         return false;
/*     */       }
/*     */       
/* 145 */       Origin baseOrigin = new Origin(getCurrentUrl(request));
/* 146 */       Origin relayStateOrigin = new Origin(relayState);
/*     */       
/* 148 */       if (baseOrigin.equals(relayStateOrigin)) {
/* 149 */         return false;
/*     */       }
/*     */       
/* 152 */       LOGGER.warn("Warning! RelayState attribute has different base URL comparing to configured in the system. ServerUrl={}, RelayState={}", baseOrigin
/* 153 */           .getOriginAsString(), relayStateOrigin.getOriginAsString());
/* 154 */     } catch (Exception e) {
/* 155 */       LOGGER.warn("Error validating RelayState attribute: ", e);
/*     */     } 
/*     */     
/* 158 */     return true;
/*     */   }
/*     */   
/*     */   private static boolean isRelativeUrl(String url) {
/*     */     URI uri;
/*     */     try {
/* 164 */       uri = new URI(url);
/* 165 */     } catch (Exception e) {
/* 166 */       throw new IllegalArgumentException("Provided URL is invalid! URL: " + url);
/*     */     } 
/*     */     
/* 169 */     return !uri.isAbsolute();
/*     */   }
/*     */ }


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\sam\\util\HttpUtil.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */