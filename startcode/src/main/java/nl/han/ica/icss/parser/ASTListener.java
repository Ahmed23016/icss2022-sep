package nl.han.ica.icss.parser;

import java.util.Stack;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;
import nl.han.ica.icss.ast.selectors.*;

public class ASTListener extends ICSSBaseListener {

	private AST ast;
	private Stack<ASTNode> currentContainer;
	private Stack<Expression> exprStack = new Stack<>();

	public ASTListener() {
		ast = new AST();
		currentContainer = new Stack<>();
	}

	public AST getAST() {
		return ast;
	}


	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		Stylesheet sheet = new Stylesheet();
		currentContainer.push(sheet);
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		ast.root = (Stylesheet) currentContainer.pop();
	}

	@Override
	public void enterStyleRule(ICSSParser.StyleRuleContext ctx) {
		Stylerule rule = new Stylerule();
		currentContainer.push(rule);
	}

	@Override
	public void exitStyleRule(ICSSParser.StyleRuleContext ctx) {
		Stylerule rule = (Stylerule) currentContainer.pop();
		((Stylesheet) currentContainer.peek()).addChild(rule);
	}

	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration decl = new Declaration();
		currentContainer.push(decl);
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration decl = (Declaration) currentContainer.pop();
		ASTNode parent = currentContainer.peek();
		if (parent instanceof Stylerule)
			((Stylerule) parent).addChild(decl);
		else if (parent instanceof IfClause)
			((IfClause) parent).body.add(decl);
	}


	@Override
	public void enterProperty(ICSSParser.PropertyContext ctx) {
		PropertyName prop = new PropertyName(ctx.LOWERIDENT().getText());
		((Declaration) currentContainer.peek()).property = prop;
	}


	@Override
	public void enterIdSelector(ICSSParser.IdSelectorContext ctx) {
		IdSelector selector = new IdSelector(ctx.IDIDENT().getText().substring(1));
		((Stylerule) currentContainer.peek()).selectors.add(selector);
	}

	@Override
	public void enterClassSelector(ICSSParser.ClassSelectorContext ctx) {
		ClassSelector selector = new ClassSelector(ctx.CLASSIDENT().getText().substring(1));
		((Stylerule) currentContainer.peek()).selectors.add(selector);
	}

	@Override
	public void enterTagSelector(ICSSParser.TagSelectorContext ctx) {
		TagSelector selector = new TagSelector(ctx.LOWERIDENT().getText());
		((Stylerule) currentContainer.peek()).selectors.add(selector);
	}


	@Override
	public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment va = new VariableAssignment();
		va.name = new VariableReference(ctx.CAPITALIDENT().getText());
		currentContainer.push(va);
	}

	@Override
	public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment va = (VariableAssignment) currentContainer.pop();
		((Stylesheet) currentContainer.peek()).addChild(va);
	}


	@Override
	public void enterIfClause(ICSSParser.IfClauseContext ctx) {
		IfClause clause = new IfClause();
		currentContainer.push(clause);
	}

	@Override
	public void exitIfClause(ICSSParser.IfClauseContext ctx) {
		IfClause clause = (IfClause) currentContainer.pop();
		ASTNode parent = currentContainer.peek();
		if (parent instanceof Stylerule)
			((Stylerule) parent).body.add(clause);
		else if (parent instanceof IfClause)
			((IfClause) parent).body.add(clause);
	}


	@Override
	public void enterVariableReference(ICSSParser.VariableReferenceContext ctx) {
		exprStack.push(new VariableReference(ctx.CAPITALIDENT().getText()));
	}

	@Override
	public void enterLiteral(ICSSParser.LiteralContext ctx) {
		if (ctx.PIXELSIZE() != null) {
			int val = Integer.parseInt(ctx.PIXELSIZE().getText().replace("px", ""));
			exprStack.push(new PixelLiteral(val));
		} else if (ctx.PERCENTAGE() != null) {
			int val = Integer.parseInt(ctx.PERCENTAGE().getText().replace("%", ""));
			exprStack.push(new PercentageLiteral(val));
		} else if (ctx.NUMBER() != null) {
			int val = Integer.parseInt(ctx.NUMBER().getText());
			exprStack.push(new ScalarLiteral(val));
		} else if (ctx.COLOR() != null) {
			exprStack.push(new ColorLiteral(ctx.COLOR().getText()));
		} else if (ctx.TRUE() != null || ctx.FALSE() != null) {
			boolean val = ctx.TRUE() != null;
			exprStack.push(new BoolLiteral(val));
		}
	}

	@Override
	public void exitExpression(ICSSParser.ExpressionContext ctx) {
		if (exprStack.isEmpty()) return;
		Expression expr = exprStack.pop();
		ASTNode top = currentContainer.peek();
		if (top instanceof Declaration)
			((Declaration) top).expression = expr;
		else if (top instanceof VariableAssignment)
			((VariableAssignment) top).expression = expr;
		else if (top instanceof IfClause)
			((IfClause) top).conditionalExpression = expr;
	}
}
