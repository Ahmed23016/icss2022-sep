package nl.han.ica.icss.parser;

import java.util.Stack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;
import nl.han.ica.icss.ast.selectors.*;

public class ASTListener extends ICSSBaseListener {

	private AST ast;
	private Stack<ASTNode> currentContainer = new Stack<>();
	private Stack<Expression> exprStack = new Stack<>();

	public AST getAST() { return ast; }

	public ASTListener() { ast = new AST(); }


	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.push(new Stylesheet());
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		ast.root = (Stylesheet) currentContainer.pop();
	}

	@Override
	public void enterStyleRule(ICSSParser.StyleRuleContext ctx) {
		currentContainer.push(new Stylerule());
	}

	@Override
	public void exitStyleRule(ICSSParser.StyleRuleContext ctx) {
		Stylerule rule = (Stylerule) currentContainer.pop();
		((Stylesheet) currentContainer.peek()).addChild(rule);
	}

	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		currentContainer.push(new Declaration());
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
		((Declaration) currentContainer.peek())
				.property = new PropertyName(ctx.LOWERIDENT().getText());
	}


	@Override
	public void enterIdSelector(ICSSParser.IdSelectorContext ctx) {
		((Stylerule) currentContainer.peek())
				.selectors.add(new IdSelector(ctx.IDIDENT().getText().substring(1)));
	}

	@Override
	public void enterClassSelector(ICSSParser.ClassSelectorContext ctx) {
		((Stylerule) currentContainer.peek())
				.selectors.add(new ClassSelector(ctx.CLASSIDENT().getText().substring(1)));
	}

	@Override
	public void enterTagSelector(ICSSParser.TagSelectorContext ctx) {
		((Stylerule) currentContainer.peek())
				.selectors.add(new TagSelector(ctx.LOWERIDENT().getText()));
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
		currentContainer.push(new IfClause());
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
		if (ctx.PIXELSIZE() != null)
			exprStack.push(new PixelLiteral(Integer.parseInt(ctx.PIXELSIZE().getText().replace("px", ""))));
		else if (ctx.PERCENTAGE() != null)
			exprStack.push(new PercentageLiteral(Integer.parseInt(ctx.PERCENTAGE().getText().replace("%", ""))));
		else if (ctx.NUMBER() != null)
			exprStack.push(new ScalarLiteral(Integer.parseInt(ctx.NUMBER().getText())));
		else if (ctx.COLOR() != null)
			exprStack.push(new ColorLiteral(ctx.COLOR().getText()));
		else if (ctx.TRUE() != null || ctx.FALSE() != null)
			exprStack.push(new BoolLiteral(ctx.TRUE() != null));
	}

	@Override
	public void exitAdditiveExpression(ICSSParser.AdditiveExpressionContext ctx) {
		if (ctx.multiplicativeExpression().size() == 1) return;
		Expression expr = exprStack.pop();
		for (int i = ctx.multiplicativeExpression().size() - 2; i >= 0; i--) {
			Expression left = exprStack.pop();
			String opText = ctx.getChild(2 * i + 1).getText();
			Operation op = opText.equals("+") ? new AddOperation() : new SubtractOperation();
			op.lhs = left;
			op.rhs = expr;
			expr = op;
		}
		exprStack.push(expr);
	}

	@Override
	public void exitMultiplicativeExpression(ICSSParser.MultiplicativeExpressionContext ctx) {
		if (ctx.value().size() == 1) return;
		Expression expr = exprStack.pop();
		for (int i = ctx.value().size() - 2; i >= 0; i--) {
			Expression left = exprStack.pop();
			MultiplyOperation op = new MultiplyOperation();
			op.lhs = left;
			op.rhs = expr;
			expr = op;
		}
		exprStack.push(expr);
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
