package moa.server.auth

import grails.plugin.springsecurity.rest.token.reader.HttpHeaderTokenReader
import moa.Submitter
import moa.auth.AuthenticationToken

import javax.servlet.http.HttpServletRequest

/**
 * Created by sajjan on 7/29
 * /15.
 */
class AuthenticationService {
    /**
     * Uses Spring Security Rest to extract the authentication token from an
     * http request
     */
    String getAuthenticationTokenFromRequest(HttpServletRequest request) {
        def tokenReader = new HttpHeaderTokenReader()
        tokenReader.headerName = 'X-Auth-Token'

        return tokenReader.findToken(request).accessToken
    }

    /**
     * Finds the associated email address given an authentication token
     */
    String getSubmitterEmailAddressFromToken(String token) {
        AuthenticationToken auth = AuthenticationToken.findByTokenValue(token)

        return auth ? auth.emailAddress : null;
    }

    /**
     * Finds the associated email address given an http request
     */
    String getSubmitterEmailAddressFromRequest(HttpServletRequest request) {
        String token = getAuthenticationTokenFromRequest(request);

        return token ? getSubmitterEmailAddressFromToken(token) : null;
    }

    /**
     * Finds the submitter given an authentication token
     */
    Submitter getSubmitterFromToken(String token) {
        String emailAddress = getSubmitterEmailAddressFromToken(token);

        return emailAddress ? Submitter.findByEmailAddress(emailAddress) : null;
    }

    /**
     * Finds the submitter given an http request
     */
    String getSubmitterFromRequest(HttpServletRequest request) {
        String token = getAuthenticationTokenFromRequest(request);

        return token ? getSubmitterFromToken(token) : null;
    }
}
