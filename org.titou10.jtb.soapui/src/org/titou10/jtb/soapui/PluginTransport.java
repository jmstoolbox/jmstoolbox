package org.titou10.jtb.soapui;

import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.submit.RequestTransport;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.util.BaseResponse;
import com.eviware.soapui.plugins.auto.PluginRequestTransport;

@PluginRequestTransport(protocol = "jms2")
public class PluginTransport implements RequestTransport {

   public PluginTransport() {
      System.out.println("a");
   }

   @Override
   public void abortRequest(SubmitContext arg0) {
      // TODO Auto-generated method stub
      System.out.println("b");

   }

   @Override
   public void addRequestFilter(RequestFilter arg0) {
      // TODO Auto-generated method stub
      System.out.println("c");

   }

   @Override
   public void insertRequestFilter(RequestFilter arg0, RequestFilter arg1) {
      // TODO Auto-generated method stub
      System.out.println("d");

   }

   @Override
   public void removeRequestFilter(RequestFilter arg0) {
      // TODO Auto-generated method stub
      System.out.println("e");
   }

   @Override
   public Response sendRequest(SubmitContext arg0, Request request) throws Exception {
      // TODO Auto-generated method stub
      System.out.println("f arg0=" + arg0 + " request=" + request);
      System.out.println("pn=" + arg0.getPropertyNames());
      System.out.println("mi=" + arg0.getModelItem());
      System.out.println("mi=" + request.getEndpoint());
      System.out.println("mi=" + request.getOperation());
      System.out.println("rq=" + request.getRequestContent());
      System.out.println("rr1=" + request.getRequestParts());
      System.out.println("rr2=" + request.getResponseParts());

      Response r = new BaseResponse(request, "abcdef", "application/json");

      return r;
   }

}
