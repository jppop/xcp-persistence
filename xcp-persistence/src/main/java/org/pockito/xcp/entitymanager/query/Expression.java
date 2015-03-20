package org.pockito.xcp.entitymanager.query;

import org.pockito.xcp.entitymanager.api.PersistentProperty;

public class Expression<B> {
	public final PersistentProperty prop;
	public final RightExpression<B> rightOpt;
	
	public Expression(PersistentProperty prop, RightExpression<B> rightOp) {
		this.prop = prop;
		this.rightOpt = rightOp;
	}

}
