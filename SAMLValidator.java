package com.okta.saml;

import java.io.IOException;

public interface SAMLValidator {
  Configuration getConfiguration(String paramString) throws SAMLException;
  
  Configuration getConfigurationFrom(String paramString) throws SAMLException, IOException;
  
  SAMLRequest getSAMLRequest(Application paramApplication);
  
  SAMLResponse getSAMLResponse(String paramString, Configuration paramConfiguration) throws SAMLException;
}


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\saml\SAMLValidator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */