package com.github.janssk1.maven.plugin.graph;

/**
 * Created by IntelliJ IDEA.
 * User: janssk1
 * Date: 12/2/11
 * Time: 1:23 PM
 */
public class DependencyOptions {

    private boolean showProvidedScope = true;

    public boolean isShowProvidedScope() {
        return showProvidedScope;
    }

    public DependencyOptions setShowProvidedScope(boolean showProvidedScope) {
        this.showProvidedScope = showProvidedScope;
        return this;
    }
}
