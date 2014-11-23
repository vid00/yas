package vida.phd.yas.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public enum Database {

  INSTANCE;

  public Connection getConnection() throws SQLException, ClassNotFoundException {
    Class.forName("org.sqlite.JDBC");
    return DriverManager.getConnection("jdbc:sqlite:database.db");        
    //return DriverManager.getConnection("jdbc:sqlite::memory:");
  }
}
