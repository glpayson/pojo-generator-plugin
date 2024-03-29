package com.glp.pojoplugin.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.glp.pojoplugin.parsed.SourceClass;
import com.glp.pojoplugin.parsed.Type;
import com.glp.pojoplugin.parsed.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class ValueClass {
    private List<Variable> variables;
    private SourceClass sourceClass;
    private Access access;

    public ValueClass(List<Variable> variables, SourceClass sourceClass, Access access) {
        this.variables = variables;
        this.sourceClass = sourceClass;
        this.access = access;
    }

    public ValueClass(List<Variable> variables, SourceClass sourceClass) {
        this.variables = variables;
        this.sourceClass = sourceClass;
        this.access = Access.METHOD;
    }

    public static void main(String[] args) {
        List<Variable> vars = new ArrayList<>();
        vars.add(new Variable(new Type("java.lang.String"), new Variable.Name("surname")));
        vars.add(new Variable(new Type("java.lang.Integer"), new Variable.Name("age")));
        vars.add(new Variable(new Type("java.lang.String"), new Variable.Name("name")));
        System.out.println("result:\n" + new ValueClass(vars, new SourceClass("Person"), Access.METHOD).asString());
    }

    private void createGetter(StringJoiner joiner) {
        joiner.add(new ValueGetter(variables).asString());
        joiner.add("");
    }

    public String asString() {
        StringJoiner resultLineJointer = new StringJoiner("\n");
        resultLineJointer.add(new ValueFields(variables, access).asString());
        resultLineJointer.add("");
        if (access == Access.METHOD) {
            createGetter(resultLineJointer);
        }
        resultLineJointer.add(new Constructor(sourceClass, variables).asString());
        resultLineJointer.add("");
        variables.stream().filter(variable -> Type.ALL_SUPPORTED_CLASSES_AND_PRIMITIVES.values().contains(variable.getType()))
                .forEach(variable -> resultLineJointer.add(new ValueSubClass(variable).asString()));
        resultLineJointer.add(createEquals());
        resultLineJointer.add(createHashCode());
        resultLineJointer.add(createToString());
        String complete = resultLineJointer.toString();
        return complete;
    }

    public enum Access {
        METHOD,
        FIELD
    }

    private String createHashCode() {
        StringJoiner variableHashParams = new StringJoiner(", ");
        variables.forEach(variable -> variableHashParams.add(variable.getName().getValue()));
        String s = variableHashParams.toString();
        return "@Override\n" +
                "    public int hashCode() {\n" +
                "        return java.util.Objects.hash(" +
                s +
                ");\n" +
                "    }";
    }

    public String createEquals() {
        String begin = "@Override\n" +
                "    public boolean equals(Object o) {\n" +
                "        if (this == o) return true;\n" +
                "        if (o == null || getClass() != o.getClass()) return false;\n" +
                "        " + sourceClass.getName() + " " + sourceClass.getLowerCasedName() + " = (" + sourceClass.getName() + ") o;\n" +
                "        return ";
        //
        StringJoiner equals = new StringJoiner("\n");
        variables.forEach(variable -> {
            equals.add("java.util.Objects.equals(" + variable.getName().getValue() + ", " + sourceClass.getLowerCasedName() + "." + variable.getName().getValue() + ") &&");
        });
        //
        String variables = equals.toString().substring(0, equals.length() - 3) + ";}";
        String s = begin + variables;
        return s;
    }

    public String createToString() {
        String result = "    @Override\n" +
                "    public String toString() {\n" +
                "        return \"" + sourceClass.getName() + "{\" +\n";
        StringJoiner subValueJoiner = new StringJoiner("\n");
        variables.forEach(variable -> {
                    subValueJoiner.add(
                            "\"" + variable.getName().getValue() + ":\" + " + variable.getName().getValue() + getExtension(variable) + " + \",\"  +");
                }

        );
        String s = result + subValueJoiner.toString() + "\n                \"}\";\n" +
                "    }";
        System.out.println(s);
        return s;
    }

    public String getExtension(Variable variable) {
        if (Type.ALL_SUPPORTED_CLASSES_AND_PRIMITIVES.values().contains(variable.getType())) {
            return ".get()";
        }
        return "";
    }

    public List<PsiElement> getGeneratedPsiElements(Project project) {
        PsiClass classFromText =
                JavaPsiFacade.getInstance(project).getElementFactory().createClassFromText(
                        new ValueClass(variables, sourceClass).asString(), null);
        classFromText.setName(sourceClass.getName());

        List<PsiElement> result = new ArrayList<>();
        Stream.of(classFromText.getAllFields()).forEach(psiField -> result.add(psiField));
        Stream.of(classFromText.getInnerClasses()).forEach(psiClass -> result.add(psiClass));
        Stream.of(classFromText.getMethods()).forEach(psiClass -> result.add(psiClass)); // getMethod contains all constructors
        return result;
    }
}
