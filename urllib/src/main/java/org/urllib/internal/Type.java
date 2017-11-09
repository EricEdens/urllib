package org.urllib.internal;

public enum Type {

  /**
   * Contains at least a protocol (scheme) and host.
   * <ul>
   *   <li>https://www.mozilla.org:2020/</li>
   *   <li>ldap://ldap.netscape.com/o=Airius.com</li>
   *   <li>android.resource://com.google.maps/images/pin</li>
   * </ul>
   */
  FULL,


  /**
   * Contains everything from {@link Type#FULL}, except the protocol.
   *
   * <ul>
   *   <li>//en.wikipedia.org/wiki/Java_(programming_language)#Implementations</li>
   * </ul>
   */
  PROTOCOL_RELATIVE,


  /**
   * Contains everything from {@link Type#FULL}, except the protocol.
   * <ul>
   *   <li>/maps</li>
   *   <li>/wiki/Java_(programming_language)#Implementations</li>
   *   <li>/search?q=funny+cats</li>
   * </ul>
   */
  PATH_ABSOLUTE,


  /**
   * Contains everything from {@link Type#FULL}, except the protocol.
   * <ul>
   *   <li>videos/baseball/index.html</li>
   *   <li>../image.jpg</li>
   *   <li>?id=A3234A</li>
   * </ul>
   */
  PATH_RELATIVE,


 /**
   * Contains everything from {@link Type#FULL}, except the protocol.
   * <ul>
   *   <li>#Ingredients</li>
   * </ul>
   */
  FRAGMENT,
}
