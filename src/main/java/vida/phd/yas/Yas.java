package vida.phd.yas;

import vida.phd.yas.commandline.CommandLine;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
import vida.phd.yas.family.FailedFile;
import vida.phd.yas.family.FamilyLoader;

public class Yas {

  private static final String version = "0.4.1";
  private CommandLine getter;
  private boolean running;

  public static void main(String[] args) {
    Yas yas = new Yas();
    yas.run();
  }

  public void run() {
    showVersion();
    checkDatabase();
    running = true;
    getter = new CommandLine(System.in, System.out, ";", "YAS> ", "-> ");
    while (running) {
      try {
        List<String> commands = getter.read();
        for (String command : commands) {
          if (command != null && command.trim().length() > 0) {
            command = command.trim();
            if (command.equalsIgnoreCase("exit")) {
              running = false;
              System.out.print("Bye\n");
            } else if (command.startsWith("family")) {
              familyCommand(command);
            } else if (command.equals("families")) {
              familiesCommand(command);
            } else if (command.equals("malwares")) {
              malwaresCommand(command);
            } else if (command.equalsIgnoreCase("time") || command.equalsIgnoreCase("date") || command.equalsIgnoreCase("now")) {
              timeCommand(command);
            } else if (command.equalsIgnoreCase("help")) {
              showYasHelp();
            } else if (command.startsWith("weight")) {
              scoreCommand(command);
            } else {
              System.out.println("Unknown Command!");
              System.out.println("");
              showYasHelp();
            }
          }
        }
      } catch (IOException e) {
        System.out.println("Error: " + e.getMessage());
      }
    }
  }

  private void familyCommand(String command) {
    if (command.equals("family")) {
      showFamilyHelp();
    } else {
      String[] parts = splitCommand(command);
      if (parts.length == 4 && parts[1].equals("add")) {
        addFamily(parts[2], parts[3]);
      } else if (parts.length == 3 && parts[1].equals("delete")) {
        deleteFamily(parts[2]);
      } else if (parts.length == 3 && parts[1].equals("show")) {
        showFamily(parts[2]);
      } else {
        showFamilyHelp();
      }
    }
  }

  private String[] splitCommand(String command) {
    return command.split("\\s+");
  }

  private void timeCommand(String command) {
    Calendar calendar = new GregorianCalendar();
    DateFormat format = DateFormat.getInstance();
    System.out.println(format.format(calendar.getTime()));
  }

  private void showYasHelp() {
    System.out.println("Available commands:");
    System.out.println("family");
    System.out.println("families");
    System.out.println("malwares");
    System.out.println("score");

  }

  private void showVersion() {
    System.out.println("Yas ".concat(version).concat("\n"));
  }

  private void showFamilyHelp() {
    System.out.println("Family command is not valid!");
    System.out.println("e.g.");
    System.out.println("family add Stuxnet c:\\family\\");
    System.out.println("family add Stuxnet c:\\family\\stuxnet_3647.txt");
    System.out.println("family add Stuxnet /home/family/");
    System.out.println("family delete Stuxnet");
    System.out.println("family show Stuxnet");
  }

