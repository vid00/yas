package vida.phd.yas.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import vida.phd.yas.database.entity.BasicBlock;

public class BasicBlockDao {

  public BasicBlock read(Connection conn, Integer malwareId, String hash) throws SQLException {
    BasicBlock result = null;

    String sql = "SELECT * FROM BASIC_BLOCK WHERE MALWARE_ID = ? AND HASH = ?";
    PreparedStatement statement = conn.prepareStatement(sql);

    statement.setInt(1, malwareId);
    statement.setString(2, hash);

    ResultSet resultSet = statement.executeQuery();

    if (resultSet.next()) {
      result = new BasicBlock();

      result.setId(resultSet.getInt("ID"));
      result.setMalwareId(resultSet.getInt("MALWARE_ID"));
      result.setCount(resultSet.getInt("COUNT"));
      result.setHash(resultSet.getString("HASH"));
      result.setTermFrequency(resultSet.getDouble("TERM_FREQ"));
    }

    return result;
  }

  public List<BasicBlock> loadByMalware(Connection conn, Integer malwareId) throws SQLException {
    List<BasicBlock> result = new ArrayList<>();

    String sql = "SELECT * FROM BASIC_BLOCK WHERE MALWARE_ID = ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setInt(1, malwareId);

    ResultSet resultSet = statement.executeQuery();

    while (resultSet.next()) {
      BasicBlock basicBlock = new BasicBlock();

      basicBlock.setId(resultSet.getInt("ID"));
      basicBlock.setMalwareId(resultSet.getInt("MALWARE_ID"));
      basicBlock.setCount(resultSet.getInt("COUNT"));
      basicBlock.setHash(resultSet.getString("HASH"));
      basicBlock.setTermFrequency(resultSet.getDouble("TERM_FREQ"));

      result.add(basicBlock);
    }

    return result;
  }

  public long countByMalware(Connection conn, Integer malwareId) throws SQLException {
    long result = 0;

    String sql = "SELECT sum(`count`) AS C FROM BASIC_BLOCK WHERE MALWARE_ID = ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setInt(1, malwareId);

    ResultSet resultSet = statement.executeQuery();

    if (resultSet.next()) {
      result = resultSet.getLong("c");
    }

    return result;
  }

  public BasicBlock insert(Connection conn, Integer malwareId, String hash) throws SQLException {
    BasicBlock result = null;

    String sql = "INSERT INTO BASIC_BLOCK (MALWARE_ID, HASH, COUNT) VALUES (?, ?, ?)";
    PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

    statement.setInt(1, malwareId);
    statement.setString(2, hash);
    statement.setInt(3, 1);

    statement.executeUpdate();
    ResultSet resultSet = statement.getGeneratedKeys();

    if (resultSet.next()) {
      result = new BasicBlock();

      result.setId(resultSet.getInt(1));
      result.setHash(hash);
      result.setMalwareId(malwareId);
      result.setCount(1);
    }

    return result;
  }

  public BasicBlock update(Connection conn, final BasicBlock basicBlock) throws SQLException {
    String sql = "UPDATE BASIC_BLOCK SET COUNT = ?, TERM_FREQ = ? WHERE ID = ?";

    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setInt(1, basicBlock.getCount());
    statement.setDouble(2, basicBlock.getTermFrequency());
    statement.setInt(3, basicBlock.getId());

    statement.executeUpdate();

    return basicBlock;
  }

  public void deleteByMalware(Connection conn, Integer malwareId) throws SQLException {
    String sql = "DELETE FROM BASIC_BLOCK WHERE MALWARE_ID = ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setInt(1, malwareId);
    statement.executeUpdate();
  }
}
