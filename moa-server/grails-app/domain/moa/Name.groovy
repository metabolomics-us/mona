package moa

class Name {
	static mapping = {
		name sqlType: "text"

	}

    static constraints = {
    }

    static belongsTo = [compound:Compound]

    String name
}
