package pl.databucket.client;

public enum Operator {
	
	equal("=="),
	grater(">"),
	graterEqual(">="),
	notIn("!in"),
	in("in"),
	less("<"), 
	lessEqual("<="),
	notEqual("<>"),
	like("like"),
	notLike("not like"),
	similar("similar"),
	notSimilar("!similar"),
	matchCaseSensitive("~"),
	matchCaseInsensitive("~*"),
	notMatchCaseSensitive("!~"),
	notMatchCaseInsensitive("!~*");

	private final String symbol;
	
	Operator(String text) {
		this.symbol = text;
	}
	
	public String toString() {
		return symbol;
	}	
}
