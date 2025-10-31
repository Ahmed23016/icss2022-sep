package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;

import java.util.*;

public class Evaluator implements Transform {
    private final LinkedList<HashMap<String, Literal>> varirableAssigmentsSafe;
    public Evaluator() {
        varirableAssigmentsSafe = new LinkedList<>();
    }
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
        // alle variable assignments verwijderen in de transformer. dan zie je hem niet meer :)
        stylesheet.getChildren().removeAll(toRemove);
    }


    private void applyStylerule(Stylerule rule) {
        varirableAssigmentsSafe.push(new HashMap<>());
        var processedBody = new LinkedList<ASTNode>();

        for (ASTNode node : rule.body) {
            if (node instanceof VariableAssignment) {
                addVarAssignnmentVariable((VariableAssignment) node);
            }
            else if (node instanceof IfClause) {
                processedBody.addAll(apllyIfclause((IfClause) node));
            }
            else if (node instanceof Declaration) {
                Declaration declar = (Declaration) node;
                declar.expression = evalExpression(declar.expression);
                processedBody.add(declar);
            }
        }

        LinkedList<ASTNode> unique = getAstNodes(processedBody);

        rule.body = new ArrayList<>(unique);
        varirableAssigmentsSafe.pop();
    }
    // dit is vor meerdere declaraties
    // bijvoorbeeld in level 3
    /*
p {
	background-color: #ffffff; hier is background color
	width: ParWidth;
	if[AdjustColor] {
	    color: #124532;
	    if[UseLinkColor]{
	        background-color: LinkColor; hier ook
	    } else {
	        background-color: #000000; hier ook
	    }
	}
	height: 20px;
}
eerst kreeg ik in de generation steeds 2 background-color maar door processedBody en de getAstNodes functie pakt hij de laatste


     */
    private static LinkedList<ASTNode> getAstNodes(LinkedList<ASTNode> processedBody) {
        LinkedList<ASTNode> unique = new LinkedList<>();
        HashSet<String> seen = new HashSet<>();

        for (int i = processedBody.size() - 1; i >= 0; i--) {
            ASTNode node = processedBody.get(i);
            if (node instanceof Declaration) {
                String name = ((Declaration) node).property.name;
                if (seen.add(name)) {
                    unique.addFirst(node);
                }
            } else {
                unique.addFirst(node);
            }
        }
        return unique;
    }

    private ArrayList<ASTNode> apllyIfclause(IfClause ifClause) {
        Literal condition = evalExpression(ifClause.conditionalExpression);
        ArrayList<ASTNode> activeBody = null;

        if (condition instanceof BoolLiteral && ((BoolLiteral) condition).value) {
            activeBody = ifClause.body;
        } else if (ifClause.elseClause != null) {
            activeBody = ifClause.elseClause.body;
        }

        if (activeBody == null) {
            return new ArrayList<>();
        }

        ArrayList<ASTNode> result = new ArrayList<>();

        for (ASTNode element : activeBody) {
            if (element instanceof VariableAssignment) {
                addVarAssignnmentVariable((VariableAssignment) element);
            }
            else if (element instanceof Declaration) {
                Declaration declar = (Declaration) element;
                declar.expression = evalExpression(declar.expression);
                result.add(declar);
            }
            else if (element instanceof IfClause) {
                result.addAll(apllyIfclause((IfClause) element));
            }
        }

        return result;
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
            AddOperation adOperation = (AddOperation) expr;
            return addLiterals(evalExpression(adOperation.lhs), evalExpression(adOperation.rhs));
        }

        if (expr instanceof SubtractOperation) {
            SubtractOperation minusOpration = (SubtractOperation) expr;
            return minusLiterals(evalExpression(minusOpration.lhs), evalExpression(minusOpration.rhs));
        }

        if (expr instanceof MultiplyOperation) {
            MultiplyOperation multiplyOperation = (MultiplyOperation) expr;
            return multiplyLiterals(evalExpression(multiplyOperation.lhs), evalExpression(multiplyOperation.rhs));
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
