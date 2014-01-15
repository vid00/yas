/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vida.phd.yas;

import java.text.MessageFormat;

/**
 *
 * @author ehsun7b
 */
public class Utils {
  public static String shortenHash(String hash, int headSize) {
    if (hash.length() <= 10) {
      return hash;
    } else {
      return MessageFormat.format("{0}...{1}", hash.substring(0, headSize), 
              hash.substring(hash.length() - headSize, hash.length()));
    }    
  }
  
  public static String shortenHash(String hash) {
    return shortenHash(hash, 6);
  }  
}
