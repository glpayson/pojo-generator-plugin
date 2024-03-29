package com.glp.pojoplugin.generator;

import com.glp.pojoplugin.parsed.SourceClass;
import com.glp.pojoplugin.parsed.Type;
import com.glp.pojoplugin.parsed.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by mauer on 06.04.17.
 */
public class Constructor {
    private SourceClass sourceClass;
    private List<Variable> variables;

    public Constructor(SourceClass sourceClass, List<Variable> variables) {
        this.sourceClass = sourceClass;
        this.variables = variables;
    }

    public static void main(String[] args) {
        List<Variable> vars = new ArrayList<>();
        vars.add(new Variable(new Type("java.lang.String"),
                new Variable.Name("name")));
        vars.add(new Variable(new Type("java.lang.String"),
                new Variable.Name("surname")));
        vars.add(new Variable(new Type("java.lang.String"),
                new Variable.Name("address")));
        vars.add(new Variable(new Type("java.lang.Integer"),
                new Variable.Name("age")));
        System.out.println(new Constructor(new SourceClass("User"), vars).asString());
    }

    public String asString() {
        StringJoiner resultLineJointer = new StringJoiner("\n");
        resultLineJointer.add(getConstructorLine());
        variables.stream().map(variable -> variable.getName().getValue())
                .forEach(name -> resultLineJointer.add("\tthis." + name + " = " + name + ";"));
        resultLineJointer.add("}");
        return resultLineJointer.toString();
    }

    private String getConstructorLine() {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder
                .append("public ")
                .append(sourceClass.getName())
                .append("(");
        StringJoiner constructorParams = new StringJoiner(", ");
        variables.stream().forEach(variable -> {
            String name = variable.getName().getValue();
            constructorParams.add(determinClassName(variable) + " " + name);
        });
        resultBuilder.append(constructorParams.toString());
        resultBuilder.append(
                ") {");
        return resultBuilder.toString();
    }

    private String determinClassName(Variable variable) {
        if (Type.ALL_SUPPORTED_CLASSES_AND_PRIMITIVES.values().contains(variable.getType())) {
            return variable.getUppercasedName();
        }
        return variable.getType().getClassName().getValue();
    }
}
