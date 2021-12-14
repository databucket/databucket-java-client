package pl.databucket.client;

public enum LogicalOperator {

	and("and"),
	or("or"),
	not("not");

	private final String symbol;

	LogicalOperator(String text) {
		this.symbol = text;
	}
	
	public String toString() {
		return symbol;
	}	
}
