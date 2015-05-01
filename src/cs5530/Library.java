//Written by Nicolas Metz for CS5530 Spring 2015
package cs5530;

import java.sql.*;
import java.util.Calendar;

public class Library {
	public Library(){
	}
	
	//Function that allows a book to be returned or marks as lost on the current date
	public String returnBook(String lost, String isbn, String copy_number, Connection con){	
		String query= "";
		String resultstr = "";
		try{
	    	long time = System.currentTimeMillis();
	    	java.sql.Date date = new java.sql.Date(time);
	    	
	    	//This section checks to make sure a book is available
	    	query = "SELECT co.due_date, co.user_id FROM BOOK_STOCK bs, CHECK_OUT co where co.isbn = ? and bs.isbn = ? and co.copy_number = ? and bs.copy_number = ? and location = 'checkedout' or location = 'lost'";
	    	PreparedStatement check_state= con.prepareStatement(query);
	    	check_state.setString(1, isbn);
	    	check_state.setString(2, isbn);
	    	check_state.setString(3, copy_number);
	    	check_state.setString(4, copy_number);

	    	// if book is available
	    	ResultSet check_result = check_state.executeQuery();
	    	if (check_result.next()){
	    		java.sql.Date due_date = check_result.getDate("due_date");
	    		int user_id = check_result.getInt("user_id");

	    		//Updates the checkout table
		    	query = "UPDATE CHECK_OUT SET return_date = ? WHERE user_id = ? AND isbn = ? AND copy_number = ? AND due_date = ?";
		    	PreparedStatement state3 = con.prepareStatement(query);
		    	state3.setDate(1, date);
		    	state3.setInt(2, user_id);
		    	state3.setString(3, isbn);
		    	state3.setString(4, copy_number);
		    	state3.setDate(5, due_date);
		    	state3.executeUpdate();
		    	if(lost == "2")
		    	{
			    	query = "UPDATE BOOK_STOCK SET location = 'Lost' WHERE isbn = ? AND copy_number = ?";
			    	PreparedStatement state4 = con.prepareStatement(query);
			    	state4.setString(1, isbn);
			    	state4.setString(2, copy_number);
			    	//System.out.println(state4);
			    	state4.executeUpdate();
			    	resultstr = resultstr + ("*Book ISBN " + isbn +" has been marked as lost on: " + date);
		    	}
		    	else{
			    	query = "UPDATE BOOK_STOCK SET location = 'Shelving' WHERE isbn = ? AND copy_number = ?";
			    	PreparedStatement state4 = con.prepareStatement(query);
			    	state4.setString(1, isbn);
			    	state4.setString(2, copy_number);
			    	state4.executeUpdate();
			    	resultstr = resultstr + ("*Book ISBN " + isbn +" has been returned on: " + date);
		    	}
		    	
		    	query = "SELECT * FROM WAIT_LIST where isbn = ?";
		    	PreparedStatement query_stat = con.prepareStatement(query);
		    	query_stat.setString(1, "" + isbn);
		    	ResultSet rs1=query_stat.executeQuery();
		    	resultstr = resultstr +("Users on waitlist for book: ");
			    while(rs1.next())
			    {
			    	resultstr = resultstr +("USER_ID:");
			    	resultstr = resultstr +(rs1.getString("user_id") + "\n");
			    }
	    	}else{
	    		return "book with isbn " + isbn + " and copy number " + copy_number + " is not checked out or does not exist";
	    	}
	    	
		}
		catch(Exception e){
            return "Unable to execute query:"+query+" <BR>" + e.getMessage();
		}
    	return resultstr;
	}
	
	
	
