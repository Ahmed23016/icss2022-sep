package nl.han.ica.icss.parser;

import java.util.Stack;


import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	
	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private Stack currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new Stack();
	}
    public AST getAST() {
        return ast;
    }

	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		Stylesheet stylesheet = new Stylesheet();
		currentContainer.push(stylesheet);
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		Stylesheet stylesheet = (Stylesheet) currentContainer.pop();
		ast.root = stylesheet;
	}

	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		Stylerule rule = new Stylerule();
		currentContainer.push(rule);
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		Stylerule rule = (Stylerule) currentContainer.pop();
		((Stylesheet) currentContainer.peek()).addChild(rule);
	}

	@Override
	public void enterVariable_assignment(ICSSParser.Variable_assignmentContext ctx){

		VariableAssignment va = new VariableAssignment();
		currentContainer.push(va);
		System.out.println(ctx.getText());
	}

	@Override
	public void exitVariable_assignment(ICSSParser.Variable_assignmentContext ctx){
		VariableAssignment va = (VariableAssignment) currentContainer.pop();
		((Stylesheet) currentContainer.peek()).addChild(va);
	}
	@Override
	public void enterId_selector(ICSSParser.Id_selectorContext ctx) {
		String name = ctx.ID_IDENT().getText().substring(1); // zonder '#'
		IdSelector selector = new IdSelector(name);
		((Stylerule) currentContainer.peek()).selectors.add(selector);
	}

	@Override
	public void enterClass_selector(ICSSParser.Class_selectorContext ctx) {
		String name = ctx.CLASS_IDENT().getText().substring(1);
		ClassSelector selector = new ClassSelector(name);
		((Stylerule) currentContainer.peek()).selectors.add(selector);
	}

	@Override
	public void enterTag_selector(ICSSParser.Tag_selectorContext ctx) {
		String name = ctx.LOWER_IDENT().getText();
		TagSelector selector = new TagSelector(name);
		((Stylerule) currentContainer.peek()).selectors.add(selector);
	}


	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration declaration = new Declaration();
		currentContainer.push(declaration);
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration declaration = (Declaration) currentContainer.pop();
		((Stylerule) currentContainer.peek()).addChild(declaration);
	}

	@Override
	public void enterProperty(ICSSParser.PropertyContext ctx) {
		String name = ctx.LOWER_IDENT().getText();
		PropertyName property = new PropertyName(name);
		((Declaration) currentContainer.peek()).property = property;
	}

	@Override
	public void enterPixel_literal(ICSSParser.Pixel_literalContext ctx) {
		String text = ctx.PIXELSIZE().getText();
		int valuewuithoutpixel = Integer.parseInt(text.replace("px", ""));
		PixelLiteral literal = new PixelLiteral(valuewuithoutpixel);
		((Declaration) currentContainer.peek()).expression = literal;
	}

	@Override
	public void enterColor_literal(ICSSParser.Color_literalContext ctx) {
		String colorValue = ctx.COLOR().getText();
		ColorLiteral literal = new ColorLiteral(colorValue);
		((Declaration) currentContainer.peek()).expression = literal;
	}




}