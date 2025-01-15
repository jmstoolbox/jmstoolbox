/*
 * Copyright (C) 2025 Denis Forveille titou10.titou10@gmail.com
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

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for encrypting/decrypting sensitive data
 * 
 * @author Denis Forveille
 *
 */
public final class EncryptUtils {
   private static final Logger log            = LoggerFactory.getLogger(EncryptUtils.class);

   private static final String ENC_PREFIX     = "{##}";
   private static final int    ENC_PREFIX_LEN = ENC_PREFIX.length();

   // must be 16,24 or 32 bytes in length
   private static final byte[] keyValue       = "JMSToolBox!?&$*-".getBytes();
   private static final String ALGORITHM      = "AES";

   private static Key          key            = new SecretKeySpec(keyValue, ALGORITHM);

   public static String encrypt(String data) {
      if (data == null) {
         return null;
      }

      try {
         Cipher c = Cipher.getInstance(ALGORITHM);
         c.init(Cipher.ENCRYPT_MODE, key);
         byte[] encVal = c.doFinal(data.getBytes());
         String base64 = Base64.getEncoder().encodeToString(encVal);
         return EncryptUtils.ENC_PREFIX + base64;
      } catch (Exception e) {
         log.warn("Exception occurred when encrypting :" + e.getMessage());
         return null;
      }
   }

   public static String decrypt(String encryptedData) {
      if (encryptedData == null) {
         return null;
      }

      if (encryptedData.startsWith(ENC_PREFIX)) {
         String d = encryptedData.substring(ENC_PREFIX_LEN);
         try {
            Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedValue = Base64.getDecoder().decode(d);
            byte[] decValue = c.doFinal(decodedValue);
            return new String(decValue);
         } catch (Exception e) {
            log.warn("Exception occurred when decrypting :" + e.getMessage());
            return null;
         }
      } else {
         return encryptedData;
      }
   }

   public static void main(String[] arg) throws Exception {
      String x = EncryptUtils.encrypt("abcdefg");
      System.out.println("Enc=" + x);
      System.out.println("Dec=" + EncryptUtils.decrypt(x));
   }
}
