package mn.mpf.system.constants

import mn.application.mobilechargelocker.App

object ConstVariables {
    /**
     * Attribute name of a [org.apache.http.conn.scheme.Scheme]
     * object that represents the actual protocol scheme registry.
     */
    val APP_STORAGE = "app-storage"

    val ARDUINO_DEVICE = "arduino-device"
    val TTY_DEVICE = "tty-device"
    val PI_DEVICE = "pi-device"
    val SWITCH_MODE = "switch"
    /**
     * Attribute name of a [org.apache.http.conn.scheme.Scheme]
     * object that represents the actual protocol scheme registry.
     */
    val SCHEME_REGISTRY = "http.scheme-registry"
    /**
     * Attribute name of a [org.apache.http.cookie.CookieSpecRegistry]
     * object that represents the actual cookie specification registry.
     */
    val COOKIESPEC_REGISTRY = "http.cookiespec-registry"

    /**
     * Attribute name of a [org.apache.http.cookie.CookieSpec]
     * object that represents the actual cookie specification.
     */
    val COOKIE_SPEC = "http.cookie-spec"

    /**
     * Attribute name of a [org.apache.http.cookie.CookieOrigin]
     * object that represents the actual details of the origin server.
     */
    val COOKIE_ORIGIN = "http.cookie-origin"

    /**
     * Attribute name of a [org.apache.http.client.CookieStore]
     * object that represents the actual cookie store.
     */
    val COOKIE_STORE = "http.cookie-store"

    /**
     * Attribute name of a [org.apache.http.auth.AuthSchemeRegistry]
     * object that represents the actual authentication scheme registry.
     */
    val AUTHSCHEME_REGISTRY = "http.authscheme-registry"

    /**
     * Attribute name of a [org.apache.http.client.CredentialsProvider]
     * object that represents the actual crednetials provider.
     */
    val CREDS_PROVIDER = "http.auth.credentials-provider"

    /**
     * Attribute name of a [org.apache.http.auth.AuthState]
     * object that represents the actual target authentication state.
     */
    val TARGET_AUTH_STATE = "http.auth.target-scope"

    /**
     * Attribute name of a [org.apache.http.auth.AuthState]
     * object that represents the actual proxy authentication state.
     */
    val PROXY_AUTH_STATE = "http.auth.proxy-scope"

    /**
     * RESERVED. DO NOT USE!!!
     */
    val AUTH_SCHEME_PREF = "http.auth.scheme-pref"

    /**
     * Attribute name of a [java.lang.Object] object that represents
     * the actual user identity such as user [java.security.Principal].
     */
    val USER_TOKEN = "http.user-token"


    val SSLContext_TLS = "TLS"
    val SSLContext_SSL = "SSL"
    val KeyStore_BKS = "BKS"
    val KeyStore_JKS = "JKS"

    val STRING_LINE_END = "\r\n"
    val STRING_TWO_HYPHENS = "--"
    val STRING_BOUNDARY = "*****"

    val CONTENT_TYPE = "Content-Type"
    val JSON_CONTENT_TYPE = "application/json"
    val X_CSRF_Token_HEADER = "X-CSRFToken"
    val CLIENT_ID_HEADER = "X-Client-ID"
    val COOKIES_HEADER = "Set-Cookie"
    val COOKIE_SEPARATER = "; "

    val STR_EMPTY = ""
    val INT_ZERO = 0

    val UPLOAD_FILE_FIELD_NAME = "files"
}