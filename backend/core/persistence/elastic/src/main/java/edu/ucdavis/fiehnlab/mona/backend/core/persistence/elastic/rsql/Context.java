package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.rsql;

/**
 * Created by wohlgemuth on 3/15/16.
 */
public class Context {
    private Context parent = null;

    public String buildNestedPath() {
        if (parent != null) {
            if (parent.buildNestedPath() == "") {
                return parentPath;
            } else {
                return parent.buildNestedPath() + "." + parentPath;
            }
        } else {
            return "";
        }
    }

    private String parentPath = null;

    public boolean isOriginatedAsNestedQuery() {
        return originatedAsNestedQuery;
    }

    public void setOriginatedAsNestedQuery(boolean originatedAsNestedQuery) {
        this.originatedAsNestedQuery = originatedAsNestedQuery;
    }

    private boolean originatedAsNestedQuery = false;

    public Context createChieldContent(String parentPath) {
        Context context = new Context();
        context.parent = this;
        context.parentPath = parentPath;
        context.setOriginatedAsNestedQuery(true);

        return context;
    }

    public Context getParent() {
        return parent;
    }

}
