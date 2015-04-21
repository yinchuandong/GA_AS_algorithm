package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.naming.spi.DirStateFactory.Result;

import com.mysql.jdbc.Statement;


public class DbUtil {
	
	public static void main(String[] args){
//		System.out.println(System.currentTimeMillis());
		
//		String uniqueKey = AppUtil.md5("guangzhou-4");
//		String sql = "insert into t_crawled (sid,isVisited) values (?,?)";
//		DbUtil.executeUpdate(sql, new String[]{uniqueKey, "0"});
		
//		String sql2 = "update t_crawled set isVisited='1' where sid=?";
//		DbUtil.executeUpdate(sql2, new String[]{uniqueKey});
		
		int nums = count("select count(*) from t_crawled where sid=?", new String[]{"63b3f3f9afe9f8c75f3ca2f360e726d"});
		System.out.println(nums);
		
//		ResultSet resultSet = executeQuery("select * from t_crawled where isVisited=?", new String[]{"0"});
//		try {
//			while(resultSet.next()){
//				String tsid = resultSet.getString("sid");
//				String turl = resultSet.getString("url");
//				int tlayer = resultSet.getInt("layer");
//				System.out.println(tsid);
//				System.out.println(turl);;
//				System.out.println(tlayer);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		close(null, null, getConnection());
	}
	//连接
	private static java.sql.Connection conn = null;
	/**
	 * 打开数据库连接
	 * @return
	 */
	public static Connection getConnection(){
		try {
			if (conn != null && !conn.isClosed()) {
				return conn;
			}
			
			com.mysql.jdbc.Driver jdbcDriver = new com.mysql.jdbc.Driver();
			DriverManager.registerDriver(jdbcDriver);
			
//			String dbUrl = "jdbc:mysql://127.0.0.1:3306/travel?characterEncoding=gbk";
//			String dbUrl = "jdbc:mysql://192.168.233.21:3306/travel";
			String dbUrl = "jdbc:mysql://127.0.0.1:3306/traveldata?characterEncoding=utf-8";

			String dbUser = "123";
			String dbPwd = "123";
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
		} catch (Exception e) {
			System.err.println("数据库连接失败");
			e.printStackTrace();
		}
		return conn;
	}
	
	/**
	 * 关闭数据库连接，释放资源
	 * @param resultSet
	 * @param statement
	 * @param connection
	 */
	public static void close(ResultSet resultSet, PreparedStatement statement, Connection connection){
		
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		conn = null;
	}
	
	/**
	 * 关闭数据库连接
	 * 直接关闭connection
	 */
	public static void close(){
		close(null, null, getConnection());
	}
	
	public static int count(String sql, String[] params){
		ResultSet resultSet = executeQuery(sql, params);
		int nums = -1;
		try {
			while(resultSet.next()){
				nums = resultSet.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			close();
		}
		return nums;
	}
	
	/**
	 * 执行select 操作，执行完不会释放资源，需要用户自己释放
	 * @param sql
	 * @param params
	 * @return
	 */
	public static ResultSet executeQuery(String sql, String[] params){
		Connection conn = getConnection();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = conn.prepareStatement(sql);
			if (params != null) {
				for(int i=0; i<params.length; i++){
					statement.setString(i+1, params[i]);
				}
			}
			resultSet = statement.executeQuery();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
		}
		return resultSet;
	}
	
	/**
	 * 执行update/insert/delete语句
	 * 会自动调用close关闭连接，释放资源
	 * @param sql
	 * @param params
	 * @return
	 */
	public static int executeUpdate(String sql, String[] params){
		Connection conn = getConnection();
		PreparedStatement statement = null;
		int result = -1;
		try {
			statement = conn.prepareStatement(sql);
			if (params != null) {
				for(int i=0; i<params.length; i++){
					statement.setString(i+1, params[i]);
				}
			}
			result = statement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
//			close(null, statement, conn);
		}
		return result;
	}
	
	
}