  private boolean checkDatabase() {
    Connection connection = null;
    try {
      connection = Database.INSTANCE.getConnection();
      connection.setAutoCommit(false);

      String sql = "CREATE TABLE IF NOT EXISTS FAMILY (";
      sql = sql.concat("ID INTEGER PRIMARY KEY AUTOINCREMENT,");
      sql = sql.concat("NAME TEXT NOT NULL");
      sql = sql.concat(")");

      Statement stmnt = connection.createStatement();

      stmnt.executeUpdate(sql);

      sql = "CREATE TABLE IF NOT EXISTS MALWARE (";
      sql = sql.concat("ID INTEGER PRIMARY KEY AUTOINCREMENT,");
      sql = sql.concat("FAMILY_ID INTEGER NOT NULL,");
      sql = sql.concat("NAME TEXT NOT NULL");
      sql = sql.concat(")");

      stmnt.executeUpdate(sql);

      sql = "CREATE TABLE IF NOT EXISTS BASIC_BLOCK (";
      sql = sql.concat("ID INTEGER PRIMARY KEY AUTOINCREMENT,");
      sql = sql.concat("MALWARE_ID INTEGER NOT NULL,");
      sql = sql.concat("HASH TEXT NOT NULL,");
      sql = sql.concat("COUNT INTEGER,");
      sql = sql.concat("TERM_FREQ REAL,");
      sql = sql.concat("INV_DOC_FREQ REAL,");
      sql = sql.concat("WEIGHT REAL");
      sql = sql.concat(")");

      stmnt.executeUpdate(sql);

      connection.commit();

      return true;
    } catch (SQLException | ClassNotFoundException ex) {
      Logger.getLogger(Yas.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException ex) {
          Logger.getLogger(Yas.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
  }

  private void addFamily(String name, String path) {
    try (Connection conn = Database.INSTANCE.getConnection()) {
      conn.setAutoCommit(false);
      FamilyLoader familyLoader = new FamilyLoader(name, path);
      familyLoader.load(conn);
      System.out.println("Family added successfully.");
      System.out.println("Malwares: " + familyLoader.getCountOfMalwares());
      System.out.println("Basic blocks: " + familyLoader.getCountOfBasicBlocks());
      System.out.println("Skiped files: " + familyLoader.getFailedFiles().size());
      conn.commit();
      int i = 1;
      for (FailedFile failedFile : familyLoader.getFailedFiles()) {
        System.out.println(MessageFormat.format("{0}. {1}", i++, failedFile.getMessage()));
      }
    } catch (SQLException | ClassNotFoundException | IOException ex) {
      Logger.getLogger(Yas.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void showFamily(String familyName) {
    try (Connection conn = Database.INSTANCE.getConnection()) {
      FamilyDao familyDao = new FamilyDao();
      Family family = familyDao.load(conn, familyName);

      if (family != null) {
        MalwareDao malwareDao = new MalwareDao();
        List<Malware> malwares = malwareDao.loadByFamily(conn, family.getId());

        for (Malware malware : malwares) {
          System.out.println(malware.getName());
        }

      } else {
        System.out.println(MessageFormat.format("Family {0} not found!", familyName));
      }
    } catch (SQLException | ClassNotFoundException ex) {
      Logger.getLogger(Yas.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void familiesCommand(final String command) {
    try (Connection conn = Database.INSTANCE.getConnection()) {
      FamilyDao familyDao = new FamilyDao();
      List<Family> families = familyDao.loadAll(conn);

      System.out.println(MessageFormat.format("Count of families: {0}", families.size()));
      System.out.println("");

      int i = 1;
      for (Family family : families) {
        System.out.println(MessageFormat.format("{0}. {1}", i++, family.getName()));
      }
    } catch (SQLException | ClassNotFoundException ex) {
      Logger.getLogger(Yas.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void deleteFamily(String familyName) {
    try (Connection conn = Database.INSTANCE.getConnection()) {
      FamilyDao familyDao = new FamilyDao();
      Family family = familyDao.load(conn, familyName);

      if (family != null) {
        System.out.print(MessageFormat.format("Are you sure you want to DELETE the family {0}? (Y/N)", familyName));
        char ch = (char) System.in.read();
        System.in.read();

        if (ch == 'y' || ch == 'Y') {
          conn.setAutoCommit(false);

          MalwareDao malwareDao = new MalwareDao();
          BasicBlockDao basicBlockDao = new BasicBlockDao();

          List<Malware> malwares = malwareDao.loadByFamily(conn, family.getId());

          for (Malware malware : malwares) {
            basicBlockDao.deleteByMalware(conn, malware.getId());
          }

          malwareDao.deleteByFamily(conn, family.getId());
          familyDao.delete(conn, family.getId());

          conn.commit();
          System.out.println(MessageFormat.format("Family {0} has been deleted successfully!", familyName));
        }
      } else {
        System.out.println(MessageFormat.format("Family {0} NOT found!", familyName));
      }
    } catch (SQLException | ClassNotFoundException | IOException ex) {
      Logger.getLogger(Yas.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  
  private void scoreCommand(final String command) {
    if (command.equals("weight")) {
      showScoreHelp();
    } else {
      String[] parts = splitCommand(command);
      if (parts.length == 2 && parts[1].equals("update")) {
        updateScore(false);
      } else if (parts.length == 3 && parts[1].equals("update") && parts[2].equalsIgnoreCase("show")) {
        updateScore(true);
      } else {
        showScoreHelp();
      }
    }
  }

  private void showScoreHelp() {
    System.out.println("Score command is not valid!");
    System.out.println("e.g.");
    System.out.println("score update");
    System.out.println("score update show");
  }

  private void updateScore(boolean showBasicBlock) {
    updateTermFrequency(showBasicBlock);
    updateInverseDocumentFrequency(showBasicBlock);
    //updateWeight(showBasicBlock);
  }

  /*
  private void updateWeight(boolean showBasicBlock) {
    System.out.println("Updating WEIGHT ...");

    waitFor(500);

    try (Connection conn = Database.INSTANCE.getConnection()) {
      conn.setAutoCommit(false);
      FamilyDao familyDao = new FamilyDao();
      MalwareDao malwareDao = new MalwareDao();
      BasicBlockDao basicBlockDao = new BasicBlockDao();

      List<Family> families = familyDao.loadAll(conn);      
      for (Family family : families) {
        List<Malware> malwares = malwareDao.loadByFamily(conn, family.getId());

        System.out.println("Family: " + family.getName());
        waitFor(300);

        for (Malware malware : malwares) {
          System.out.println("\tMalware: " + malware.getName());
          waitFor(500);

          List<BasicBlock> basicBlocks = basicBlockDao.loadByMalware(conn, malware.getId());
          int countOfBasicBlocks = basicBlocks.size();
          int index = 0;
          int oldPercent = 0;
          for (BasicBlock basicBlock : basicBlocks) {
            index++;            

            double weight = basicBlock.getTermFrequency() * basicBlock.getInverseDocumentFrequency();
            basicBlock.setWeight(weight);
            basicBlockDao.update(conn, basicBlock);
            if (showBasicBlock) {
              DecimalFormat df = new DecimalFormat("#.#########");
              df.setDecimalSeparatorAlwaysShown(true);
              System.out.println("\t\t" + Utils.shortenHash(basicBlock.getHash()) + " " + df.format(weight));
            } // if 

            int percent = (int) ((index * 100.0f) / countOfBasicBlocks);
            if (percent > oldPercent) {
              oldPercent = percent;
              System.out.println(MessageFormat.format("\t\t% {0}", percent));
              waitFor(100);
            } // if
          } // for each
        } // for each
      }
      conn.commit();
    } catch (SQLException | ClassNotFoundException ex) {
      Logger.getLogger(Yas.class.getName()).log(Level.SEVERE, null, ex);
    }
  }*/

  private void updateInverseDocumentFrequency(boolean showBasicBlock) {
    System.out.println("Updating INVERSE DOCUMENT FREQUENCY ...");

    waitFor(500);

    try (Connection conn = Database.INSTANCE.getConnection()) {
      conn.setAutoCommit(false);
      FamilyDao familyDao = new FamilyDao();
      MalwareDao malwareDao = new MalwareDao();
      BasicBlockDao basicBlockDao = new BasicBlockDao();

      List<Family> families = familyDao.loadAll(conn);
      int countOfAllMalwares = malwareDao.loadAll(conn).size();
      for (Family family : families) {
        List<Malware> malwares = malwareDao.loadByFamily(conn, family.getId());

        System.out.println("Family: " + family.getName());
        waitFor(300);

        for (Malware malware : malwares) {
          System.out.println("\tMalware: " + malware.getName());
          waitFor(500);

          List<BasicBlock> basicBlocks = basicBlockDao.loadByMalware(conn, malware.getId());
          int countOfBasicBlocks = basicBlocks.size();
          int index = 0;
          int oldPercent = 0;
          for (BasicBlock basicBlock : basicBlocks) {
            index++;
            String hash = basicBlock.getHash();
            int countOfMalwares = basicBlockDao.getCountOfMalwaresHavingBasicBlock(conn, hash);

            double inverseDocumentFrequency = (double) countOfAllMalwares / countOfMalwares;
            double weight = inverseDocumentFrequency * basicBlock.getTermFrequency();
            basicBlock.setInverseDocumentFrequency(inverseDocumentFrequency);
            basicBlock.setWeight(weight);
            basicBlockDao.update(conn, basicBlock);
            if (showBasicBlock) {
              DecimalFormat df = new DecimalFormat("#.#########");
              df.setDecimalSeparatorAlwaysShown(true);
              System.out.println("\t\t" + Utils.shortenHash(basicBlock.getHash()) + " IDF: " + df.format(inverseDocumentFrequency) + " WEIGHT: " + df.format(weight));
            } // if 

            int percent = (int) ((index * 100.0f) / countOfBasicBlocks);
            if (percent > oldPercent) {
              oldPercent = percent;
              System.out.println(MessageFormat.format("\t\t% {0}", percent));
              waitFor(100);
            } // if
          } // for each
        } // for each
      }
      conn.commit();
    } catch (SQLException | ClassNotFoundException ex) {
      Logger.getLogger(Yas.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void updateTermFrequency(boolean showBasicBlock) {
    System.out.println("Updating TERM FREQUENCY ...");

    waitFor(500);

    try (Connection conn = Database.INSTANCE.getConnection()) {
      conn.setAutoCommit(false);
      FamilyDao familyDao = new FamilyDao();
      MalwareDao malwareDao = new MalwareDao();
      BasicBlockDao basicBlockDao = new BasicBlockDao();

      List<Family> families = familyDao.loadAll(conn);

      for (Family family : families) {
        List<Malware> malwares = malwareDao.loadByFamily(conn, family.getId());

        System.out.println("Family: " + family.getName());
        waitFor(300);

        for (Malware malware : malwares) {
          System.out.println("\tMalware: " + malware.getName());
          waitFor(500);

          long totalCountOfBasicBlocks = basicBlockDao.countByMalware(conn, malware.getId());

          List<BasicBlock> basicBlocks = basicBlockDao.loadByMalware(conn, malware.getId());
          for (BasicBlock basicBlock : basicBlocks) {
            double termFrequency = (double) basicBlock.getCount() / totalCountOfBasicBlocks;
            basicBlock.setTermFrequency(termFrequency);
            basicBlockDao.update(conn, basicBlock);
            if (showBasicBlock) {
              DecimalFormat df = new DecimalFormat("#.#########");
              df.setDecimalSeparatorAlwaysShown(true);
              System.out.println("\t\t" + Utils.shortenHash(basicBlock.getHash()) + " " + df.format(termFrequency));
            }
          }
        }
      }
      conn.commit();
    } catch (SQLException | ClassNotFoundException ex) {
      Logger.getLogger(Yas.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void waitFor(int millisec) {
    try {
      Thread.sleep(millisec);
    } catch (InterruptedException ex) {
      Logger.getLogger(Yas.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void malwaresCommand(String command) {
    try (Connection conn = Database.INSTANCE.getConnection()) {
      MalwareDao malwareDao = new MalwareDao();
      List<Malware> malwares = malwareDao.loadAll(conn);
      System.out.println("Malwares: " + malwares.size());
      System.out.println("");

      int index = 0;
      int count = 0;
      int perPage = 20;

      for (Malware malware : malwares) {
        if (count == perPage && index < malwares.size()) {
          if (!wannaContinue()) {
            break;
          }
          count = 0;
        }
        System.out.println(MessageFormat.format("{0}. {1}", ++index, malware.getName()));
        count++;
      }
      System.out.println("");
    } catch (SQLException | ClassNotFoundException ex) {
      Logger.getLogger(Yas.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private boolean wannaContinue() {
    try {
      System.out.print("Do you want to continue? (Y/N)");
      char ch = (char) System.in.read();
      System.in.read();
      return (ch == 'y' || ch == 'Y');
    } catch (IOException ex) {
      Logger.getLogger(Yas.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
  }
}
