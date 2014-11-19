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
      result.setInverseDocumentFrequency(resultSet.getDouble("INV_DOC_FREQ"));
      result.setInverseDocumentFrequencyInFamily(resultSet.getDouble("INV_DOC_FREQ_FAM"));
      result.setWeight(resultSet.getDouble("WEIGHT"));
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
      basicBlock.setInverseDocumentFrequency(resultSet.getDouble("INV_DOC_FREQ"));
      basicBlock.setInverseDocumentFrequencyInFamily(resultSet.getDouble("INV_DOC_FREQ_FAM"));
      basicBlock.setWeight(resultSet.getDouble("WEIGHT"));
      basicBlock.setWeightInFam(resultSet.getDouble("WEIGHT_FAM"));

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
    String sql = "UPDATE BASIC_BLOCK SET COUNT = ?, TERM_FREQ = ?, ";
    sql = sql.concat("INV_DOC_FREQ = ?, WEIGHT = ?, INV_DOC_FREQ_FAM = ?, WEIGHT_FAM = ? WHERE ID = ?");

    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setInt(1, basicBlock.getCount());
    statement.setDouble(2, basicBlock.getTermFrequency());
    statement.setDouble(3, basicBlock.getInverseDocumentFrequency());
    statement.setDouble(4, basicBlock.getWeight());    
    statement.setDouble(5, basicBlock.getInverseDocumentFrequencyInFamily());
    statement.setDouble(6, basicBlock.getWeightInFam());
    statement.setInt(7, basicBlock.getId());

    statement.executeUpdate();

    return basicBlock;
  }

  public void deleteByMalware(Connection conn, Integer malwareId) throws SQLException {
    String sql = "DELETE FROM BASIC_BLOCK WHERE MALWARE_ID = ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setInt(1, malwareId);
    statement.executeUpdate();
  }

  public BasicBlock insert(Connection conn, BasicBlock basicBlock) throws SQLException {    
    String sql = "INSERT INTO BASIC_BLOCK (MALWARE_ID, HASH, COUNT) VALUES (?, ?, ?)";
    PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

    statement.setInt(1, basicBlock.getMalwareId());
    statement.setString(2, basicBlock.getHash());
    statement.setInt(3, basicBlock.getCount());

    statement.executeUpdate();
    ResultSet resultSet = statement.getGeneratedKeys();

    if (resultSet.next()) {      
      basicBlock.setId(resultSet.getInt(1));      
    }

    return basicBlock;
  }  

  public int getCountOfMalwaresHavingBasicBlock(Connection conn, String hash) throws SQLException {    
    String sql = "SELECT count(*) FROM malware m inner join basic_block b on (m.id = b.malware_id) where b.hash = ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    
    statement.setString(1, hash);
    ResultSet resultSet = statement.executeQuery();
    if (resultSet.next()) {
      return resultSet.getInt(1);
    } else {
      throw new SQLException("NO RESULT!!!");
    }
  }
  
  public int getCountOfMalwaresHavingBasicBlockInFamily(Connection conn, String hash, Integer familyId) throws SQLException {    
    String sql = "SELECT count(*) FROM malware m inner join basic_block b on (m.id = b.malware_id) inner join family f on (m.family_id = f.id) where b.hash = ? AND f.id = ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    
    statement.setString(1, hash);
    statement.setInt(2, familyId);
    
    ResultSet resultSet = statement.executeQuery();
    if (resultSet.next()) {
      return resultSet.getInt(1);
    } else {
      throw new SQLException("NO RESULT!!!");
    }
  }

  public List<BasicBlock> loadTopByWeight(Connection conn, int top) throws SQLException {
    List<BasicBlock> result = new ArrayList<>();
    
    String sql = "SELECT * FROM basic_block b ORDER BY weight DESC limit ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setInt(1, top);
    ResultSet resultSet = statement.executeQuery();
    
    while (resultSet.next()) {
      BasicBlock basicBlock = new BasicBlock();
      
      basicBlock.setId(resultSet.getInt("ID"));
      basicBlock.setMalwareId(resultSet.getInt("MALWARE_ID"));
      basicBlock.setCount(resultSet.getInt("COUNT"));
      basicBlock.setHash(resultSet.getString("HASH"));
      basicBlock.setTermFrequency(resultSet.getDouble("TERM_FREQ"));
      basicBlock.setInverseDocumentFrequency(resultSet.getDouble("INV_DOC_FREQ"));
      basicBlock.setInverseDocumentFrequencyInFamily(resultSet.getDouble("INV_DOC_FREQ_FAM"));
      basicBlock.setWeight(resultSet.getDouble("WEIGHT"));
      basicBlock.setWeightInFam(resultSet.getDouble("WEIGHT_FAM"));
      
      result.add(basicBlock);
    }
    
    return result;
  }

  public List<BasicBlock> loadTopInFamByWeight(Connection conn, int top, String familyName) throws SQLException {
    List<BasicBlock> result = new ArrayList<>();
    
    String sql = "SELECT b.* FROM basic_block b inner join malware m on (m.id = b.malware_id) inner join family f on (f.id = m.family_id) WHERE f.name = ? ORDER BY b.weight_fam DESC limit ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setInt(2, top);
    statement.setString(1, familyName);
    ResultSet resultSet = statement.executeQuery();
    
    while (resultSet.next()) {
      BasicBlock basicBlock = new BasicBlock();
      
      basicBlock.setId(resultSet.getInt("ID"));
      basicBlock.setMalwareId(resultSet.getInt("MALWARE_ID"));
      basicBlock.setCount(resultSet.getInt("COUNT"));
      basicBlock.setHash(resultSet.getString("HASH"));
      basicBlock.setTermFrequency(resultSet.getDouble("TERM_FREQ"));
      basicBlock.setInverseDocumentFrequency(resultSet.getDouble("INV_DOC_FREQ"));
      basicBlock.setInverseDocumentFrequencyInFamily(resultSet.getDouble("INV_DOC_FREQ_FAM"));
      basicBlock.setWeight(resultSet.getDouble("WEIGHT"));
      basicBlock.setWeightInFam(resultSet.getDouble("WEIGHT_FAM"));
      
      result.add(basicBlock);
    }
    
    return result;
  }
}
