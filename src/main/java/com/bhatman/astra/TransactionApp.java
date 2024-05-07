package com.bhatman.astra;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.deleteFrom;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.insertInto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;

public class TransactionApp {

	private static Logger LOGGER = LoggerFactory.getLogger(TransactionApp.class);

	private static PreparedStatement insertCourseRecord;
	private static PreparedStatement insertStudentRecord;

	private static PreparedStatement deleteCourseRecord;
	private static PreparedStatement deleteStudentRecord;

	private CqlSession session;
	private int numOfCourses;
	private int numOfStudents;
	private int failStudentId;
	private boolean isBatch;

	public TransactionApp(String scbPath, String clientId, String clientSecret, int numOfCourses, int numOfStudents,
			int failStudentId, boolean isBatch) {
		super();
		session = AppUtil.getCQLSession(scbPath, clientId, clientSecret);
		AppUtil.createBatchTablesIfNotExists(session);
		insertCourseRecord = session.prepare(
				insertInto(AppUtil.BATCH_COURSE).value("course_id", bindMarker()).value("student_id", bindMarker())
						.value("value", bindMarker()).value("join_date", bindMarker()).build());
		insertStudentRecord = session.prepare(insertInto(AppUtil.BATCH_STUDENT).value("student_id", bindMarker())
				.value("course_id", bindMarker()).value("value", bindMarker()).value("join_date", bindMarker())
				.build());

		deleteCourseRecord = session.prepare(deleteFrom(AppUtil.BATCH_COURSE).whereColumn("course_id")
				.isEqualTo(bindMarker()).whereColumn("student_id").isEqualTo(bindMarker()).build());
		deleteStudentRecord = session.prepare(deleteFrom(AppUtil.BATCH_STUDENT).whereColumn("student_id")
				.isEqualTo(bindMarker()).whereColumn("course_id").isEqualTo(bindMarker()).build());

		this.numOfCourses = numOfCourses;
		this.numOfStudents = numOfStudents;
		this.failStudentId = failStudentId;
		this.isBatch = isBatch;
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			LOGGER.error(
					"Not all input args received. Please provide these four args: SCB-Path, Client-Id, Client-Secret");
			System.exit(-1);
		}
		int numOfCourses = 10; // Default
		int numOfStudents = 20; // Default
		int failStudentId = 15; // Default
		if (args.length > 3) {
			numOfCourses = Integer.parseInt(args[3]);
			numOfStudents = Integer.parseInt(args[4]);
			if (args.length == 6) {
				failStudentId = Integer.parseInt(args[5]);
			}
		}

		TransactionApp txApp = new TransactionApp(args[0], args[1], args[2], numOfCourses, numOfStudents, failStudentId, false);
		txApp.performInsertByMode();
		AppUtil.closeSession(txApp.session);

		TransactionApp batchApp = new TransactionApp(args[0], args[1], args[2], numOfCourses, numOfStudents, failStudentId, true);
		batchApp.performInsertByMode();
		AppUtil.closeSession(batchApp.session);
	}

	private void performInsertByMode() throws Exception {
		LOGGER.info("======================= PERFORMING Transactional INSERTS with Batch: {} =======================",
				isBatch);
		long testStartTime = Calendar.getInstance().getTimeInMillis();

		List<TxStatement> allStmts = createStatements();
		long testEndTime = Calendar.getInstance().getTimeInMillis();
		LOGGER.info("Took {} milliseconds to create {} statements", testEndTime - testStartTime, allStmts.size());

		testStartTime = Calendar.getInstance().getTimeInMillis();
		executeStatementsAsync(allStmts);
		testEndTime = Calendar.getInstance().getTimeInMillis();
		LOGGER.info("Took {} milliseconds to execute {} statements with Batch: {}", testEndTime - testStartTime,
				allStmts.size(), isBatch);
	}

	private List<TxStatement> createStatements() {
		List<TxStatement> allStmts = new ArrayList<>();
		IntStream.range(1, numOfCourses+1).forEach(cidx -> {
			IntStream.range(1, numOfStudents+1).forEach(sidx -> {
				BoundStatement cStmt = insertCourseRecord.bind(cidx, sidx, "Course: " + cidx + ", Student: " + sidx,
						Instant.now());
				BoundStatement sStmt = insertStudentRecord.bind(sidx, cidx, "Student: " + sidx + ", Course: " + cidx,
						Instant.now());
				if (isBatch) {
					TxStatement ts = new TxStatement(
							BatchStatement.builder(BatchType.LOGGED).addStatement(cStmt).addStatement(sStmt).build(),
							cidx, sidx);
					allStmts.add(ts);
				} else {
					allStmts.add(new TxStatement(cStmt, cidx, sidx));
					allStmts.add(new TxStatement(sStmt, cidx, sidx));
				}
			});
		});

		return allStmts;
	}

	private void executeStatementsAsync(List<TxStatement> statements) throws Exception {
		int idx = 0;
		for (TxStatement statement : statements) {
			statement.setCs(session.executeAsync(statement.getStatement()));
			idx++;
			if (isBatch && idx % 500 == 0) {
				LOGGER.info("Sleeping {} millis...", 100);
				Thread.sleep(100);
			}
			if (!isBatch && idx % 1000 == 0) {
				LOGGER.info("Sleeping {} millis...", 100);
				Thread.sleep(100);
			}
		}

		for (TxStatement statement : statements) {
			try {
				statement.getCs().toCompletableFuture().get().one();
				if (statement.getsId() == failStudentId) {
					throw new Exception("Exception while performing task for student id " + failStudentId);
				}
			} catch (Exception ex) {
				LOGGER.error("{}. Initiating deletes...", ex.getLocalizedMessage());
				session.execute(deleteCourseRecord.bind(statement.getcId(), statement.getsId()));
				session.execute(deleteStudentRecord.bind(statement.getcId(), statement.getsId()));
				LOGGER.error("Deletes complete");
				// Ideally, capture failed objects in a dead-letter queue & retry as needed
			}
		}

	}

}
