package moa

class MolRenderController {

    def index() {}

    def renderCompoundAsMolFile(long id) {

        render Compound.get(id).molFile
    }
}
