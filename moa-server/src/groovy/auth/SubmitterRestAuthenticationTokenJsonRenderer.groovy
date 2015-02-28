package auth

import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken
import com.odobo.grails.plugin.springsecurity.rest.token.rendering.RestAuthenticationTokenJsonRenderer
import groovy.json.JsonBuilder
import moa.Submitter

/**
 * Created by sajjan on 2/27/15.
 */
class SubmitterRestAuthenticationTokenJsonRenderer implements RestAuthenticationTokenJsonRenderer {
    @Override
    String generateJson(RestAuthenticationToken restAuthenticationToken) {
        def submitter = Submitter.findByEmailAddress(restAuthenticationToken.principal.username);

        def response = new AuthResponse(
                id: restAuthenticationToken.principal.id,
                emailAddress: submitter.emailAddress,
                firstName: submitter.firstName,
                lastName: submitter.lastName,
                institution: submitter.institution,
                access_token: restAuthenticationToken.tokenValue,
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