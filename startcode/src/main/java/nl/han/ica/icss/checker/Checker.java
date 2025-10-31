package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.*;

public class Checker {


    private LinkedList<HashMap<String, ExpressionType>> safedepositOfVariableAssignments;

    //Ik heb een Map gemaakt zodat een width bijvoorbeeld geen kleurcode mag enzo. anders moest ik allemaal if clauses maken tijdens het checken.
    //dit is een veel beter idee vond ik :)
    private static final Map<String, Set<Class<? extends Literal>>> ALLOWED_TYPES_FOR_PROPERTY = Map.of(
            "width", Set.of(ScalarLiteral.class, PixelLiteral.class, PercentageLiteral.class),
            "height", Set.of(ScalarLiteral.class, PixelLiteral.class, PercentageLiteral.class),
            "color", Set.of(ColorLiteral.class),
            "background-color", Set.of(ColorLiteral.class),
            "display", Set.of(BoolLiteral.class)
    );

    public Checker() {
        safedepositOfVariableAssignments = new LinkedList<>();
    }

    public void check(AST ast) {
        safedepositOfVariableAssignments.push(new HashMap<>());
        checkStylesheet(ast.root);
    }

    // spreekt voor zich. ik check gewoon of het een stylerule is of variableassignment
    private void checkStylesheet(Stylesheet stylesheet) {
        for (ASTNode child : stylesheet.getChildren()) {
            if (child instanceof VariableAssignment)
                checkVariableAssignment((VariableAssignment) child);
            else if (child instanceof Stylerule)
                checkStylerule((Stylerule) child);
        }
    }

    private void checkStylerule(Stylerule rule) {
        safedepositOfVariableAssignments.push(new HashMap<>());
        for (ASTNode child : rule.getChildren()) {
            if(child instanceof VariableAssignment){
                checkVariableAssignment((VariableAssignment) child);
            }
            if (child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            }
            else if (child instanceof IfClause) {
                checkIfClause((IfClause) child);
            }
        }
        safedepositOfVariableAssignments.pop();
    }


    private ExpressionType checkType(Expression expr) {
        if (expr instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        }
        if (expr instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        }
        if (expr instanceof ScalarLiteral){
            return ExpressionType.SCALAR;
        }
        if (expr instanceof ColorLiteral){
            return ExpressionType.COLOR;
        }
        if (expr instanceof BoolLiteral){
            return ExpressionType.BOOL;
        }
        if (expr instanceof VariableReference){
            return checkVariableReference((VariableReference) expr);
        }
        if (expr instanceof Operation) {
            return checkOperation((Operation) expr);
        }
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkVariableReference(VariableReference ref) {
        for (HashMap<String, ExpressionType> scope : safedepositOfVariableAssignments) {
            if (scope.containsKey(ref.name))
                return scope.get(ref.name);
        }
        ref.setError("Variabele '" + ref.name + "' is niet gedefinierd.");
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkOperation(Operation op) {
        ExpressionType left = checkType(op.lhs);
        ExpressionType right = checkType(op.rhs);

        if (left == ExpressionType.COLOR || right == ExpressionType.COLOR) {
            op.setError("Kleuren mogen niet gebruikt worden in operaties.");
            return ExpressionType.UNDEFINED;
        }

        if (op instanceof AddOperation || op instanceof SubtractOperation) {
            if (left != right)
                op.setError("waarde in " + op.getClass().getSimpleName() + " moeten gelijk zijn.");
            return left;
        }

        if (op instanceof MultiplyOperation) {
            if (!(left == ExpressionType.SCALAR || right == ExpressionType.SCALAR)) {
                op.setError("Bij vermenigvuldigen moet 1 waarde een scalar zijn.");
                return ExpressionType.UNDEFINED;
            }

            if (left == ExpressionType.SCALAR){ return right;}
            if (right == ExpressionType.SCALAR){ return left;}
        }

        return ExpressionType.UNDEFINED;
    }


    private void checkDeclaration(Declaration declaration) {
        if (declaration.expression == null) return;

        if (declaration.expression instanceof VariableReference ref) {
            boolean defined = safedepositOfVariableAssignments.stream().anyMatch(scope -> scope.containsKey(ref.name));
            if (!defined) {
                ref.setError("Variabele '" + ref.name + "' is niet gedefinieerd.");
                return;
            }
        }

        checkType(declaration.expression);


        var allowed = ALLOWED_TYPES_FOR_PROPERTY.get(declaration.property.name.toLowerCase());
        if (allowed == null) return;

        Literal literal = extractLiteral(declaration.expression);
        if (literal == null) return;

        if (!allowed.contains(literal.getClass())) {
            declaration.setError(String.format(
                    "Property '%s' verwacht %s, maar kreeg %s.", // nu kan ik error geven voor meerdere type errors zoals : color:10px; en width:#ffffff;
                    declaration.property.name,
                    formatAllowed(allowed),
                    literal.getClass().getSimpleName()
            ));
        }
    }

    private void checkVariableAssignment(VariableAssignment varassin) {
        ExpressionType expresiontype = checkType(varassin.expression);
        safedepositOfVariableAssignments.peek().put(varassin.name.name, expresiontype);
    }

    private void checkIfClause(IfClause ifClause) {
        safedepositOfVariableAssignments.push(new HashMap<>());
        ExpressionType condType = checkType(ifClause.conditionalExpression);
        if (condType != ExpressionType.BOOL) {
            ifClause.setError("If clause conditie moet boolean zijn.");
        }

        for (ASTNode child : ifClause.body) {
            if (child instanceof VariableAssignment){
                checkVariableAssignment((VariableAssignment) child);
            }
            else if (child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            }
            else if(child instanceof IfClause){
                checkIfClause((IfClause) child);
            }
        }

        if (ifClause.elseClause != null) {
            safedepositOfVariableAssignments.push(new HashMap<>());
            for (ASTNode child : ifClause.elseClause.body) {
                if (child instanceof VariableAssignment){
                    checkVariableAssignment((VariableAssignment) child);
                }
                if (child instanceof Declaration) {
                    checkDeclaration((Declaration) child);
                }
                else if(child instanceof IfClause){
                    checkIfClause((IfClause) child);
                }

            }
            safedepositOfVariableAssignments.pop();
        }
        safedepositOfVariableAssignments.pop();
    }


    private Literal extractLiteral(Expression expr) {
        if (expr instanceof Literal) return (Literal) expr;
        if (expr instanceof Operation) {
            Literal left = extractLiteral(((Operation)expr).lhs);
            if (left != null) return left;
            return extractLiteral(((Operation)expr).rhs);
        }
        return null;
    }

    private String formatAllowed(Set<Class<? extends Literal>> allowed) {
        return String.join(" of ",
                allowed.stream()
                        .map(c -> c.getSimpleName().replace("Literal", "").toLowerCase())
                        .toList());
    }
}
