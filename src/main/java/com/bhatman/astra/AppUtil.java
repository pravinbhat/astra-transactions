package com.bhatman.astra;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;

public class AppUtil {
	private static Logger LOGGER = LoggerFactory.getLogger(AppUtil.class);

	public static final String KEYSPACE_NAME = "test_ks";
	public static final String BATCH_COURSE = "BATCH_COURSE";
	public static final String BATCH_STUDENT = "BATCH_STUDENT";

	public static CqlSession getCQLSession(String scbPath, String clientId, String clientSecret) {
		CqlSession cqlSession = CqlSession.builder().withCloudSecureConnectBundle(Paths.get(scbPath))
				.withAuthCredentials(clientId, clientSecret).withKeyspace(KEYSPACE_NAME).build();

		return cqlSession;
	}

	public static void closeSession(CqlSession session) {
		if (session != null) {
			session.close();
		}
		LOGGER.info("Closed connection!");
	}

	public static void createBatchTablesIfNotExists(CqlSession session) {
		session.execute(SchemaBuilder.createTable(BATCH_COURSE).ifNotExists()
				.withPartitionKey("course_id", DataTypes.INT).withClusteringColumn("student_id", DataTypes.INT)
				.withColumn("value", DataTypes.TEXT).withColumn("join_date", DataTypes.TIMESTAMP).build());
		session.execute(QueryBuilder.truncate(BATCH_COURSE).build());
		LOGGER.info("Table '{}' has been created (if not exists) OR truncated (if exists).", BATCH_COURSE);

		session.execute(SchemaBuilder.createTable(BATCH_STUDENT).ifNotExists()
				.withPartitionKey("student_id", DataTypes.INT).withClusteringColumn("course_id", DataTypes.INT)
				.withColumn("value", DataTypes.TEXT).withColumn("join_date", DataTypes.TIMESTAMP).build());
		session.execute(QueryBuilder.truncate(BATCH_STUDENT).build());
		LOGGER.info("Table '{}' has been created (if not exists) OR truncated (if exists).", BATCH_STUDENT);
	}

}
