//Written by Nicolas Metz for CS5530 Spring 2015
package cs5530;

import java.sql.*;

public class Book {
	public Book(){
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
			System.err.println("Unable to execute query:"+query+"\n");
            System.err.println(e.getMessage());	
            return "Unable to execute query:"+query+" <BR>" + e.getMessage();
            }

    	return resultstr;
	}

}
