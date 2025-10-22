package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.types.ExpressionType;

import javax.swing.text.Style;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;


public class Checker {

    private LinkedList<HashMap<String, ExpressionType>> variableTypes;
    private static final Map<String, Set<Class<? extends Literal>>> ALLOWED_TYPES_FOR_PROPERTY = Map.of(
            "width", Set.of(ScalarLiteral.class, PixelLiteral.class, PercentageLiteral.class),
            "height", Set.of(ScalarLiteral.class, PixelLiteral.class, PercentageLiteral.class),
            "color", Set.of(ColorLiteral.class),
            "background-color", Set.of(ColorLiteral.class),
            "display", Set.of(BoolLiteral.class)
    );
    public void check(AST ast) {
        checkStylesheet(ast.root);
        // variableTypes = new HANLinkedList<>();

    }

    private void checkStylesheet(Stylesheet stylesheet) {
        for(ASTNode child: stylesheet.getChildren()){
            if(child instanceof Stylerule){
                checkStylerule((Stylerule) child);
            }
        }
    }
    private void checkStylerule(Stylerule stylerule) {
        for(ASTNode child: stylerule.getChildren()){
            if(child instanceof Declaration){
                checkDeclaration((Declaration) child);
            }
        }
    }

    private void checkDeclaration(Declaration declaration) {
        var allowed = ALLOWED_TYPES_FOR_PROPERTY.get(declaration.property.name.toLowerCase());
        if (allowed == null) return;
        var expr = declaration.expression;

        Literal literal = extractLiteral(expr);
        if (literal != null && !allowed.contains(literal.getClass())) {
            declaration.setError(String.format(
                    "Property '%s' verwacht %s, maar kreeg %s.",
                    declaration.property.name,
                    formatAllowed(allowed),
                    literal.getClass().getSimpleName()
            ));
        }
    }
    private Literal extractLiteral(Expression expr) {
        if (expr instanceof Literal) return (Literal) expr;
        if (expr instanceof Operation) {
            Literal left = extractLiteral( ((Operation) expr).lhs);
            if (left != null) return left;
            return extractLiteral(((Operation)expr).rhs);
        }
        return null;
    }

    private String formatAllowed(Set<Class<? extends Literal>> allowed) {
        return allowed.stream()
                .map(c -> c.getSimpleName().replace("Literal", "").toLowerCase())
                .reduce((a, b) -> a + " or " + b)
                .orElse("unknown");
    }
}
