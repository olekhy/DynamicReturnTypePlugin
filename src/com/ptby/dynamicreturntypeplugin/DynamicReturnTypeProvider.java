package com.ptby.dynamicreturntypeplugin;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider2;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.intellij.openapi.diagnostic.Logger.getInstance;

public class DynamicReturnTypeProvider implements PhpTypeProvider2 {

    private final MethodCallTypeCalculator methodCallTypeCalculator;
    private final CallReturnTypeCaster callReturnTypeCaster = new CallReturnTypeCaster();
    private final ConfigAnalyser configAnalyser;
    private com.intellij.openapi.diagnostic.Logger logger = getInstance( "DynamicReturnTypePlugin" );


    public DynamicReturnTypeProvider() {
        MethodCallValidator methodCallValidator = new MethodCallValidator();
        methodCallTypeCalculator = new MethodCallTypeCalculator( methodCallValidator, new CallReturnTypeCaster() );
        configAnalyser = new ConfigAnalyser( methodCallValidator );
    }


    @Override
    public char getKey() {
        return 0;
    }


    public String getType( PsiElement psiElement ) {
        try {
            try {
                try {
                    String dynamicReturnType = createDynamicReturnType( psiElement );
                    if ( dynamicReturnType == null ) {
                        return null;
                    }

                    return dynamicReturnType;
                } catch ( MalformedJsonException e ) {
                    logger.warn( "MalformedJsonException", e );
                } catch ( JsonSyntaxException e ) {
                    logger.warn( "JsonSyntaxException", e );
                } catch ( IOException e ) {
                    logger.error( "IOException", e );
                }

            } catch ( IndexNotReadyException e ) {
                logger.error( "IndexNotReadyException", e );
            }
        } catch ( Exception e ) {
            logger.error( "Exception", e );

            e.printStackTrace();
        }


        return null;
    }


    @Override
    public Collection<? extends PhpNamedElement> getBySignature( String type, Project project ) {
        return PhpIndex.getInstance( project ).getAnyByFQN( type );
    }


    private DynamicReturnTypeConfig getDynamicReturnTypeConfig( PsiElement psiElement ) throws IOException {
        DynamicReturnTypeConfig dynamicReturnTypeConfig = configAnalyser.analyseConfig( psiElement.getProject() );
        if ( dynamicReturnTypeConfig == null ) {
            return new DynamicReturnTypeConfig();
        }

        return dynamicReturnTypeConfig;
    }


    private String createDynamicReturnType( PsiElement psiElement ) throws IOException {
        if ( PlatformPatterns.psiElement( PhpElementTypes.METHOD_REFERENCE ).accepts( psiElement ) ) {
            MethodReferenceImpl classMethod = ( MethodReferenceImpl ) psiElement;

            List<ClassMethodConfig> classMethodConfigs
                    = getDynamicReturnTypeConfig( psiElement ).getClassMethodConfigs();

            return getTypeFromMethodCall( classMethodConfigs, classMethod );
        } else if ( PlatformPatterns.psiElement( PhpElementTypes.FUNCTION_CALL ).accepts( psiElement ) ) {
            FunctionReferenceImpl functionReference = ( FunctionReferenceImpl ) psiElement;

            List<FunctionCallConfig> functionCallConfigs
                    = getDynamicReturnTypeConfig( psiElement ).getFunctionCallConfigs();

            return getTypeFromFunctionCall( functionCallConfigs, functionReference
            );
        }

        return null;
    }


    private String getTypeFromMethodCall( List<ClassMethodConfig> classMethodConfigList, MethodReferenceImpl methodReference ) {
        for ( ClassMethodConfig classMethodConfig : classMethodConfigList ) {
            String phpType = methodCallTypeCalculator.calculateFromMethodCall( classMethodConfig, methodReference );
            if ( phpType != null ) {
                return phpType;
            }
        }

        return null;
    }


    private String getTypeFromFunctionCall( List<FunctionCallConfig> functionCallConfigs,
                                            FunctionReferenceImpl functionReference ) {
        String fullFunctionName = functionReference.getNamespaceName() + functionReference.getName();
        for ( FunctionCallConfig functionCallConfig : functionCallConfigs ) {
            if ( functionCallIsValid( functionCallConfig, fullFunctionName, functionReference ) ) {
                return callReturnTypeCaster
                        .calculateTypeFromFunctionParameter( functionReference, functionCallConfig.getParameterIndex()
                        );
            }
        }

        return null;
    }


    private boolean functionCallIsValid( FunctionCallConfig functionCallConfig,
                                         String fullFunctionName,
                                         FunctionReferenceImpl functionReference ) {
        return functionCallConfig.getFunctionName().equals( fullFunctionName ) ||
                validateAgainstPossibleGlobalFunction( functionReference, functionCallConfig );
    }


    private boolean validateAgainstPossibleGlobalFunction( FunctionReferenceImpl functionReference, FunctionCallConfig functionCallConfig ) {
        String text = functionReference.getText();
        return !text.contains( "\\" ) &&
                functionCallConfig.getFunctionName().lastIndexOf( "\\" ) != -1 &&
                ( "\\" + functionReference.getName() ).equals( functionCallConfig.getFunctionName() );
    }
}