	//Function that allows a user to review and rate a book on the given date
	public String reviewBook(String user_id, String isbn, String book_score, String book_review, Connection con){
		String query= "";
		String resultstr = "";
		try{
	    	long time = System.currentTimeMillis();
	    	java.sql.Date date = new java.sql.Date(time);
	    	
	    	//Ensures that the book exists in the library datatabase
	    	query = "SELECT * FROM BOOK_DIR where isbn = ?";
	    	PreparedStatement query_statment = con.prepareStatement(query);
	    	query_statment.setString(1, "" + isbn);
	    	ResultSet rs1=query_statment.executeQuery();
	    	if (!rs1.next()){
	        	return "A book with this ISBN does not currently exist in the library.";
	    	}
	    	
	    	query = "INSERT INTO REVIEWS (user_id, isbn, rating, review_text, review_date) "
	    			+ "VALUES(?,?,?,?,?)";
	    	PreparedStatement state1 = con.prepareStatement(query);
	    	state1.setString(1, user_id);
	    	state1.setString(2, isbn);
	    	state1.setString(3, book_score);
	    	state1.setString(4, book_review);
	    	state1.setDate(5, date);
	    	state1.executeUpdate();
	    	resultstr = ("Book review for book isbn: " + isbn + " successfully entered into database");
		}
		catch(Exception e){
            return "Unable to execute query:"+query+" <BR>" + e.getMessage();
		}
    	return resultstr;
	}
	
	
	
	
	//Function that returns a list of late books after taking a date input
	public String lateBooks(int days_away, Connection con){
		String query= "";
		String resultstr = "";
		try{
	    	long time = System.currentTimeMillis();
	    	java.sql.Date date = new java.sql.Date(time);
	    	
	    	Calendar cal = Calendar.getInstance();
	    	cal.setTime(date);
	    	cal.add(Calendar.DAY_OF_YEAR,days_away);
	    	java.sql.Date date1 = new java.sql.Date(cal.getTimeInMillis());

			query = "SELECT d.title, c.isbn, c.copy_number, c.due_date, u.uname, u.phone, u.email "
					+   "FROM CHECK_OUT c, LIB_USER u, BOOK_DIR d "
					+   "WHERE c.due_date < ? "
					+	"AND c.user_id = u.user_id "
					+	"AND c.isbn = d.isbn ";
	    	PreparedStatement query_stat = con.prepareStatement(query);
	    	query_stat.setString(1, "" + date1);
	    	ResultSet rs1=query_stat.executeQuery();
	    	
	    	resultstr = resultstr + ("List of late books using date " + date1 + ": \n");

		    while(rs1.next())
		    {
		    	resultstr = resultstr +("BOOK TITLE: ");
		    	resultstr = resultstr +(rs1.getString("title"));
		    	resultstr = resultstr +("	ISBN: ");
		    	resultstr = resultstr +(rs1.getString("isbn"));
		    	resultstr = resultstr +("	COPY NUMBER: ");
		    	resultstr = resultstr +(rs1.getString("copy_number"));
		    	resultstr = resultstr +("	DUE DATE: ");
		    	resultstr = resultstr +(rs1.getString("due_date"));
		    	resultstr = resultstr +("	NAME: ");
		    	resultstr = resultstr +(rs1.getString("uname"));
		    	resultstr = resultstr +("	PHONE: ");
		    	resultstr = resultstr +(rs1.getString("phone"));
		    	resultstr = resultstr +("	EMAIL: ");
		    	resultstr = resultstr +(rs1.getString("email"));
		    	resultstr = resultstr +("\n");
		    }			
		}
		catch(Exception e){
            return "Unable to execute query:"+query+" <BR>" + e.getMessage();
		}
    	return resultstr;
	}
	
	
	//Function for allowing users to check out books
	public String checkOut(String isbn, String user_id, Connection con){
		String query= "";
		String resultstr = "";
		try{
	    	query = "SELECT * FROM BOOK_STOCK where isbn = ?";
	    	PreparedStatement query_statment = con.prepareStatement(query);
	    	query_statment.setString(1, isbn);
	    	ResultSet rs1=query_statment.executeQuery();
	    	if (!rs1.next()){
	        	return ("You can only check out books that are currently stocked in the library.");	
	        }
	
	    	query = "SELECT * FROM WAIT_LIST where isbn = ?";
	    	PreparedStatement query_stat = con.prepareStatement(query);
	    	query_stat.setString(1, isbn);
	    	ResultSet rs2=query_stat.executeQuery();
	    	//Will go into this block if a waitlist for the book exists
	    	if (rs2.next()){
	    		//inside this block we try to find if the user_id is the one waiting the longest
		    	query = "SELECT user_id FROM WAIT_LIST where isbn = ? GROUP BY isbn HAVING min(wait_since)";
		    	PreparedStatement insert_stat = con.prepareStatement(query);
		    	insert_stat.setString(1, "" + isbn);
		    	ResultSet rs3=query_stat.executeQuery();
		    	rs3.next();
		    	int user_id2 = rs3.getInt(1);
		    	int user_id3 = Integer.parseInt(user_id);
		    	if (user_id3 == user_id2)
		    	{
		    		resultstr = "User " + user_id2 +" is first in line on the waitlist";
			    	query = "DELETE FROM WAIT_LIST "
			    			+ "WHERE isbn = ? AND user_id = ?";
			    	PreparedStatement state4= con.prepareStatement(query);
			    	state4.setString(1, isbn);
			    	state4.setInt(2, user_id2);
			    	//System.out.println(state4);
			    	state4.executeUpdate();

		    	}
		    	else
		    	{
		        	return ("There are people waiting for this item in front of you, so it is currently not available to checkout");
		    	}
	    	}
	    	
	    	//This section checks to make sure a book is available
	    	query = "SELECT copy_number FROM BOOK_STOCK where isbn = ? and location <> 'checkedout' and location <> 'lost'";
	    	PreparedStatement check_state= con.prepareStatement(query);
	    	check_state.setString(1, isbn);
	    	ResultSet check_result = check_state.executeQuery();
	    	if (check_result.next()){
		    	String copy_number = check_result.getString("copy_number");
		    	query = "INSERT INTO CHECK_OUT (user_id, isbn, copy_number, due_date) "
		    			+ "VALUES(?,?,?,?)";
		    	PreparedStatement state3 = con.prepareStatement(query);
		    	state3.setString(1, user_id);
		    	state3.setString(2, isbn);
		    	state3.setString(3, copy_number);
		    	long time = System.currentTimeMillis();
		    	java.sql.Date date = new java.sql.Date(time);
		    	Calendar cal = Calendar.getInstance();
		    	cal.setTime(date);
		    	cal.add(Calendar.DAY_OF_YEAR,30);
		    	java.sql.Date date1 = new java.sql.Date(cal.getTimeInMillis());
		    	state3.setDate(4,date1);
		    	state3.executeUpdate();

		    	query = "UPDATE BOOK_STOCK SET location = 'checkedout' WHERE isbn = ? AND copy_number = ?";
		    	PreparedStatement state4= con.prepareStatement(query);
		    	state4.setString(1, isbn);
		    	state4.setString(2, copy_number);
		    	state4.executeUpdate();
		    	
		    	resultstr = resultstr + ("*Book ISBN: " + isbn +" Copy Number: " + copy_number + " has been checked out. Due Date: " + date1);
	    	}
	    	else{
	        	return("There are currently no copies of this book available for checkout");
	    	}
		}
		catch(Exception e){
            return "Unable to execute query:"+query+" <BR>" + e.getMessage();
		}
    	return resultstr;
	}
	
	
	//Function for allowing users to be added to a waitlist
	public String waitList(String isbn, String user_id, Connection con){
		String query="";
		String resultstr="";
		try{
			/*
			String isbn = null;
			boolean isbn_loop = true;
			//this loop ensures the isbn exists in the database
			while (isbn_loop)
			{
				System.out.println("Enter the isbn of the books to be waited for:");
		    	isbn = in.nextLine();
		    	String query = "SELECT * FROM BOOK_DIR where isbn = ?";
		    	PreparedStatement query_statment = con.prepareStatement(query);
		    	query_statment.setString(1, "" + isbn);
		    	ResultSet rs1=query_statment.executeQuery();
		    	if (rs1.next()){
		    		isbn_loop = false;
		    	}
		    	else 
		    	{
		        	System.out.println("You can only wait for books that already exist in the library database.");
		        	System.out.println("Hit enter to return to the main menu and add information about this book to the database.");
		        	in.nextLine();
		        	return;
		    	}
			}*/
			
	    	query = "INSERT INTO WAIT_LIST(user_id, isbn, wait_since) "
	    			+ "VALUES(?,?,?)";
	    	PreparedStatement wait_state = con.prepareStatement(query);
	    	wait_state.setString(1, user_id);
	    	wait_state.setString(2, isbn);
	    	long time = System.currentTimeMillis();
	    	java.sql.Date date = new java.sql.Date(time);
	    	wait_state.setDate(3,date);
	    	wait_state.executeUpdate();
	    	resultstr = "user " + user_id + "added to waitlist for book with isbn:" + isbn;
		}
		catch(Exception e){
            return "Unable to execute query:"+query+" <BR>" + e.getMessage();
		}
		return resultstr;
	}
}
