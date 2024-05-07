package com.bhatman.astra;

import com.datastax.oss.driver.api.core.cql.Statement;

public class TxStatement {
	@SuppressWarnings("rawtypes")
	private Statement statement;
	private int cId;
	private int sId;

	@SuppressWarnings("rawtypes")
	public TxStatement(Statement statement, int cId, int sId) {
		super();
		this.statement = statement;
		this.cId = cId;
		this.sId = sId;
	}

	@SuppressWarnings("rawtypes")
	public Statement getStatement() {
		return statement;
	}

	@SuppressWarnings("rawtypes")
	public void setStatement(Statement statement) {
		this.statement = statement;
	}

	public int getcId() {
		return cId;
	}

	public void setcId(int cId) {
		this.cId = cId;
	}

	public int getsId() {
		return sId;
	}

	public void setsId(int sId) {
		this.sId = sId;
	}
}
