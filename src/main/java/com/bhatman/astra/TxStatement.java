package com.bhatman.astra;

import java.util.concurrent.CompletionStage;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;

public class TxStatement {
	@SuppressWarnings("rawtypes")
	private Statement statement;
	private CompletionStage<AsyncResultSet> cs;
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

	public CompletionStage<AsyncResultSet> getCs() {
		return cs;
	}

	public void setCs(CompletionStage<AsyncResultSet> cs) {
		this.cs = cs;
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
