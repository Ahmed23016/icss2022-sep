package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.types.ExpressionType;

import javax.swing.text.Style;
import java.util.HashMap;
import java.util.LinkedList;


public class Checker {

    private LinkedList<HashMap<String, ExpressionType>> variableTypes;

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
        System.out.println(stylerule);
    }

    private void checkDeclaration(Declaration declaration) {
        if(declaration.property.name.equals("width")){
            if(declaration.expression instanceof ColorLiteral){
                declaration.setError("GRRRRR. Je hebt fout gedaan. Property width is geen kleur\uD83D\uDE20");
            }
        }
    }
}
