package ie.atu.birdappmanager;

import java.io.Serializable;

// Bird class - represents a bird with its details
public class Bird implements Serializable {
    private static final long serialVersionUID = 1L;
    // Variables to store bird details
    private String scientificName;
    private String family;
    private String commonName;
    private String habitat;

    // Constructor - sets up a new bird with all its details
    public Bird(String scientificName, String family, String commonName, String habitat) {
        this.scientificName = scientificName;
        this.family = family;
        this.commonName = commonName;
        this.habitat = habitat;
    }


    
    // Getters and Setters for each field
    // Getter for scientificName
    public String getScientificName() { return scientificName; }

    // Setter for scientificName
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }

    // Getter for family
    public String getFamily() { return family; }

    // Setter for family
    public void setFamily(String family) { this.family = family; }

    // Getter for commonName
    public String getCommonName() { return commonName; }

    // Setter for commonName
    public void setCommonName(String commonName) { this.commonName = commonName; }

    // Getter for habitat
    public String getHabitat() { return habitat; }

    // Setter for habitat
    public void setHabitat(String habitat) { this.habitat = habitat; }

    // toString method - makes it easy to print a Bird object
    @Override
    public String toString() {
        return "Bird{" +
                "scientificName='" + scientificName + '\'' +
                ", family='" + family + '\'' +
                ", commonName='" + commonName + '\'' +
                ", habitat='" + habitat + '\'' +
                '}';
    }
}
