package nl.han.ica.icss.checker;

public class SemanticError {
	public String description;
	public Boolean withError=true;
	public SemanticError(String description) {
		this.description = description;
	}
	public SemanticError() {
		withError=false;
	}
	public String toString() {
		return withError? "ERROR: " + description:"";
	}
}
