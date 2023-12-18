package ie.atu.birdappmanager;

// Importing necessary Java utilities
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// BirdManager class - handles all the bird-related stuff
public class BirdManager implements Serializable {
    // Unique ID for serialization
    private static final long serialVersionUID = 1L;
    
    // List to store all the birds
    private List<Bird> birdList;

    // Singleton instance of BirdManager
    private static BirdManager instance = new BirdManager();

    // Private constructor - because this is a singleton
    private BirdManager() {
        birdList = new ArrayList<>();
    }

    // getInstance method - get the singleton instance
    public static BirdManager getInstance() {
        return instance;
    }

    // addBird method - adds a new bird to the list
    public void addBird(Bird bird) {
        birdList.add(bird);
    }

    // removeBird method - removes a bird from the list
    public boolean removeBird(Bird bird) {
        return birdList.remove(bird);
    }

    // getBirdList method - returns the list of birds
    public List<Bird> getBirdList() {
        return birdList;
    }

    // saveBirds method - saves the birds to a file
    public void saveCollectorBirds(String filePath) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(birdList);
        }
    }
    public void saveCollectorBirds(List<Bird> collectorCollection, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(collectorCollection);
        }
    }
    
    // loadBirds method - loads birds from a file
    public List<Bird> loadCollectorBirds(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            return (List<Bird>) in.readObject();
        }
    }
    // importBirdsFromCSV method - imports birds from a CSV file
    public void importBirdsFromCSV(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true; // To skip the header line in CSV

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip the first line and continue to the next iteration
                    continue;
                }

                // Splits each line into bird attributes
                String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (values.length >= 4) {
                    // Create a new Bird object and add it to the list
                    Bird bird = new Bird(values[0].trim(), values[1].trim(), values[2].trim(), values[3].trim());
                    addBird(bird);
                } else {
                    // Here you could handle lines that don't have enough data
                }
            }
        }
    }


    // exportBirdsToCSV method - exports birds to a CSV file
    public void exportBirdsToCSV(String filePath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (Bird bird : getBirdList()) {
                // Creates a CSV line for each bird
                String line = String.join(",",
                        formatCsvField(bird.getScientificName()),
                        formatCsvField(bird.getFamily()),
                        formatCsvField(bird.getCommonName()),
                        formatCsvField(bird.getHabitat()));
                bw.write(line); // Writes the line to the file
                bw.newLine(); // Starts a new line for the next record
            }
        }
    }

    // formatCsvField method - formats a field to be CSV safe
    private String formatCsvField(String field) {
        return "\"" + field.replace("\"", "\"\"") + "\""; // Handle quotes in the field
    }

    // getUniqueFamilyNamesAndHabitats method - gets unique family names and habitats from CSV
    public Map<String, Set<String>> getUniqueFamilyNamesAndHabitats(String filePath) throws IOException {
        Set<String> uniqueFamilyNames = new HashSet<>(); // Stores unique family names
        Set<String> uniqueHabitats = new HashSet<>(); // Stores unique habitats

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Splits on commas outside quotes
                if (values.length >= 4) {
                    uniqueFamilyNames.add(values[1].trim()); // Family name is now the third column
                    uniqueHabitats.add(values[3].trim()); // Habitat is the fourth column
                }
            }
        }

        Map<String, Set<String>> result = new HashMap<>();
        result.put("familyNames", uniqueFamilyNames); // Adds family names to the result
        result.put("habitats", uniqueHabitats); // Adds habitats to the result
        return result;
    }

    // getTotalBirdCount method - returns the total count of birds
    public int getTotalBirdCount() {
        return birdList.size(); // Just returns the size of the bird list
    }
}
// End of BirdManager class
