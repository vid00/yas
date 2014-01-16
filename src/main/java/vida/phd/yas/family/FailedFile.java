package vida.phd.yas.family;

public class FailedFile {

  private String filename;
  private String message;

  public FailedFile(String filename, String message) {
    this.filename = filename;
    this.message = message;
  }
  
  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
