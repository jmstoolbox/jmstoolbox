/*
 * Copyright (C) 2015 Denis Forveille titou10.titou10@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.titou10.jtb.util;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 * Trust manager that accepts all certificates. Big Security Hole !!!
 * 
 * @author Denis Forveille
 *
 */
public class TrustEverythingSSLTrustManager extends X509ExtendedTrustManager {
   @Override
   public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
      // NOP
   }

   @Override
   public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
      // NOP
   }

   @Override
   public X509Certificate[] getAcceptedIssuers() {
      return null;
   }

   @Override
   public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
      // NOP
   }

   @Override
   public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
      // NOP
   }

   @Override
   public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
      // NOP
   }

   @Override
   public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
      // NOP
   }

}
