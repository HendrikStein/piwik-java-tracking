/**
 * Piwik - Open source web analytics
 * 
 * @license released under BSD License http://www.opensource.org/licenses/bsd-license.php
 * @link http://piwik.org/docs/tracking-api/
 *
 * @category Piwik
 * @package PiwikTracker
 */
package org.piwik;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;

/**
 * 
 * @author Martin Fochler
 * @author Hendrik Stein
 */
public class ResponseData {

    /**
     * Map to store header information.
     */
    private Map<String, List<String>> headerData;

    /** HTTP response code. */
    private int responseCode;

    /**
     * For debug output.
     */
    private static final Logger LOGGER = Logger.getLogger(ResponseData.class.getName());

    /**
     * Initialize the local header data with the header fields from the connection. Those information are needed to
     * parse the cookie information.
     * 
     * @param connection used to retrieve the header fields
     */
    public ResponseData(final HttpURLConnection connection) {
        headerData = connection.getHeaderFields();
        try {
            responseCode = connection.getResponseCode();
        } catch (IOException e) {
            // Set status to internal server error
            responseCode = 500;
        }
    }

    /**
     * Get the HTTP response code.
     * 
     * @return the response code
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Get cookies from the connections HTTP header.
     * 
     * @return the list of cookies
     */
    public List<Cookie> getCookies() {
        List<Cookie> cookies = new ArrayList<Cookie>();

        for (String key : headerData.keySet()) {
            List<String> headerParts = headerData.get(key);

            StringBuilder cookieInfo = new StringBuilder();
            for (String part : headerParts) {
                cookieInfo.append(part);
            }

            if (key == null && cookieInfo.toString().equals("")) {
                LOGGER.log(Level.FINE, "No more headers, not proceeding");
                return null;
            }

            if (key == null) {
                LOGGER.log(Level.FINE, "The header value contains the server's HTTP version, not proceeding");
            } else if (key.equals("Set-Cookie")) {
                List<HttpCookie> httpCookies = HttpCookie.parse(cookieInfo.toString());
                for (HttpCookie h : httpCookies) {
                    Cookie c = new Cookie(h.getName(), h.getValue());
                    c.setComment(h.getComment());
                    if (h.getDomain() != null) {
                        c.setDomain(h.getDomain());
                    }
                    c.setMaxAge(Long.valueOf(h.getMaxAge()).intValue());
                    c.setPath(h.getPath());
                    c.setSecure(h.getSecure());
                    c.setVersion(h.getVersion());
                    cookies.add(c);
                }
            } else {
                LOGGER.log(Level.FINE,
                        "The provided key ({0}) with value ({1}) were not processed because the key is unknown",
                        new Object[] { key, cookieInfo });
            }
        }
        return cookies;
    }

    @Override
    public String toString() {
        return "ResponseData [headerData=" + headerData + ", responseCode=" + responseCode + "]";
    }

}
