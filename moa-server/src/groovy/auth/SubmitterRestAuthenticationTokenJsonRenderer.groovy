package auth

import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.rendering.AccessTokenJsonRenderer
import groovy.json.JsonBuilder
import moa.Submitter

/**
 * Created by sajjan on 2/27/15.
 */
class SubmitterRestAuthenticationTokenJsonRenderer implements AccessTokenJsonRenderer {
    @Override
    String generateJson(AccessToken restAuthenticationToken) {
        def submitter = Submitter.findByEmailAddress(restAuthenticationToken.principal.username);

        def response = new AuthResponse(
                id: restAuthenticationToken.principal.id,
                emailAddress: submitter.emailAddress,
                firstName: submitter.firstName,
                lastName: submitter.lastName,
                institution: submitter.institution,
                access_token: restAuthenticationToken.accessToken,
                roles: restAuthenticationToken.authorities
        )

        return new JsonBuilder(response).toPrettyString()
    }
}

class AuthResponse {
    String id
    String emailAddress
    String firstName
    String lastName
    String institution
    String access_token
    Collection roles
}