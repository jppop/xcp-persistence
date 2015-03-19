package org.pockito.xcp.entitymanager.query;

import java.util.ArrayList;
import java.util.List;

public class RightExpression<B>  {

	private final B value;
	private final List<B> values;
	private final boolean multipleValue;

	public final Operator operator;

	private RightExpression(Operator op, B value) {
		this.operator = op;
		this.value = value;
		this.values = null;
		this.multipleValue = false;
	}

	@SafeVarargs
	private RightExpression(Operator op, B... values) {
		this.operator = op; // force IN op ?
		this.value = null;
		List<B> valueList = new ArrayList<B>();
		for (B singleValue : values) {
			valueList.add(singleValue);
		}
		this.values = valueList;
		this.multipleValue = true;
	}

	public B value() {
		return value;
	}

	public List<B> values() {
		return this.values;
	}

	public Operator op() {
		return operator;
	}

	public boolean isMultipleValue() {
		return multipleValue;
	}

	public static <B> RightExpression<B> eq(B value) {
		return new RightExpression<B>(Operator.eq, value);
	}
	public static <B> RightExpression<B> gt(B value) {
		return new RightExpression<B>(Operator.gt, value);
	}
	@SafeVarargs
	public static <B> RightExpression<B> in(B... values) {
		return new RightExpression<B>(Operator.in, values);
	}

}
