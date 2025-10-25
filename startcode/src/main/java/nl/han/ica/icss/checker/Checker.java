package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.*;

public class Checker {


    private LinkedList<HashMap<String, ExpressionType>> safedepositOfVariableAssignments = new LinkedList<>();

    private static final Map<String, Set<Class<? extends Literal>>> ALLOWED_TYPES_FOR_PROPERTY = Map.of(
            "width", Set.of(ScalarLiteral.class, PixelLiteral.class, PercentageLiteral.class),
            "height", Set.of(ScalarLiteral.class, PixelLiteral.class, PercentageLiteral.class),
            "color", Set.of(ColorLiteral.class),
            "background-color", Set.of(ColorLiteral.class),
            "display", Set.of(BoolLiteral.class)
    );


    public void check(AST ast) {
        safedepositOfVariableAssignments.push(new HashMap<>());
        checkStylesheet(ast.root);
    }


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
            if (child instanceof Declaration)
                checkDeclaration((Declaration) child);
            else if (child instanceof IfClause)
                checkIfClause((IfClause) child);
        }
        safedepositOfVariableAssignments.pop();
    }


    private ExpressionType getType(Expression expr) {
        if (expr instanceof PixelLiteral) return ExpressionType.PIXEL;
        if (expr instanceof PercentageLiteral) return ExpressionType.PERCENTAGE;
        if (expr instanceof ScalarLiteral) return ExpressionType.SCALAR;
        if (expr instanceof ColorLiteral) return ExpressionType.COLOR;
        if (expr instanceof BoolLiteral) return ExpressionType.BOOL;
        if (expr instanceof VariableReference) return checkVariableReference((VariableReference) expr);
        if (expr instanceof Operation) return checkOperation((Operation) expr);
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkVariableReference(VariableReference ref) {
        for (HashMap<String, ExpressionType> scope : safedepositOfVariableAssignments) {
            if (scope.containsKey(ref.name))
                return scope.get(ref.name);
        }
        ref.setError("Variabele '" + ref.name + "' is niet gedefinieerd.");
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkOperation(Operation op) {
        ExpressionType left = getType(op.lhs);
        ExpressionType right = getType(op.rhs);

        if (left == ExpressionType.COLOR || right == ExpressionType.COLOR) {
            op.setError("Kleuren mogen niet gebruikt worden in operaties.");
            return ExpressionType.UNDEFINED;
        }

        if (op instanceof AddOperation || op instanceof SubtractOperation) {
            if (left != right)
                op.setError("Operand types in " + op.getClass().getSimpleName() + " moeten gelijk zijn.");
            return left;
        }

        if (op instanceof MultiplyOperation) {
            if (!(left == ExpressionType.SCALAR || right == ExpressionType.SCALAR)) {
                op.setError("Bij vermenigvuldigen moet één operand een scalar zijn.");
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
                declaration.setError("Variabele '" + ref.name + "' is niet gedefinieerd.");
                ref.setError();
                return;
            }
        }

        getType(declaration.expression);
        if (declaration.expression.hasError()) {
            declaration.setError();
            return;
        }

        var allowed = ALLOWED_TYPES_FOR_PROPERTY.get(declaration.property.name.toLowerCase());
        if (allowed == null) return;

        Literal literal = extractLiteral(declaration.expression);
        if (literal == null) return;

        if (!allowed.contains(literal.getClass())) {
            declaration.setError(String.format(
                    "Property '%s' verwacht %s, maar kreeg %s.",
                    declaration.property.name,
                    formatAllowed(allowed),
                    literal.getClass().getSimpleName()
            ));
        }
    }

    private void checkVariableAssignment(VariableAssignment assignment) {
        ExpressionType type = getType(assignment.expression);
        safedepositOfVariableAssignments.peek().put(assignment.name.name, type);
    }

    private void checkIfClause(IfClause ifClause) {
        safedepositOfVariableAssignments.push(new HashMap<>());
        ExpressionType condType = getType(ifClause.conditionalExpression);
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
