package com.company.fs.analytics.kafka.dbfactory;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class establishes connection with the control table in staging database and returns the last time-stamp which got processed.
 * It also update the control table with the latest time-stamp which got processed to kafka topic. 
 *
 * It reads the database properties from the properties file - dbconnection.properties
 */
public class DBConnector {

	private static final Logger logger = LoggerFactory.getLogger(DBConnector.class);
	private Connection con = null;

	public DBConnector() {
		try {
			Properties props = new Properties();
			String dbPropertyFile = "C:\\Users\\pravat.sutar\\FSCodebase\\RestToKafkaUtility\\src\\main\\resources\\dbconnection.properties";
			FileReader fReader = new FileReader(dbPropertyFile);

			props.load(fReader);

			String dbDriverClass = props.getProperty("db.driver.class");
			String dbConnUrl = props.getProperty("db.conn.url");
			String dbUserName = props.getProperty("db.username");
			String dbPassword = props.getProperty("db.password");

			if (!"".equals(dbDriverClass) && !"".equals(dbConnUrl)) {
				con = DriverManager.getConnection(dbConnUrl, dbUserName, dbPassword);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateControlTable(String s, int num_record, String offset) throws Exception {
		try {
			Statement stmt = con.createStatement();
			logger.info("updateControlTable(): The values receieved are " + s + " " + num_record + " " + offset);
			String query = "INSERT INTO rest_control (refresh_dt,offset,last_ts,num_record) VALUE (CURRENT_TIMESTAMP,'"
					+ offset + "','" + s + "'," + num_record + ")";
			stmt.executeUpdate(query);
			logger.info("Control table gets updated..");
			con.close();
		} catch (Exception e) {
			logger.error("Exception while updating control table! ", e.getMessage());
		}
	}

	public String getLastTimeStamp() throws ClassNotFoundException, SQLException {
		String result = "";
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(
				"select max(DATE_FORMAT( last_ts, '%Y:%m:%d:%H:%i:%S.%f')) as last_ts from stg.rest_control");
		while (rs.next()) {
			result = rs.getString(1);
			logger.info("getLastTimeStamp():Latest timestamp received from control table" + result);
		}
		con.close();
		return result.toString();
	}
}
