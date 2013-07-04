package com.ptby.dynamicreturntypeplugin.config;

import com.jetbrains.php.lang.psi.elements.MethodReference;

public class ClassMethodConfig {

    private final String fqnClassName;
    private final String methodName;
    private final int parameterIndex;


    public ClassMethodConfig( String fqnClassName, String methodName, int parameterIndex ) {
        this.fqnClassName = fqnClassName;
        this.methodName = methodName.toLowerCase();
        this.parameterIndex = parameterIndex;
    }


    public boolean methodCallMatches( String actualFqnClassName, String actualMethodName ) {
        return fqnClassName.equals( actualFqnClassName ) && equalsMethodName( actualMethodName );
    }


    public boolean equalsMethodName( String currentMethodName ) {
        String lowerCaseCurrentMethodName = currentMethodName.toLowerCase();
        return lowerCaseCurrentMethodName.equals( methodName );
    }


    public boolean equalsMethodReferenceName( MethodReference methodReference ) {
        String methodName = methodReference.getName();
        return equalsMethodName( methodName );
    }


    public String getFqnClassName() {
        return fqnClassName;
    }


    public int getParameterIndex() {
        return parameterIndex;
    }


    @Override
    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        ClassMethodConfig that = ( ClassMethodConfig ) o;

        if ( parameterIndex != that.parameterIndex ) {
            return false;
        }
        if ( !fqnClassName.equals( that.fqnClassName ) ) {
            return false;
        }
        if ( !methodName.equals( that.methodName ) ) {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode() {
        int result = fqnClassName.hashCode();
        result = 31 * result + methodName.hashCode();
        result = 31 * result + parameterIndex;
        return result;
    }


    @Override
    public String toString() {
        return "ClassMethodConfig{" +
                "\nfqnClassName='" + fqnClassName + '\'' +
                "\n, methodName='" + methodName + '\'' +
                "\n, parameterIndex=" + parameterIndex +
                '}';
    }
}
