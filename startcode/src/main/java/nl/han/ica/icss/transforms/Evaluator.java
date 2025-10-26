package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

public class Evaluator implements Transform {
    private final LinkedList<HashMap<String, Literal>> varirableAssigmentsSafe = new LinkedList<>();
    @Override
    public void apply(AST ast) {
            varirableAssigmentsSafe.push(new HashMap<>());
            applyStylesheet((Stylesheet) ast.root);
    }

    private void applyStylesheet(Stylesheet stylesheet) {
        var toRemove = new LinkedList<ASTNode>();
        for (ASTNode node : stylesheet.getChildren()) {
            if (node instanceof VariableAssignment) {
                addVarAssignnmentVariable((VariableAssignment) node);
                toRemove.add(node);
            } else if (node instanceof Stylerule) {
                applyStylerule((Stylerule) node);
            }
        }
        stylesheet.getChildren().removeAll(toRemove);
    }


    private void applyStylerule(Stylerule rule) {
        varirableAssigmentsSafe.push(new HashMap<>());

        var newBody = new LinkedList<ASTNode>();

        for (ASTNode node : rule.body) {
            System.out.println(node.toString());
            if (node instanceof VariableAssignment) {
                addVarAssignnmentVariable((VariableAssignment) node);
            }

            else if (node instanceof IfClause) {
                IfClause ifClause = (IfClause) node;
                Literal condition = evalExpression(ifClause.conditionalExpression);

                if (condition instanceof BoolLiteral && ((BoolLiteral) condition).value) {
                    for (ASTNode ifClauseNode : ifClause.body) {
                        if (ifClauseNode instanceof Declaration) {
                            Declaration declaration = (Declaration) ifClauseNode;
                            declaration.expression = evalExpression(declaration.expression);
                            newBody.add(declaration);
                        } else if (ifClauseNode instanceof VariableAssignment) {
                            addVarAssignnmentVariable((VariableAssignment) ifClauseNode);
                        }
                    }
                } else if (ifClause.elseClause != null) {
                    for (ASTNode elseClauseNode : ifClause.elseClause.body) {
                        if (elseClauseNode instanceof Declaration) {
                            Declaration declaration = (Declaration) elseClauseNode;
                            declaration.expression = evalExpression(declaration.expression);
                            newBody.add(declaration);
                        } else if (elseClauseNode instanceof VariableAssignment) {
                            addVarAssignnmentVariable((VariableAssignment) elseClauseNode);
                        }
                    }
                }
            }

            else if (node instanceof Declaration) {
                Declaration declaration = (Declaration) node;
                declaration.expression = evalExpression(declaration.expression);
                newBody.add(declaration);
            }
        }

        rule.body = new ArrayList<>(newBody);

        varirableAssigmentsSafe.pop();
    }

    private void addVarAssignnmentVariable(VariableAssignment varAssign) {
        Literal value = evalExpression(varAssign.expression);
        varirableAssigmentsSafe.peek().put(varAssign.name.name, value);
    }

    private Literal evalExpression(Expression expr) {
        if (expr instanceof VariableReference) {
            VariableReference ref = (VariableReference) expr;
            for (HashMap<String, Literal> scope : varirableAssigmentsSafe) {
                if (scope.containsKey(ref.name)) {
                    return scope.get(ref.name);
                }
            }
            return new ScalarLiteral(0);
        }

        if (expr instanceof Literal)
            return (Literal) expr;

        if (expr instanceof AddOperation) {
            AddOperation op = (AddOperation) expr;
            return addLiterals(evalExpression(op.lhs), evalExpression(op.rhs));
        }

        if (expr instanceof SubtractOperation) {
            SubtractOperation op = (SubtractOperation) expr;
            return minusLiterals(evalExpression(op.lhs), evalExpression(op.rhs));
        }

        if (expr instanceof MultiplyOperation) {
            MultiplyOperation op = (MultiplyOperation) expr;
            return multiplyLiterals(evalExpression(op.lhs), evalExpression(op.rhs));
        }

        return new ScalarLiteral(0);
    }

    private Literal addLiterals(Literal leftLiteral, Literal rightLiteral) {
        if (leftLiteral instanceof PixelLiteral && rightLiteral instanceof PixelLiteral) {
            return new PixelLiteral(((PixelLiteral) leftLiteral).value + ((PixelLiteral) rightLiteral).value);
        }
        if (leftLiteral instanceof ScalarLiteral && rightLiteral instanceof ScalarLiteral) {
            return new ScalarLiteral(((ScalarLiteral) leftLiteral).value + ((ScalarLiteral) rightLiteral).value);
        }
        if (leftLiteral instanceof PercentageLiteral && rightLiteral instanceof PercentageLiteral){
            return new PercentageLiteral(((PercentageLiteral) leftLiteral).value + ((PercentageLiteral) rightLiteral).value);
        }


        return leftLiteral;
    }

    private Literal minusLiterals(Literal left, Literal right) {
        if (left instanceof PixelLiteral && right instanceof PixelLiteral)
            return new PixelLiteral(((PixelLiteral) left).value - ((PixelLiteral) right).value);
        if (left instanceof PercentageLiteral && right instanceof PercentageLiteral)
            return new PercentageLiteral(((PercentageLiteral) left).value - ((PercentageLiteral) right).value);
        if (left instanceof ScalarLiteral && right instanceof ScalarLiteral)
            return new ScalarLiteral(((ScalarLiteral) left).value - ((ScalarLiteral) right).value);
        return left;
    }

    private Literal multiplyLiterals(Literal left, Literal right) {
        // MOET NOG VERANDEREN EN MISS WAT DYNAMISCHER MKAKEN. NIET VERGETEN!!!!l
        if (left instanceof PixelLiteral && right instanceof ScalarLiteral)
            return new PixelLiteral(((PixelLiteral) left).value * ((ScalarLiteral) right).value);
        if (left instanceof PercentageLiteral && right instanceof ScalarLiteral)
            return new PercentageLiteral(((PercentageLiteral) left).value * ((ScalarLiteral) right).value);
        if (left instanceof ScalarLiteral && right instanceof ScalarLiteral)
            return new ScalarLiteral(((ScalarLiteral) left).value * ((ScalarLiteral) right).value);
        if (left instanceof ScalarLiteral && right instanceof PixelLiteral)
            return new PixelLiteral(((ScalarLiteral) left).value * ((PixelLiteral) right).value);
        if (left instanceof ScalarLiteral && right instanceof PercentageLiteral)
            return new PercentageLiteral(((ScalarLiteral) left).value * ((PercentageLiteral) right).value);
        return left;
    }
}
