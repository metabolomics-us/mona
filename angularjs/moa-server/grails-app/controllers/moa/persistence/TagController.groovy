package moa.persistence

import grails.rest.RestfulController
import moa.Tag

class TagController extends RestfulController {
	static responseFormats = ['json']

	public TagController() {
		super(Tag)
	}

	/**
	 * otherwise grails won't populate the json fields
	 * @return
	 */
	protected Map getParametersToBind() {
		log.debug("===>json: " + request.getJSON())
		if (request.JSON) {
			params.putAll(request.JSON)
		}

		params
	}

	@Override
	protected Tag createResource(Map params) {

		Tag t = super.createResource(params)

		t = Tag.findOrCreateWhere(text: t.text)

		return t;
	}
}
