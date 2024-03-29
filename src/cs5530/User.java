//Written by Nicolas Metz for CS5530 Spring 2015
package cs5530;

import java.sql.*;
import java.util.Calendar;

public class User {
	public User() {
	}
	
	//Function returns statistics for N amount of users - Shows users who have checked out, lost, or rated the most books
	public String userStats(int n, Connection con){
		String query = "";
		String resultstr = "";
		try{
		    query = "SELECT c.user_id, l.uname, COUNT(c.user_id) "
		    		+ "FROM CHECK_OUT c, LIB_USER l "
		    		+ "WHERE c.user_id = l.user_id "
		    		+ "GROUP BY c.user_id "
		    		+ "ORDER BY count(c.user_id) desc "
		    		+ "LIMIT ?";
		    PreparedStatement state1 = con.prepareStatement(query);
		    state1.setInt(1, n);
		    ResultSet rs1=state1.executeQuery();
		    resultstr = resultstr + ("*****Top " + n + " users who have check out the most books***** ");
		    while(rs1.next())
		    {
		    	resultstr = resultstr + ("<br>User ID: ");
		    	resultstr = resultstr + (rs1.getString("user_id"));
		    	resultstr = resultstr + ("		Full Name: ");
		    	resultstr = resultstr + (rs1.getString("uname"));
		    	resultstr = resultstr + ("		Books Checked Out: ");
		    	resultstr = resultstr + (rs1.getString("COUNT(c.user_id)"));
		    }
		    
		    
		    query = "SELECT c.user_id, l.uname, COUNT(c.user_id) "
		    		+ "FROM REVIEWS c, LIB_USER l "
		    		+ "WHERE c.user_id = l.user_id "
		    		+ "GROUP BY c.user_id "
		    		+ "ORDER BY count(c.user_id) desc "
		    		+ "LIMIT ?";
		    PreparedStatement state2 = con.prepareStatement(query);
		    state2.setInt(1, n);
		    ResultSet rs2=state2.executeQuery();
		    resultstr = resultstr + ("<br><br>*****Top " + n + " users who have rated the most books***** ");
		    while(rs2.next())
		    {
		    	resultstr = resultstr + ("<br>User ID: ");
		    	resultstr = resultstr + (rs2.getString("user_id"));
		    	resultstr = resultstr + ("		Full Name: ");
		    	resultstr = resultstr + (rs2.getString("uname"));
		    	resultstr = resultstr + ("		Books Rated: ");
		    	resultstr = resultstr + (rs2.getString("COUNT(c.user_id)"));
		    }
		    
		    //Query and section for reporting the n users who have lost the most books
		    query = "SELECT l.user_id, l.uname, count(l.user_id) "
		    		+ "FROM CHECK_OUT c, LIB_USER l, BOOK_STOCK b "
		    		+ "WHERE c.isbn = b.isbn "
		    		+ "AND c.copy_number = b.copy_number "
		    		+ "AND l.user_id = c.user_id "
		    		+ "AND b.location = 'Lost' "
		    		+ "GROUP BY l.user_id "
		    		+ "ORDER BY count(c.user_id) desc "
		    		+ "LIMIT ?";
		    PreparedStatement state3 = con.prepareStatement(query);
		    state3.setInt(1, n);
		   // System.out.println(state3);
		    ResultSet rs3=state3.executeQuery();
		    resultstr = resultstr + ("<br><br>*****Top " + n + " users who have lost the most books***** ");
		    while(rs3.next())
		    {
		    	resultstr = resultstr + ("<br>User ID: ");
		    	resultstr = resultstr + (rs3.getString("user_id"));
		    	resultstr = resultstr + ("		Full Name: ");
		    	resultstr = resultstr + (rs3.getString("uname"));
		    	resultstr = resultstr + ("		Books Lost: ");
		    	resultstr = resultstr + (rs3.getString("COUNT(l.user_id)"));
		    }
		}
		catch(Exception e){
			System.err.println("Unable to execute query:"+query+"<BR>");
            System.err.println(e.getMessage());
		}
		return resultstr;
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
		    query = "SELECT * FROM LIB_USER where user_id = ?";
		    PreparedStatement state2 = con.prepareStatement(query);
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
		    query = "SELECT * FROM BOOK_STOCK bs, CHECK_OUT co, BOOK_DIR bd "
		    		+ "where co.user_id = ? "
		    		+ "and co.copy_number = bs.copy_number "
		    		+ "AND co.isbn = bs.isbn "
		    		+ "AND co.isbn = bd.isbn "
		    		+ "and co.return_date IS NOT NULL "
		    		+ "and location <> 'Lost'";
		    PreparedStatement state3 = con.prepareStatement(query);
		    state3.setString(1, user_id);
		   // System.out.println(state3);
		    ResultSet rs3=state3.executeQuery();
		    resultstr = resultstr + ("<BR><BR>*****Returned Books*****");
		    while(rs3.next())
		    {
		    	resultstr = resultstr + ("<BR><BR>Title: ");
		    	resultstr = resultstr + (rs3.getString("title"));
		    	resultstr = resultstr + ("   <BR>ISBN: ");
		    	resultstr = resultstr + (rs3.getString("isbn"));
		    	resultstr = resultstr + ("   <BR>Check Out Date: ");
		    	java.sql.Date date = rs3.getDate("due_date");
		    	Calendar cal = Calendar.getInstance();
		    	cal.setTime(date);
		    	cal.add(Calendar.DAY_OF_YEAR,-30);
		    	java.sql.Date date1 = new java.sql.Date(cal.getTimeInMillis());
		    	resultstr = resultstr + (date1);
		    	resultstr = resultstr + ("   Return Date: ");
		    	resultstr = resultstr + (rs3.getString("return_date"));
		    }
		    
		    
		    
		    //Returns all Books Checked Out at the moment by user
		    query = "SELECT * FROM BOOK_STOCK bs, CHECK_OUT co, BOOK_DIR bd "
		    		+ "where co.user_id = ? "
		    		+ "and co.copy_number = bs.copy_number "
		    		+ "AND co.isbn = bs.isbn "
		    		+ "AND co.isbn = bd.isbn "
		    		+ "and co.return_date is NULL";
		    PreparedStatement state = con.prepareStatement(query);
		    state.setString(1, user_id);
		   // System.out.println(state3);
		    ResultSet rs=state.executeQuery();
		    resultstr = resultstr + ("<BR><BR>*****Checked Out Books*****");
		    while(rs.next())
		    {
		    	resultstr = resultstr + ("<BR><BR>Title: ");
		    	resultstr = resultstr + (rs.getString("title"));
		    	resultstr = resultstr + ("   <BR>ISBN: ");
		    	resultstr = resultstr + (rs.getString("isbn"));
		    	resultstr = resultstr + ("   <BR>Check Out Date: ");
		    	java.sql.Date date = rs.getDate("due_date");
		    	Calendar cal = Calendar.getInstance();
		    	cal.setTime(date);
		    	cal.add(Calendar.DAY_OF_YEAR,-30);
		    	java.sql.Date date1 = new java.sql.Date(cal.getTimeInMillis());
		    	resultstr = resultstr + (date1);
		    	resultstr = resultstr + ("   Due Date: ");
		    	resultstr = resultstr + (rs.getString("due_date"));
		    }
		    
		    
		    
		    //Returns all books lost by the user
		    query = "SELECT * FROM BOOK_STOCK bs, CHECK_OUT co, BOOK_DIR bd "
		    		+ "where co.user_id = ? "
		    		+ "and co.copy_number = bs.copy_number "
		    		+ "AND co.isbn = bs.isbn "
		    		+ "AND co.isbn = bd.isbn "
		    		+ "and location = 'Lost'";
		    PreparedStatement state4 = con.prepareStatement(query);
		    state4.setString(1, user_id);
		   // System.out.println(state3);
		    ResultSet rs4=state4.executeQuery();
		    resultstr = resultstr + ("<BR><BR>*****Lost Books*****");
		    while(rs4.next())
		    {
		    	resultstr = resultstr + ("<BR><BR>Title: ");
		    	resultstr = resultstr + (rs4.getString("title"));
		    	resultstr = resultstr + ("   <BR>ISBN: ");
		    	resultstr = resultstr + (rs4.getString("isbn"));
		    	resultstr = resultstr + ("   <BR>CheckOutDate: ");
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
		    query = "SELECT * FROM WAIT_LIST w, BOOK_DIR b "
		    		+ "where w.user_id = ? "
		    		+ "AND w.isbn = b.isbn";
		    PreparedStatement state5 = con.prepareStatement(query);
		    state5.setString(1, user_id);
		    //System.out.println(state5);
		    ResultSet rs5=state5.executeQuery();
		    resultstr = resultstr + ("<BR><BR>*****Waiting For Books*****");
		    while(rs5.next())
		    {
		    	resultstr = resultstr + ("<BR><BR>Title: ");
		    	resultstr = resultstr + (rs5.getString("title"));
		    	resultstr = resultstr + ("   <BR>ISBN: ");
		    	resultstr = resultstr + (rs5.getString("isbn"));
		    	resultstr = resultstr + ("   <BR>Waiting Since: ");
		    	resultstr = resultstr + (rs5.getDate("wait_since"));
		    }
		    
		    //Returns Al the reviews a user has posted
		    query = "SELECT * FROM REVIEWS r, BOOK_DIR b "
		    		+ "where r.user_id = ? "
		    		+ "AND r.isbn = b.isbn";
		    PreparedStatement state6 = con.prepareStatement(query);
		    state6.setString(1, user_id);
		    ResultSet rs6=state6.executeQuery();
		    resultstr = resultstr + ("<BR><BR>*****Book Reviews*****");
		    while(rs6.next())
		    {
		    	resultstr = resultstr + ("<BR><BR>Title: ");
		    	resultstr = resultstr + (rs6.getString("title"));
		    	resultstr = resultstr + ("<BR>ISBN: ");
		    	resultstr = resultstr + (rs6.getString("isbn"));
		    	resultstr = resultstr + ("<BR>Review Date: ");
		    	resultstr = resultstr + (rs6.getDate("review_date"));
		    	resultstr = resultstr + ("<BR>Rating: ");
		    	resultstr = resultstr + (rs6.getString("rating"));
		    	resultstr = resultstr + ("<BR>Review: ");
		    	resultstr = resultstr + (rs6.getString("review_text"));
		    }
		}
		catch(Exception e){
			System.err.println("Unable to execute query:"+query+"<BR>");
            System.err.println(e.getMessage());
		}
		return resultstr;
	}
	

}
