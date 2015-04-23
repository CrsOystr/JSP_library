//Written by Nicolas Metz for CS5530 Spring 2015
package cs5530;

import java.sql.*;
import java.util.Calendar;

public class User {
	public User() {
	}
	
	//Method to add a new user to the database
	public String newUser(String username, String name, String address, String email, String phone, Statement stmt){
		String query = "";
		String resultstr = "";
		ResultSet IDresults;
		int user_id;
		
    	//Finds the current max ID number in the SQL database and increments it.
    	String ID_query = "SELECT MAX(user_id) FROM LIB_USER";
    	try{
    	IDresults= stmt.executeQuery(ID_query);
    	IDresults.next();
    	user_id = IDresults.getInt(1) + 1;

    	//MAY HAVE TO FIX THIS TO PREPARED 
    	//query = "INSERT INTO LIB_USER(user_id, username, uname, address, email, phone)"
    	//		+ "VALUES('"+user_id+"','"+username+"','"+name+"','"+address+"','"+email+"','"+phone+"')"; 
		
    	query = "INSERT INTO LIB_USER(user_id, username, uname, address, email, phone)"
    	 + "VALUES('"+user_id+"','"+username+"','"+name+"','"+address+"','"+email+"','"+phone+"')"; 	
 
    	stmt.executeUpdate(query);
		resultstr = "*New user " + username +" has been added to the database. UserID: " + user_id;
		
    	}
    	catch (Exception e){
            return "Unable to execute query:"+query+" <BR>" + e.getMessage();

    	}
		return resultstr;
	}
	
