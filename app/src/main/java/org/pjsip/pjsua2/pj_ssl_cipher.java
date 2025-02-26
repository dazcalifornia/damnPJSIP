/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public final class pj_ssl_cipher {
  public final static int PJ_TLS_UNKNOWN_CIPHER = -1;
  public final static int PJ_TLS_NULL_WITH_NULL_NULL = 0x00000000;
  public final static int PJ_TLS_RSA_WITH_NULL_MD5 = 0x00000001;
  public final static int PJ_TLS_RSA_WITH_NULL_SHA = 0x00000002;
  public final static int PJ_TLS_RSA_WITH_NULL_SHA256 = 0x0000003B;
  public final static int PJ_TLS_RSA_WITH_RC4_128_MD5 = 0x00000004;
  public final static int PJ_TLS_RSA_WITH_RC4_128_SHA = 0x00000005;
  public final static int PJ_TLS_RSA_WITH_3DES_EDE_CBC_SHA = 0x0000000A;
  public final static int PJ_TLS_RSA_WITH_AES_128_CBC_SHA = 0x0000002F;
  public final static int PJ_TLS_RSA_WITH_AES_256_CBC_SHA = 0x00000035;
  public final static int PJ_TLS_RSA_WITH_AES_128_CBC_SHA256 = 0x0000003C;
  public final static int PJ_TLS_RSA_WITH_AES_256_CBC_SHA256 = 0x0000003D;
  public final static int PJ_TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA = 0x0000000D;
  public final static int PJ_TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA = 0x00000010;
  public final static int PJ_TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA = 0x00000013;
  public final static int PJ_TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA = 0x00000016;
  public final static int PJ_TLS_DH_DSS_WITH_AES_128_CBC_SHA = 0x00000030;
  public final static int PJ_TLS_DH_RSA_WITH_AES_128_CBC_SHA = 0x00000031;
  public final static int PJ_TLS_DHE_DSS_WITH_AES_128_CBC_SHA = 0x00000032;
  public final static int PJ_TLS_DHE_RSA_WITH_AES_128_CBC_SHA = 0x00000033;
  public final static int PJ_TLS_DH_DSS_WITH_AES_256_CBC_SHA = 0x00000036;
  public final static int PJ_TLS_DH_RSA_WITH_AES_256_CBC_SHA = 0x00000037;
  public final static int PJ_TLS_DHE_DSS_WITH_AES_256_CBC_SHA = 0x00000038;
  public final static int PJ_TLS_DHE_RSA_WITH_AES_256_CBC_SHA = 0x00000039;
  public final static int PJ_TLS_DH_DSS_WITH_AES_128_CBC_SHA256 = 0x0000003E;
  public final static int PJ_TLS_DH_RSA_WITH_AES_128_CBC_SHA256 = 0x0000003F;
  public final static int PJ_TLS_DHE_DSS_WITH_AES_128_CBC_SHA256 = 0x00000040;
  public final static int PJ_TLS_DHE_RSA_WITH_AES_128_CBC_SHA256 = 0x00000067;
  public final static int PJ_TLS_DH_DSS_WITH_AES_256_CBC_SHA256 = 0x00000068;
  public final static int PJ_TLS_DH_RSA_WITH_AES_256_CBC_SHA256 = 0x00000069;
  public final static int PJ_TLS_DHE_DSS_WITH_AES_256_CBC_SHA256 = 0x0000006A;
  public final static int PJ_TLS_DHE_RSA_WITH_AES_256_CBC_SHA256 = 0x0000006B;
  public final static int PJ_TLS_DH_anon_WITH_RC4_128_MD5 = 0x00000018;
  public final static int PJ_TLS_DH_anon_WITH_3DES_EDE_CBC_SHA = 0x0000001B;
  public final static int PJ_TLS_DH_anon_WITH_AES_128_CBC_SHA = 0x00000034;
  public final static int PJ_TLS_DH_anon_WITH_AES_256_CBC_SHA = 0x0000003A;
  public final static int PJ_TLS_DH_anon_WITH_AES_128_CBC_SHA256 = 0x0000006C;
  public final static int PJ_TLS_DH_anon_WITH_AES_256_CBC_SHA256 = 0x0000006D;
  public final static int PJ_TLS_RSA_EXPORT_WITH_RC4_40_MD5 = 0x00000003;
  public final static int PJ_TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5 = 0x00000006;
  public final static int PJ_TLS_RSA_WITH_IDEA_CBC_SHA = 0x00000007;
  public final static int PJ_TLS_RSA_EXPORT_WITH_DES40_CBC_SHA = 0x00000008;
  public final static int PJ_TLS_RSA_WITH_DES_CBC_SHA = 0x00000009;
  public final static int PJ_TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA = 0x0000000B;
  public final static int PJ_TLS_DH_DSS_WITH_DES_CBC_SHA = 0x0000000C;
  public final static int PJ_TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA = 0x0000000E;
  public final static int PJ_TLS_DH_RSA_WITH_DES_CBC_SHA = 0x0000000F;
  public final static int PJ_TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA = 0x00000011;
  public final static int PJ_TLS_DHE_DSS_WITH_DES_CBC_SHA = 0x00000012;
  public final static int PJ_TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA = 0x00000014;
  public final static int PJ_TLS_DHE_RSA_WITH_DES_CBC_SHA = 0x00000015;
  public final static int PJ_TLS_DH_anon_EXPORT_WITH_RC4_40_MD5 = 0x00000017;
  public final static int PJ_TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA = 0x00000019;
  public final static int PJ_TLS_DH_anon_WITH_DES_CBC_SHA = 0x0000001A;
  public final static int PJ_SSL_FORTEZZA_KEA_WITH_NULL_SHA = 0x0000001C;
  public final static int PJ_SSL_FORTEZZA_KEA_WITH_FORTEZZA_CBC_SHA = 0x0000001D;
  public final static int PJ_SSL_FORTEZZA_KEA_WITH_RC4_128_SHA = 0x0000001E;
  public final static int PJ_SSL_CK_RC4_128_WITH_MD5 = 0x00010080;
  public final static int PJ_SSL_CK_RC4_128_EXPORT40_WITH_MD5 = 0x00020080;
  public final static int PJ_SSL_CK_RC2_128_CBC_WITH_MD5 = 0x00030080;
  public final static int PJ_SSL_CK_RC2_128_CBC_EXPORT40_WITH_MD5 = 0x00040080;
  public final static int PJ_SSL_CK_IDEA_128_CBC_WITH_MD5 = 0x00050080;
  public final static int PJ_SSL_CK_DES_64_CBC_WITH_MD5 = 0x00060040;
  public final static int PJ_SSL_CK_DES_192_EDE3_CBC_WITH_MD5 = 0x000700C0;
}

