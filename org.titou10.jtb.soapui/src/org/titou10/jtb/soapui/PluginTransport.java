package org.titou10.jtb.soapui;

import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.submit.RequestTransport;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.plugins.auto.PluginRequestTransport;

@PluginRequestTransport(protocol = "jms2")
public class PluginTransport implements RequestTransport {

   @Override
   public void abortRequest(SubmitContext arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void addRequestFilter(RequestFilter arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void insertRequestFilter(RequestFilter arg0, RequestFilter arg1) {
      // TODO Auto-generated method stub

   }

   @Override
   public void removeRequestFilter(RequestFilter arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public Response sendRequest(SubmitContext arg0, Request arg1) throws Exception {
      // TODO Auto-generated method stub
      return null;
   }

}
