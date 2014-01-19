package vida.phd.yas.database.entity;

public class BasicBlock extends Entity {
  private String hash;
  private Integer malwareId;
  private Integer count;
  private double termFrequency;
  private double inverseDocumentFrequency;
  private double weight;

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }

  public double getInverseDocumentFrequency() {
    return inverseDocumentFrequency;
  }

  public void setInverseDocumentFrequency(double inverseDocumentFrequency) {
    this.inverseDocumentFrequency = inverseDocumentFrequency;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public Integer getMalwareId() {
    return malwareId;
  }

  public void setMalwareId(Integer malwareId) {
    this.malwareId = malwareId;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }  

  public double getTermFrequency() {
    return termFrequency;
  }

  public void setTermFrequency(double termFrequency) {
    this.termFrequency = termFrequency;
  }    
}
