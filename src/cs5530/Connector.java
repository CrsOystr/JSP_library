//Used by Nicolas Metz for Spring 2015 CS5530
//Base connector code given by Robert Christensen
package cs5530;

import java.sql.*;

public class Connector {
	public Connection con;
	public Statement stmt;
	public Connector() throws Exception {
		try{
		 	String userName = "cs5530u13";
	   		String password = "90a6snh1";
	        	String url = "jdbc:mysql://georgia.eng.utah.edu/cs5530db13";
		        Class.forName ("com.mysql.jdbc.Driver").newInstance ();
        		con = DriverManager.getConnection (url, userName, password);
        		stmt = con.createStatement();	
        } catch(Exception e) {
			System.err.println("Unable to open mysql jdbc connection. The error is as follows,\n");
            		System.err.println(e.getMessage());
			throw(e);
		}
	}
	
	public void closeConnection() throws Exception{
		con.close();
	}
	
 	public void closeStatement() throws Exception{
		stmt.close();
	}
}
