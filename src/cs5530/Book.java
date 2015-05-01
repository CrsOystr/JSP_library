//Written by Nicolas Metz for CS5530 Spring 2015
package cs5530;

import java.sql.*;
import java.util.Calendar;

public class Book {
	public Book(){
	}
	
	//Returns report for a certain book - gives information and statistics based on isbn
	public String bookReport(String isbn, Connection con){
		String query="";
		String resultstr="";
		try{
			
	    	query = "SELECT * FROM BOOK_DIR where isbn = ?";
	    	PreparedStatement query_statment = con.prepareStatement(query);
	    	query_statment.setString(1, "" + isbn);
	    	ResultSet rs1=query_statment.executeQuery();
	    	if (!rs1.next()){
	        	return "A book with this isbn does not exist";
	    	}
	    	
		    //returns all of BOOK info
		    query = "SELECT * FROM BOOK_DIR where isbn = ?";
		    PreparedStatement state2 = con.prepareStatement(query);
		    state2.setString(1, isbn);
		    ResultSet rs2=state2.executeQuery();
		    resultstr = resultstr + ("*****BOOK INFO***** ");
		    while(rs2.next())
		    {
		    	resultstr = resultstr + ("Title: ");
		    	resultstr = resultstr + (rs2.getString("title"));
		    	resultstr = resultstr + ("<br>Publisher: ");
		    	resultstr = resultstr + (rs2.getString("publisher"));
		    	resultstr = resultstr + ("<br>Publication Year: ");
		    	resultstr = resultstr + (rs2.getString("pub_year"));
		    	resultstr = resultstr + ("<br>Book Format: ");
		    	resultstr = resultstr + (rs2.getString("book_format"));
		    	resultstr = resultstr + ("<br>Subject: ");
		    	resultstr = resultstr + (rs2.getString("book_subject"));
		    	resultstr = resultstr + ("<br>Summary: ");
		    	resultstr = resultstr + (rs2.getString("summary"));
		    }
		    
		    
		    query = "SELECT * FROM AUTHOR WHERE isbn = ?";
		    PreparedStatement state = con.prepareStatement(query);
		    state.setString(1,isbn);
		    ResultSet rs = state.executeQuery();
	    	resultstr = resultstr + ("<br>Authors: ");

		    while(rs.next())
		    {
		    	resultstr = resultstr + (rs2.getString("author"));
		    }
		    
		    
		    
		    //Returns ALl Books In stock
		    query = "SELECT * FROM BOOK_STOCK bs "
		    		+ "where isbn = ?";
		    PreparedStatement state3 = con.prepareStatement(query);
		    state3.setString(1, isbn);
		    ResultSet rs3=state3.executeQuery();
		    resultstr = resultstr + ("<br><br>*****Copies of the Book*****");
		    while(rs3.next())
		    {
		    	resultstr = resultstr + ("<br>Copy Number: ");
		    	resultstr = resultstr + (rs3.getString("copy_number"));
		    	resultstr = resultstr + ("   Location: ");
		    	resultstr = resultstr + (rs3.getString("location"));
		    }
		    
		    //Returns all users that have checked out book and when
		    query = "SELECT * FROM CHECK_OUT co, LIB_USER lu "
		    		+ "where co.user_id = lu.user_id "
		    		+ "and isbn = ?";
		    PreparedStatement state4 = con.prepareStatement(query);
		    state4.setString(1, isbn);
		    ResultSet rs4=state4.executeQuery();
		    resultstr = resultstr + ("<br><br>*****Users Who Have Checked This Book Out*****");
		    while(rs4.next())
		    {
		    	resultstr = resultstr + ("<br>User ID: ");
		    	resultstr = resultstr + (rs4.getString("user_id"));
		    	resultstr = resultstr + ("   User Name: ");
		    	resultstr = resultstr + (rs4.getString("uname"));
		    	resultstr = resultstr + ("   Check Out Date: ");
		    	java.sql.Date date = rs4.getDate("due_date");
		    	Calendar cal = Calendar.getInstance();
		    	cal.setTime(date);
		    	cal.add(Calendar.DAY_OF_YEAR,-30);
		    	java.sql.Date date1 = new java.sql.Date(cal.getTimeInMillis());
		    	resultstr = resultstr + (date1);
		    	resultstr = resultstr + ("   Returned on: ");
		    	resultstr = resultstr + (rs4.getString("return_date"));
		    }
		    
		    //Returns All the reviews on a certain book
		    query = "SELECT r.review_date, r.rating, r.review_text "
		    		+ "FROM REVIEWS r, BOOK_DIR b "
		    		+ "where r.isbn = ? "
		    		+ "AND r.isbn = b.isbn";
		    PreparedStatement state6 = con.prepareStatement(query);
		    state6.setString(1, isbn);
		    //resultstr = resultstr + ln(state6);
		    ResultSet rs6=state6.executeQuery();
		    resultstr = resultstr + ("<br><br>*****Book Reviews*****");
		    while(rs6.next())
		    {
		    	resultstr = resultstr + ("<br>Review Date: ");
		    	resultstr = resultstr + (rs6.getDate("review_date"));
		    	resultstr = resultstr + ("   Rating: ");
		    	resultstr = resultstr + (rs6.getString("rating"));
		    	resultstr = resultstr + ("   Review: ");
		    	resultstr = resultstr + (rs6.getString("review_text"));
		    }
			
		    
		    query = "SELECT AVG(r.rating) "
		    		+ "FROM REVIEWS r, BOOK_DIR b "
		    		+ "where r.isbn = ? "
		    		+ "AND r.isbn = b.isbn";
		    PreparedStatement state7 = con.prepareStatement(query);
		    state7.setString(1, isbn);
		    ResultSet rs7=state7.executeQuery();
		    if(rs7.next()){
		    	resultstr = resultstr + ("<br>Average Review: ");
		    	resultstr = resultstr + (rs7.getString(1));
		    }
		}
		catch(Exception e){
			return "Unable to execute query:"+query+" <BR>" + e.getMessage();
        }
    	return resultstr;
	}
	
	
	//Function for adding copies of specified book to database, can choose any number of copies
	public String addBooks(String isbn, int copies, String location, Connection con){
		String query="";
		String resultstr="";
		try{
			query = "SELECT * FROM BOOK_DIR where isbn = ?";
	    	PreparedStatement query_statment = con.prepareStatement(query);
	    	query_statment.setString(1, "" + isbn);
	    	ResultSet rs1=query_statment.executeQuery();
	    	if (!rs1.next()){
	        	return "You can only add copies of books that already exist in the library database.";
	    	}
			
	    	query = "SELECT MAX(copy_number) FROM BOOK_STOCK WHERE isbn = ? GROUP BY isbn";
	    	PreparedStatement query2 = con.prepareStatement(query);
	    	query2.setString(1, "" + isbn);
	    	ResultSet rs2=query2.executeQuery();
    		int copy_start = 1;
	    	if(rs2.next()){
	    		copy_start = rs2.getInt(1) + 1;
	    	}
	    	for (int i = copy_start; i < copy_start + copies; i++)
	    	{
		    	query = "INSERT INTO BOOK_STOCK(isbn, copy_number, location) "
		    			+ "VALUES(?,?,?)";
		    	PreparedStatement book_state = con.prepareStatement(query);
		    	book_state.setString(1, isbn);
		    	book_state.setInt(2, i);
		    	book_state.setString(3,location);
		    	book_state.executeUpdate();
	    	}
	    	resultstr = ( copies + " copies of the book with isbn" + isbn + " have succesfully been added to the database");
		}
		catch(Exception e){
            return "Unable to execute query:"+query+" <BR>" + e.getMessage();
		}
		return resultstr;
	}
	
	
	//Function allows information about a new book to be added to the database - DOES NOT ADD ANY COPIES
	public String newBook(String isbn, String title, String author, String publisher, String pub_year, String format, String book_subject, String summary, Connection con){
		String resultstr = "";
    	String query = ""; 
		try{
			
			//Checks to ensure isbn is not in system.
	    	query = "SELECT * FROM BOOK_DIR where isbn = ?";
	    	PreparedStatement isbn_statement = con.prepareStatement(query);
	    	isbn_statement.setString(1, "" + isbn);
	    	ResultSet rs1=isbn_statement.executeQuery();
	    	if (rs1.next()){
	    		 resultstr = "This isbn already exists in database: Please try another";
	    		return resultstr;
	    	}
	    	
	    	//inserts book if isbn is not taken
	    	query = "INSERT INTO BOOK_DIR (isbn, title, publisher, pub_year, book_format, book_subject, summary) "
	    			+ "VALUES(?,?,?,?,?,?,?)"; 
	    	PreparedStatement book_statement = con.prepareStatement(query);
	    	book_statement.setString(1, isbn);
	    	book_statement.setString(2, title);
	    	book_statement.setString(3, publisher);
	    	book_statement.setString(4, pub_year);
	    	book_statement.setString(5, format);
	    	book_statement.setString(6, book_subject);
	    	book_statement.setString(7, summary);
			book_statement.executeUpdate();
			
			String[] author_array = author.split(",");
			for(String authors : author_array){
				authors = authors.trim();
				query = "INSERT INTO AUTHOR(isbn, author)"
		    			+ "VALUES(?,?)"; 
		    	PreparedStatement author_statement = con.prepareStatement(query);
		    	author_statement.setString(1, isbn);
		    	author_statement.setString(2, authors);
		    	author_statement.executeUpdate();
			}
			
			resultstr = ("*New book " + title +" has been added to the database. isbn: " + isbn);
		}
		catch(Exception e){
			System.err.println("Unable to execute query:"+query+"<br>");
            System.err.println(e.getMessage());	
            return "Unable to execute query:"+query+" <BR>" + e.getMessage();
            }

    	return resultstr;
	}

}
