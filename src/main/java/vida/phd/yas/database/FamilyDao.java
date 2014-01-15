package vida.phd.yas.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import vida.phd.yas.database.entity.Family;

/**
 *
 * @author ehsun.behravesh
 */
public class FamilyDao {

  public Family load(Connection conn, Integer id) throws SQLException {
    Family result = null;
    String sql = "SELECT * FROM FAMILY WHERE ID = ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setInt(1, id);
    ResultSet resultSet = statement.executeQuery();

    if (resultSet.next()) {
      result = new Family();
      result.setId(resultSet.getInt("ID"));
      result.setName(resultSet.getString("NAME"));
    }

    return result;
  }
  
  public List<Family> loadAll(Connection conn) throws SQLException {
    List<Family> result = new ArrayList<>();
    String sql = "SELECT * FROM FAMILY";
    PreparedStatement statement = conn.prepareStatement(sql);    
    ResultSet resultSet = statement.executeQuery();

    while (resultSet.next()) {
      Family family = new Family();
      family.setId(resultSet.getInt("ID"));
      family.setName(resultSet.getString("NAME"));
      result.add(family);
    }

    return result;
  }

  public Family load(Connection conn, String name) throws SQLException {
    Family result = null;
    String sql = "SELECT * FROM FAMILY WHERE NAME = ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setString(1, name);
    ResultSet resultSet = statement.executeQuery();

    if (resultSet.next()) {
      result = new Family();
      result.setId(resultSet.getInt("ID"));
      result.setName(resultSet.getString("NAME"));
    }

    return result;
  }

  public Family insert(Connection conn, String name) throws SQLException {
    Family result = null;

    String sql = "INSERT INTO FAMILY (NAME) VALUES (?)";
    PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    statement.setString(1, name);    
    statement.executeUpdate();

    ResultSet generatedKeys = statement.getGeneratedKeys();

    if (generatedKeys.next()) {
      result = new Family();
      result.setName(name);
      result.setId(generatedKeys.getInt(1));
    }

    return result;
  }
  
  public void delete(Connection conn, Integer id) throws SQLException {
    String sql = "DELETE FROM FAMILY WHERE ID = ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setInt(1, id);
    statement.executeUpdate();
  }
}
