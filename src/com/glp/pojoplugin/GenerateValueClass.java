package com.glp.pojoplugin;

import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;

public class GenerateValueClass extends EditorAction {

    public GenerateValueClass() {
        super(new GenerateValueClassHandler());
    }

    protected GenerateValueClass(EditorActionHandler defaultHandler) {
        super(defaultHandler);
    }
}
