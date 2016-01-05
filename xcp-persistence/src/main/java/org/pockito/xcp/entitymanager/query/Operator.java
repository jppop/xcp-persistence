package org.pockito.xcp.entitymanager.query;

public enum Operator {
	eq("="), ne("<>"), gt(">"), ge(">="), lt("<"), le("<="), in("in"), is("is"), like("like")
	;	
	private final String dqlOperator;
	
	Operator(String dqlOperator) {
		this.dqlOperator = dqlOperator;
	}
	public String dqlOperator() {
		return this.dqlOperator;
	}
}