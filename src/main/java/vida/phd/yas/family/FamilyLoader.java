package vida.phd.yas.family;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import vida.phd.yas.database.BasicBlockDao;
import vida.phd.yas.database.Database;
import vida.phd.yas.database.FamilyDao;
import vida.phd.yas.database.MalwareDao;
import vida.phd.yas.database.entity.BasicBlock;
import vida.phd.yas.database.entity.Family;
import vida.phd.yas.database.entity.Malware;
import vida.phd.yas.malware.MalwareReader;

public class FamilyLoader {

  private File directory;
  private int countOfMalwares;
  private int countOfBasicBlocks;
  private final List<FailedFile> failedFiles = new ArrayList<>();
  String name;

  public FamilyLoader(String name, File directory) throws IOException {    
    this.directory = directory;
    this.name = name;
  }

  public FamilyLoader(String name, String directory) throws IOException {
    this(name, new File(directory));
  }

  public void load(Connection conn) throws SQLException, FileNotFoundException, IOException {

    File[] files = null;
    if (directory.isDirectory()) {
      files = directory.listFiles(new FileFilter() {

        @Override
        public boolean accept(File file) {
          return file.isFile() && file.getName().toLowerCase().endsWith(".txt");
        }
      });
    }

    FamilyDao familyDao = new FamilyDao();
    Family family = familyDao.load(conn, name);

    if (family == null) {
      family = familyDao.insert(conn, name);
    }

    if (directory.isDirectory()) {
      for (File file : files) {
        String oldFamily = exists(file.getName());
        if (oldFamily == null) {
          process(file, family, conn);
        } else {
          failedFiles.add(new FailedFile(file.getName(), MessageFormat.format("The file {0} has been already added to the family {1}", file.getName(), oldFamily)));
        }
      }
    } else { // add single file
      String oldFamily = exists(directory.getName());
      if (oldFamily == null) {
        process(directory, family, conn);
      } else {
        failedFiles.add(new FailedFile(directory.getName(), MessageFormat.format("The file {0} has been already added to the family {1}", directory.getName(), oldFamily)));
      }
    }
  }

  
  private void process(File file, Family family, Connection conn) throws FileNotFoundException, IOException, SQLException {
    String malwareName = file.getName();

    MalwareDao malwareDao = new MalwareDao();
    BasicBlockDao basicBlockDao = new BasicBlockDao();
    Malware malware = malwareDao.insert(conn, malwareName, family.getId());
    countOfMalwares++;
    
    MalwareReader malwareReader = new MalwareReader(file);
    List<BasicBlock> basicBlocks = malwareReader.read();
    
    System.out.println(MessageFormat.format("Saving {0} to the database ... % 1", malware.getName()));
    int size = basicBlocks.size();
    int index = 0;
    for (BasicBlock basicBlock : basicBlocks) {
      index++;
      basicBlock.setMalwareId(malware.getId());
      basicBlockDao.insert(conn, basicBlock);
      int percent = (int)((index * 100.0f) / size);
      if (percent > 1 && (percent % 5) == 0) {        
        System.out.println(MessageFormat.format("Saving {0} to the database ... % {1}", malware.getName(), percent));
      }
    }
    System.out.println("Saved to the database.");
    /*
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
    String line;
    BasicBlockDao basicBlockDao = new BasicBlockDao();
    while ((line = reader.readLine()) != null) {
    line = line.trim();
    if (line.length() > 0) {
    BasicBlock basicBlock = basicBlockDao.read(conn, malware.getId(), line);
    if (basicBlock != null) {
    basicBlock.setCount(basicBlock.getCount() + 1);
    basicBlockDao.update(conn, basicBlock);
    System.out.println(MessageFormat.format("Basic block added: {0} - frequency: {1}", Utils.shortenHash(basicBlock.getHash()), basicBlock.getCount()));
    } else {
    basicBlockDao.insert(conn, malware.getId(), line);
    System.out.println(MessageFormat.format("Basic block added: {0} - frequency: 1", Utils.shortenHash(line)));
    }
    countOfBasicBlocks++;
    }
    }
    }*/
  }

  public File getDirectory() {
    return directory;
  }

  public int getCountOfMalwares() {
    return countOfMalwares;
  }

  public int getCountOfBasicBlocks() {
    return countOfBasicBlocks;
  }

  public String getName() {
    return name;
  }

  private String exists(String filename) {
    String result = null;

    try (Connection conn = Database.INSTANCE.getConnection()) {
      MalwareDao malwareDao = new MalwareDao();
      Malware malware = malwareDao.load(conn, filename);
      if (malware != null) {
        FamilyDao familyDao = new FamilyDao();
        Family family = familyDao.load(conn, malware.getFamilyId());
        if (family != null) {
          result = family.getName();
        }
      }
    } catch (SQLException | ClassNotFoundException ex) {
      Logger.getLogger(FamilyLoader.class.getName()).log(Level.SEVERE, null, ex);
    }

    return result;
  }

  public List<FailedFile> getFailedFiles() {
    return failedFiles;
  }

}
