package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;

import java.util.ArrayList;

public class Generator {

	public String generate(AST ast) {
        return generateStylesheet(ast.root);


	}
	private String generateStylesheet(Stylesheet stylesheet) {
		for (ASTNode child : stylesheet.getChildren()) {
			if(child instanceof Stylerule) {
				return generateStylerule((Stylerule) child);
			}
		}
		return "";
	}

	private String generateStylerule(Stylerule stylerule) {
		return stylerule.selectors.get(0).toString()
				+" {"
				+ generateDeclaration(stylerule.body) +
				"\n}";


	}

	private String generateDeclaration(ArrayList<ASTNode> body) {
		StringBuilder sb = new StringBuilder();
        for (ASTNode astNode : body) {
			Declaration child = (Declaration) astNode;
            sb.append("\n    ").append(child.property.name).append(" : ").append(generateExpression(child.expression)).append(";");
        }
		return sb.toString();

	}
	private String generateExpression(Expression expression) {
		if (expression instanceof PixelLiteral)
			return ((PixelLiteral) expression).value + "px";
		if (expression instanceof PercentageLiteral)
			return ((PercentageLiteral) expression).value + "%";
		if (expression instanceof ColorLiteral)
			return ((ColorLiteral) expression).value;
		if (expression instanceof ScalarLiteral)
			return String.valueOf(((ScalarLiteral) expression).value);
		if (expression instanceof BoolLiteral)
			return ((BoolLiteral) expression).value ? "TRUE" : "FALSE";
		return "";
	}


}
