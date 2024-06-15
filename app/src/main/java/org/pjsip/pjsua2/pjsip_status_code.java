/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public final class pjsip_status_code {
  public final static int PJSIP_SC_NULL = 0;
  public final static int PJSIP_SC_TRYING = 100;
  public final static int PJSIP_SC_RINGING = 180;
  public final static int PJSIP_SC_CALL_BEING_FORWARDED = 181;
  public final static int PJSIP_SC_QUEUED = 182;
  public final static int PJSIP_SC_PROGRESS = 183;
  public final static int PJSIP_SC_EARLY_DIALOG_TERMINATED = 199;
  public final static int PJSIP_SC_OK = 200;
  public final static int PJSIP_SC_ACCEPTED = 202;
  public final static int PJSIP_SC_NO_NOTIFICATION = 204;
  public final static int PJSIP_SC_MULTIPLE_CHOICES = 300;
  public final static int PJSIP_SC_MOVED_PERMANENTLY = 301;
  public final static int PJSIP_SC_MOVED_TEMPORARILY = 302;
  public final static int PJSIP_SC_USE_PROXY = 305;
  public final static int PJSIP_SC_ALTERNATIVE_SERVICE = 380;
  public final static int PJSIP_SC_BAD_REQUEST = 400;
  public final static int PJSIP_SC_UNAUTHORIZED = 401;
  public final static int PJSIP_SC_PAYMENT_REQUIRED = 402;
  public final static int PJSIP_SC_FORBIDDEN = 403;
  public final static int PJSIP_SC_NOT_FOUND = 404;
  public final static int PJSIP_SC_METHOD_NOT_ALLOWED = 405;
  public final static int PJSIP_SC_NOT_ACCEPTABLE = 406;
  public final static int PJSIP_SC_PROXY_AUTHENTICATION_REQUIRED = 407;
  public final static int PJSIP_SC_REQUEST_TIMEOUT = 408;
  public final static int PJSIP_SC_CONFLICT = 409;
  public final static int PJSIP_SC_GONE = 410;
  public final static int PJSIP_SC_LENGTH_REQUIRED = 411;
  public final static int PJSIP_SC_CONDITIONAL_REQUEST_FAILED = 412;
  public final static int PJSIP_SC_REQUEST_ENTITY_TOO_LARGE = 413;
  public final static int PJSIP_SC_REQUEST_URI_TOO_LONG = 414;
  public final static int PJSIP_SC_UNSUPPORTED_MEDIA_TYPE = 415;
  public final static int PJSIP_SC_UNSUPPORTED_URI_SCHEME = 416;
  public final static int PJSIP_SC_UNKNOWN_RESOURCE_PRIORITY = 417;
  public final static int PJSIP_SC_BAD_EXTENSION = 420;
  public final static int PJSIP_SC_EXTENSION_REQUIRED = 421;
  public final static int PJSIP_SC_SESSION_TIMER_TOO_SMALL = 422;
  public final static int PJSIP_SC_INTERVAL_TOO_BRIEF = 423;
  public final static int PJSIP_SC_BAD_LOCATION_INFORMATION = 424;
  public final static int PJSIP_SC_USE_IDENTITY_HEADER = 428;
  public final static int PJSIP_SC_PROVIDE_REFERRER_HEADER = 429;
  public final static int PJSIP_SC_FLOW_FAILED = 430;
  public final static int PJSIP_SC_ANONIMITY_DISALLOWED = 433;
  public final static int PJSIP_SC_BAD_IDENTITY_INFO = 436;
  public final static int PJSIP_SC_UNSUPPORTED_CERTIFICATE = 437;
  public final static int PJSIP_SC_INVALID_IDENTITY_HEADER = 438;
  public final static int PJSIP_SC_FIRST_HOP_LACKS_OUTBOUND_SUPPORT = 439;
  public final static int PJSIP_SC_MAX_BREADTH_EXCEEDED = 440;
  public final static int PJSIP_SC_BAD_INFO_PACKAGE = 469;
  public final static int PJSIP_SC_CONSENT_NEEDED = 470;
  public final static int PJSIP_SC_TEMPORARILY_UNAVAILABLE = 480;
  public final static int PJSIP_SC_CALL_TSX_DOES_NOT_EXIST = 481;
  public final static int PJSIP_SC_LOOP_DETECTED = 482;
  public final static int PJSIP_SC_TOO_MANY_HOPS = 483;
  public final static int PJSIP_SC_ADDRESS_INCOMPLETE = 484;
  public final static int PJSIP_AC_AMBIGUOUS = 485;
  public final static int PJSIP_SC_BUSY_HERE = 486;
  public final static int PJSIP_SC_REQUEST_TERMINATED = 487;
  public final static int PJSIP_SC_NOT_ACCEPTABLE_HERE = 488;
  public final static int PJSIP_SC_BAD_EVENT = 489;
  public final static int PJSIP_SC_REQUEST_UPDATED = 490;
  public final static int PJSIP_SC_REQUEST_PENDING = 491;
  public final static int PJSIP_SC_UNDECIPHERABLE = 493;
  public final static int PJSIP_SC_SECURITY_AGREEMENT_NEEDED = 494;
  public final static int PJSIP_SC_INTERNAL_SERVER_ERROR = 500;
  public final static int PJSIP_SC_NOT_IMPLEMENTED = 501;
  public final static int PJSIP_SC_BAD_GATEWAY = 502;
  public final static int PJSIP_SC_SERVICE_UNAVAILABLE = 503;
  public final static int PJSIP_SC_SERVER_TIMEOUT = 504;
  public final static int PJSIP_SC_VERSION_NOT_SUPPORTED = 505;
  public final static int PJSIP_SC_MESSAGE_TOO_LARGE = 513;
  public final static int PJSIP_SC_PUSH_NOTIFICATION_SERVICE_NOT_SUPPORTED = 555;
  public final static int PJSIP_SC_PRECONDITION_FAILURE = 580;
  public final static int PJSIP_SC_BUSY_EVERYWHERE = 600;
  public final static int PJSIP_SC_DECLINE = 603;
  public final static int PJSIP_SC_DOES_NOT_EXIST_ANYWHERE = 604;
  public final static int PJSIP_SC_NOT_ACCEPTABLE_ANYWHERE = 606;
  public final static int PJSIP_SC_UNWANTED = 607;
  public final static int PJSIP_SC_REJECTED = 608;
  public final static int PJSIP_SC_TSX_TIMEOUT = PJSIP_SC_REQUEST_TIMEOUT;
  public final static int PJSIP_SC_TSX_TRANSPORT_ERROR = PJSIP_SC_SERVICE_UNAVAILABLE;
  public final static int PJSIP_SC__force_32bit = 0x7FFFFFFF;
}