	//
	public String userReport(String user_id, Connection con)
	{
		String query = "";
		String resultstr = "";
		try{
	    	query = "SELECT * FROM LIB_USER where user_id = ?";
			PreparedStatement query_statment = con.prepareStatement(query);
	    	query_statment.setString(1, "" + user_id);
	    	ResultSet rs1=query_statment.executeQuery();
	    	if (!rs1.next()){
	        	return "A user with this ID does not exist";
	    	}
	    	
		    //returns all of user information
		    String query2 = "SELECT * FROM LIB_USER where user_id = ?";
		    PreparedStatement state2 = con.prepareStatement(query2);
		    state2.setString(1, user_id);
		    ResultSet rs2=state2.executeQuery();
		    //System.out.println("*****User Info***** ");
		    while(rs2.next())
		    {
		    	resultstr = resultstr + "Username:" + rs2.getString("username") +
		    	"<BR>Full Name: " + rs2.getString("uname") + "<BR>Address:" + rs2.getString("address") +
		    	"<BR>Email: " + rs2.getString("email") + "<BR>Phone: " + rs2.getString("phone");
		    }
		    
		    //Returns all Books Checked Out and Returned by user
		    String query3 = "SELECT * FROM BOOK_STOCK bs, CHECK_OUT co, BOOK_DIR bd "
		    		+ "where co.user_id = ? "
		    		+ "and co.copy_number = bs.copy_number "
		    		+ "AND co.isbn = bs.isbn "
		    		+ "AND co.isbn = bd.isbn "
		    		+ "and location <> 'checkedout' "
		    		+ "and location <> 'Lost'";
		    PreparedStatement state3 = con.prepareStatement(query3);
		    state3.setString(1, user_id);
		   // System.out.println(state3);
		    ResultSet rs3=state3.executeQuery();
		    resultstr = resultstr + ("<BR><BR>*****Returned Books*****");
		    while(rs3.next())
		    {
		    	resultstr = resultstr + ("<BR>Title: ");
		    	resultstr = resultstr + (rs3.getString("title"));
		    	resultstr = resultstr + ("   ISBN: ");
		    	resultstr = resultstr + (rs3.getString("isbn"));
		    	resultstr = resultstr + ("   CheckOutDate: ");
		    	java.sql.Date date = rs3.getDate("due_date");
		    	Calendar cal = Calendar.getInstance();
		    	cal.setTime(date);
		    	cal.add(Calendar.DAY_OF_YEAR,-30);
		    	java.sql.Date date1 = new java.sql.Date(cal.getTimeInMillis());
		    	resultstr = resultstr + (date1);
		    	resultstr = resultstr + ("   ReturnDate: ");
		    	resultstr = resultstr + (rs3.getString("return_date"));
		    }
		    
		    //Returns all books lost by the user
		    String query4 = "SELECT * FROM BOOK_STOCK bs, CHECK_OUT co, BOOK_DIR bd "
		    		+ "where co.user_id = ? "
		    		+ "and co.copy_number = bs.copy_number "
		    		+ "AND co.isbn = bs.isbn "
		    		+ "AND co.isbn = bd.isbn "
		    		+ "and location = 'Lost'";
		    PreparedStatement state4 = con.prepareStatement(query4);
		    state4.setString(1, user_id);
		   // System.out.println(state3);
		    ResultSet rs4=state4.executeQuery();
		    resultstr = resultstr + ("<BR><BR>*****Lost Books*****");
		    while(rs4.next())
		    {
		    	resultstr = resultstr + ("<BR>Title: ");
		    	resultstr = resultstr + (rs4.getString("title"));
		    	resultstr = resultstr + ("   ISBN: ");
		    	resultstr = resultstr + (rs4.getString("isbn"));
		    	resultstr = resultstr + ("   CheckOutDate: ");
		    	java.sql.Date date = rs4.getDate("due_date");
		    	Calendar cal = Calendar.getInstance();
		    	cal.setTime(date);
		    	cal.add(Calendar.DAY_OF_YEAR,-30);
		    	java.sql.Date date1 = new java.sql.Date(cal.getTimeInMillis());
		    	resultstr = resultstr + (date1);
		    	resultstr = resultstr + ("   Marked Lost on: ");
		    	resultstr = resultstr + (rs4.getString("return_date"));
		    }
		    
		    //Returns All Books USer is Waiting for
		    String query5 = "SELECT * FROM WAIT_LIST w, BOOK_DIR b "
		    		+ "where w.user_id = ? "
		    		+ "AND w.isbn = b.isbn";
		    PreparedStatement state5 = con.prepareStatement(query5);
		    state5.setString(1, user_id);
		    //System.out.println(state5);
		    ResultSet rs5=state5.executeQuery();
		    resultstr = resultstr + ("<BR><BR>*****Waiting For Books*****");
		    while(rs5.next())
		    {
		    	resultstr = resultstr + ("<BR>Title: ");
		    	resultstr = resultstr + (rs5.getString("title"));
		    	resultstr = resultstr + ("   ISBN: ");
		    	resultstr = resultstr + (rs5.getString("isbn"));
		    	resultstr = resultstr + ("   Waiting Since: ");
		    	resultstr = resultstr + (rs5.getDate("wait_since"));
		    }
		    
		    //Returns All the reviews a user has posted
		    String query6 = "SELECT * FROM REVIEWS r, BOOK_DIR b "
		    		+ "where r.user_id = ? "
		    		+ "AND r.isbn = b.isbn";
		    PreparedStatement state6 = con.prepareStatement(query6);
		    state6.setString(1, user_id);
		    //System.out.println(state6);
		    ResultSet rs6=state6.executeQuery();
		    resultstr = resultstr + ("<BR><BR>*****Book Reviews*****");
		    while(rs6.next())
		    {
		    	resultstr = resultstr + ("<BR>Title: ");
		    	resultstr = resultstr + (rs6.getString("title"));
		    	resultstr = resultstr + ("   ISBN: ");
		    	resultstr = resultstr + (rs6.getString("isbn"));
		    	resultstr = resultstr + ("   Review Date: ");
		    	resultstr = resultstr + (rs6.getDate("review_date"));
		    	resultstr = resultstr + ("   Rating: ");
		    	resultstr = resultstr + (rs6.getString("rating"));
		    	resultstr = resultstr + ("   Review: ");
		    	resultstr = resultstr + (rs6.getString("review_text"));
		    }
		}
		catch(Exception e){
			System.err.println("Unable to execute query:"+query+"<\n");
            System.err.println(e.getMessage());
		}
		return resultstr;
	}
	

}
