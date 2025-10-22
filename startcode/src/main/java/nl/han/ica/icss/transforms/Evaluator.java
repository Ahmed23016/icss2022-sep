package nl.han.ica.icss.transforms;

import jdk.jshell.EvalException;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.HashMap;
import java.util.LinkedList;

public class Evaluator implements Transform {

    private LinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        //variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        applyStylesheet(ast.root);
        //variableValues = new HANLinkedList<>();

    }
    private void applyStylesheet(Stylesheet stylesheet) {
        applyStylerule((Stylerule) stylesheet.getChildren().get(0));
    }
    private void applyStylerule(Stylerule stylerule) {
        for(ASTNode child : stylerule.getChildren()) {
            if(child instanceof Declaration) {
                applyDeclaration((Declaration) child);
            }
        }
    }
    private void applyDeclaration(Declaration declaration) {
        declaration.expression = evalExpression((Expression) declaration.expression);
    }
    private PixelLiteral evalExpression(Expression expression) {
        if(expression instanceof PixelLiteral) {
            return (PixelLiteral) expression;
        }
        else{
            return evalAddOperation((AddOperation)expression);
        }

    }
    private PixelLiteral evalAddOperation(AddOperation operation) {
        PixelLiteral left = evalExpression(operation.lhs);
        PixelLiteral right =evalExpression(operation.rhs);
        return new PixelLiteral(left.value + right.value);
    }
    
}
