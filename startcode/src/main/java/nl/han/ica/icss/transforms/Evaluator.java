package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;

import java.util.HashMap;
import java.util.LinkedList;

public class Evaluator implements Transform {
    private final LinkedList<HashMap<String, Literal>> varirableAssigmentsSafe = new LinkedList<>();
    @Override
    public void apply(AST ast) {
            varirableAssigmentsSafe.push(new HashMap<>());
            applyStylesheet((Stylesheet) ast.root);
    }

    private void applyStylesheet(Stylesheet stylesheet) {
        for (ASTNode node : stylesheet.getChildren()) {
            if (node instanceof VariableAssignment varAssign) {
                addVarAssignnmentVariable(varAssign);
            }
            else if (node instanceof Stylerule rule) {
                applyStylerule(rule);
            }
        }
    }

    private void addVarAssignnmentVariable(VariableAssignment varAssign) {
        Literal value = evalExpression(varAssign.expression);
        varirableAssigmentsSafe.peek().put(varAssign.name.name, value);
    }
    private void applyStylerule(Stylerule rule) {
        for (ASTNode node : rule.getChildren()) {
            if (node instanceof Declaration)
                applyDeclaration((Declaration) node);
        }
    }

    private void applyDeclaration(Declaration declaration) {
        Expression expr = declaration.expression;
        declaration.expression = evalExpression(expr);
    }


    private Literal evalExpression(Expression expr) {
        if(expr instanceof VariableReference){
            VariableReference variablerefernc = (VariableReference) expr;
            for (HashMap<String, Literal> variableAssigment : varirableAssigmentsSafe) {
                if(variableAssigment.containsKey(variablerefernc.name)){
                    return variableAssigment.get(variablerefernc.name);
                }
            }
        }
        if (expr instanceof Literal) {
            return (Literal) expr;
        }
        if (expr instanceof AddOperation) {
            Literal left = evalExpression(((AddOperation)expr).lhs);
            Literal right = evalExpression(((AddOperation)expr).rhs);
            return addLiterals(left, right);
        }
        if (expr instanceof SubtractOperation) {
            Literal left = evalExpression(((SubtractOperation)expr).lhs);
            Literal right = evalExpression(((SubtractOperation)expr).rhs);
            return subtractLiterals(left, right);
        }
        if (expr instanceof MultiplyOperation) {
            Literal left = evalExpression(((MultiplyOperation)expr).lhs);
            Literal right = evalExpression(((MultiplyOperation)expr).rhs);
            return multiplyLiterals(left, right);
        }
        return null;
    }


    private Literal addLiterals(Literal left, Literal right) {
        if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
            PixelLiteral l = (PixelLiteral) left;
            PixelLiteral r = (PixelLiteral) right;
            return new PixelLiteral(l.value + r.value);
        }
        if (left instanceof PercentageLiteral && right instanceof PercentageLiteral) {
            PercentageLiteral l = (PercentageLiteral) left;
            PercentageLiteral r = (PercentageLiteral) right;
            return new PercentageLiteral(l.value + r.value);
        }
        if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
            ScalarLiteral l = (ScalarLiteral) left;
            ScalarLiteral r = (ScalarLiteral) right;
            return new ScalarLiteral(l.value + r.value);
        }
        return left;
    }

    private Literal subtractLiterals(Literal left, Literal right) {
        if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
            PixelLiteral l = (PixelLiteral) left;
            PixelLiteral r = (PixelLiteral) right;
            return new PixelLiteral(l.value - r.value);
        }
        if (left instanceof PercentageLiteral && right instanceof PercentageLiteral) {
            PercentageLiteral l = (PercentageLiteral) left;
            PercentageLiteral r = (PercentageLiteral) right;
            return new PercentageLiteral(l.value - r.value);
        }
        if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
            ScalarLiteral l = (ScalarLiteral) left;
            ScalarLiteral r = (ScalarLiteral) right;
            return new ScalarLiteral(l.value - r.value);
        }
        return left;
    }

    private Literal multiplyLiterals(Literal left, Literal right) {
        if (left instanceof PixelLiteral && right instanceof ScalarLiteral) {
            PixelLiteral l = (PixelLiteral) left;
            ScalarLiteral r = (ScalarLiteral) right;
            return new PixelLiteral(l.value * r.value);
        }
        if (left instanceof PercentageLiteral && right instanceof ScalarLiteral) {
            PercentageLiteral l = (PercentageLiteral) left;
            ScalarLiteral r = (ScalarLiteral) right;
            return new PercentageLiteral(l.value * r.value);
        }
        if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
            ScalarLiteral l = (ScalarLiteral) left;
            ScalarLiteral r = (ScalarLiteral) right;
            return new ScalarLiteral(l.value * r.value);
        }
        return left;
    }

}
