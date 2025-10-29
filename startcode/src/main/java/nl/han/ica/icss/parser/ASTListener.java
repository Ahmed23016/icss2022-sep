package nl.han.ica.icss.parser;

import java.util.Stack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;
import nl.han.ica.icss.ast.selectors.*;

public class ASTListener extends ICSSBaseListener {

	private AST ast;
	private Stack<ASTNode> currentContainer = new Stack<>();

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
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		currentContainer.push(new Stylerule());
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
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
		else if (parent instanceof ElseClause)
			((ElseClause) parent).body.add(decl);
	}

	@Override
	public void enterProperty(ICSSParser.PropertyContext ctx) {
		((Declaration) currentContainer.peek())
				.property = new PropertyName(ctx.LOWERIDENT().getText());
	}

	@Override
	public void enterIdSelector(ICSSParser.IdSelectorContext ctx) {
		((Stylerule) currentContainer.peek())
				.selectors.add(new IdSelector(ctx.IDIDENT().getText()));
	}

	@Override
	public void enterClassSelector(ICSSParser.ClassSelectorContext ctx) {
		((Stylerule) currentContainer.peek())
				.selectors.add(new ClassSelector(ctx.CLASSIDENT().getText()));
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
		ASTNode parent = currentContainer.peek();
		if (parent instanceof Stylesheet)
			((Stylesheet) parent).addChild(va);
		else if (parent instanceof Stylerule)
			((Stylerule) parent).body.add(va);
		else if (parent instanceof IfClause)
			((IfClause) parent).body.add(va);
		else if (parent instanceof ElseClause)
			((ElseClause) parent).body.add(va);
	}

	@Override
	public void enterIfClause(ICSSParser.IfClauseContext ctx) {
		currentContainer.push(new IfClause());
	}

	@Override
	public void exitIfClause(ICSSParser.IfClauseContext ctx) {
		IfClause ifClause = (IfClause) currentContainer.pop();

		if (ctx.ELSE() != null && !ctx.statement().isEmpty()) {
			ElseClause elseClause = new ElseClause();

			int totalStatements = ctx.statement().size();
			int half = totalStatements / 2;
			for (int i = half; i < totalStatements; i++) {
				ASTNode node = ifClause.body.remove(half);
				elseClause.body.add(node);
			}
			ifClause.elseClause = elseClause;
		}

		ASTNode parent = currentContainer.peek();
		if (parent instanceof Stylerule)
			((Stylerule) parent).body.add(ifClause);
		else if (parent instanceof IfClause)
			((IfClause) parent).body.add(ifClause);
		else if (parent instanceof ElseClause)
			((ElseClause) parent).body.add(ifClause);
	}

	@Override
	public void enterVariableReference(ICSSParser.VariableReferenceContext ctx) {
		currentContainer.push(new VariableReference(ctx.CAPITALIDENT().getText()));
	}

	@Override
	public void enterLiteral(ICSSParser.LiteralContext ctx) {
		Expression lit = null;
		if (ctx.PIXELSIZE() != null)
			lit = new PixelLiteral(Integer.parseInt(ctx.PIXELSIZE().getText().replace("px", "")));
		else if (ctx.PERCENTAGE() != null)
			lit = new PercentageLiteral(Integer.parseInt(ctx.PERCENTAGE().getText().replace("%", "")));
		else if (ctx.NUMBER() != null)
			lit = new ScalarLiteral(Integer.parseInt(ctx.NUMBER().getText()));
		else if (ctx.COLOR() != null)
			lit = new ColorLiteral(ctx.COLOR().getText());
		else if (ctx.TRUE() != null || ctx.FALSE() != null)
			lit = new BoolLiteral(ctx.TRUE() != null);
		currentContainer.push(lit);
	}
	@Override
	public void exitMultiplyExpression(ICSSParser.MultiplyExpressionContext ctx) {
		if (ctx.MUL() == null) return;

		Expression right = (Expression) currentContainer.pop();
		Expression left = (Expression) currentContainer.pop();

		MultiplyOperation op = new MultiplyOperation();
		op.lhs = left;
		op.rhs = right;

		currentContainer.push(op);
	}

	@Override
	public void exitPlusExpression(ICSSParser.PlusExpressionContext ctx) {
		if (ctx.PLUS() == null && ctx.MIN() == null) return;

		Expression right = (Expression) currentContainer.pop();
		Expression left = (Expression) currentContainer.pop();

		Operation op = ctx.PLUS() != null ? new AddOperation() : new SubtractOperation();
		op.lhs = left;
		op.rhs = right;

		currentContainer.push(op);
	}


	@Override
	public void exitExpression(ICSSParser.ExpressionContext ctx) {
		if (currentContainer.peek() instanceof Expression) {
			Expression expr = (Expression) currentContainer.pop();
			ASTNode top = currentContainer.peek();
			if (top instanceof Declaration)
				((Declaration) top).expression = expr;
			else if (top instanceof VariableAssignment)
				((VariableAssignment) top).expression = expr;
			else if (top instanceof IfClause)
				((IfClause) top).conditionalExpression = expr;
		}
	}
}
