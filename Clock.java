package com.okta.saml.util;

import org.joda.time.DateTime;

public interface Clock {
  String instant();
  
  DateTime dateTimeNow();
}


/* Location:              H:\Documents\from-vdi\SCIRIBuild\SCIRIBuild\SCIRI\SCIRI\Jars\saml-toolkit-1.0.0.jar!\com\okta\sam\\util\Clock.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */