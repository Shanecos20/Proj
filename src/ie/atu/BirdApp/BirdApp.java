package ie.atu.BirdApp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import ie.atu.birdappmanager.Bird;
import ie.atu.birdappmanager.BirdManager;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BirdApp extends Application {
    private static final String CSV_IMPORT_PATH = "resources/input.csv";
    private static final String CSV_EXPORT_PATH = "resources/input.csv";

    private Label totalBirdsLabel;

    private BirdManager birdManager = BirdManager.getInstance();
    private TableView<Bird> birdTable;
    private TextField searchField;
    private ListView<Bird> collectedBirdsListView = new ListView<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setScene(createLoginForm(primaryStage));
        try {
            birdManager.importBirdsFromCSV(CSV_IMPORT_PATH);
            if (birdTable != null) {
                birdTable.setItems(FXCollections.observableArrayList(birdManager.getBirdList()));
            }
        } catch (IOException e) {
            showAlert("Import Error", "Failed to import from CSV: " + e.getMessage());
        }
        primaryStage.setTitle("Ireland's Birds: Management App");
        primaryStage.show();
    }

    private Scene createLoginForm(Stage primaryStage) {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));

        Text scenetitle = new Text("Welcome");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        gridPane.add(scenetitle, 0, 0, 2, 1);

        Label userName = new Label("User Name:");
        gridPane.add(userName, 0, 1);
        TextField userTextField = new TextField();
        gridPane.add(userTextField, 1, 1);

        Label pw = new Label("Password:");
        gridPane.add(pw, 0, 2);
        PasswordField pwBox = new PasswordField();
        gridPane.add(pwBox, 1, 2);

        Button btn = new Button("Sign in");
        btn.setOnAction(e -> handleLogin(primaryStage, userTextField, pwBox));
        gridPane.add(btn, 1, 4);

        Scene scene = new Scene(gridPane, 800, 600);
        scene.getStylesheets().add(new File("resources/styles.css").toURI().toString());
        return scene;
    }

    private void handleLogin(Stage primaryStage, TextField userTextField, PasswordField pwBox) {
        if (!userTextField.getText().trim().isEmpty() && !pwBox.getText().trim().isEmpty()) {
            primaryStage.setScene(createMainScene(primaryStage));
        } else {
            showAlert("Login Failed", "Please enter username and password.");
        }
    }

    private Scene createMainScene(Stage primaryStage) {
        BorderPane borderPane = new BorderPane();
        VBox inputArea = createInputArea(primaryStage);
        borderPane.setTop(inputArea);
        VBox filterArea = createFilterArea();
        borderPane.setBottom(filterArea);
        birdTable = createBirdTable();
        borderPane.setCenter(birdTable);
        Scene scene = new Scene(borderPane, 800, 600);
        scene.getStylesheets().add(new File("resources/styles.css").toURI().toString());
        return scene;
    }

    private void updateCollectedBirdsListView(List<Bird> collectorCollection) {
        // Clear the existing items in the ListView
        collectedBirdsListView.getItems().clear();

        // Add all birds from the collector's collection to the ListView
        collectedBirdsListView.getItems().addAll(collectorCollection);
    }

    private VBox createCollectorsPage(Stage primaryStage) {
        ImageView birdImageView = new ImageView();
        List<Bird> collectorCollection = new ArrayList<>();

        birdImageView.setFitHeight(200); // Set image view size
        birdImageView.setFitWidth(200);

        // Text fields for bird details
        TextField txtCommonName = new TextField();
        txtCommonName.setPromptText("Common Name");

        // Button to upload an image of a bird
        Button uploadImageButton = new Button("Upload Image");
        uploadImageButton.setOnAction(event -> uploadImage(birdImageView));

        // Button to add a bird to the collection
        Button addCollectedBirdButton = new Button("Add to My Collection");
        addCollectedBirdButton.setOnAction(event -> {
            String commonNameToSearch = txtCommonName.getText().trim();
            if (!commonNameToSearch.isEmpty()) {
                // Check if a bird with the common name exists in the main bird list
                Optional<Bird> birdToAdd = birdManager.getBirdList().stream()
                        .filter(bird -> bird.getCommonName().equalsIgnoreCase(commonNameToSearch))
                        .findFirst();

                if (birdToAdd.isPresent()) {
                    // Check if the bird is not already in the collector's collection
                    if (!collectorCollection.contains(birdToAdd.get())) {
                        Bird addedBird = birdToAdd.get();
                        collectorCollection.add(addedBird);

                        // Call the method to update the ListView
                        updateCollectedBirdsListView(collectorCollection);
                    } else {
                        showAlert("Duplicate Bird", "This bird is already in your collection.");
                    }
                } else {
                    showAlert("Bird Not Found", "No bird with the specified common name found.");
                }
            } else {
                showAlert("Empty Field", "Please enter a common name to add a bird to your collection.");
            }
        });

        // Button to return to the main page
        Button returnButton = new Button("Return to Main Page");
        returnButton.setOnAction(event -> {
            Scene mainScene = createMainScene(primaryStage);
            primaryStage.setScene(mainScene);
        });

        Button btnSaveCollection = new Button("Save Collection");
        btnSaveCollection.setOnAction(event -> {
            try {
                // Save the collector's collection
                birdManager.saveCollectorBirds(collectorCollection, "resources/input.ser");
                showAlert("Success", "Collection saved successfully.");
            } catch (IOException e) {
                showAlert("Error", "Failed to save collection: " + e.getMessage());
            }
        });

        Button btnLoadCollection = new Button("Load Collection");
        btnLoadCollection.setOnAction(event -> {
            try {
                List<Bird> collectorsBirds = birdManager.loadCollectorBirds("resources/input.ser");
                collectorCollection.clear(); // Clear the current collection
                collectorCollection.addAll(collectorsBirds); // Add the loaded birds to the collection
                collectedBirdsListView.setItems(FXCollections.observableArrayList(collectorsBirds)); // Update the ListView
                showAlert("Success", "Collection loaded successfully.");
            } catch (IOException | ClassNotFoundException e) {
                showAlert("Error", "Failed to load collection: " + e.getMessage());
            }
        });

        // Organize everything in a VBox
        VBox collectorsPage = new VBox(10, birdImageView, txtCommonName,
                uploadImageButton, addCollectedBirdButton, collectedBirdsListView,
                btnSaveCollection, btnLoadCollection, returnButton);
        collectorsPage.setPadding(new Insets(10));
        collectorsPage.getStylesheets().add(new File("resources/styles.css").toURI().toString());

        return collectorsPage;
    }

    private void uploadImage(ImageView birdImageView) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg",
                "*.jpeg");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            birdImageView.setImage(image);
        }
    }

    private void filterBirds(String family, String habitat) {
        List<Bird> filteredList;
        if (family == null && habitat == null) {
            filteredList = birdManager.getBirdList();
        } else {
            Stream<Bird> stream = birdManager.getBirdList().stream();
            if (family != null) {
                stream = stream.filter(b -> b.getFamily().equalsIgnoreCase(family));
            }
            if (habitat != null) {
                stream = stream.filter(b -> b.getHabitat().equalsIgnoreCase(habitat));
            }
            filteredList = stream.collect(Collectors.toList());
        }
        birdTable.setItems(FXCollections.observableArrayList(filteredList));
    }

    private void filterBirds() {
        String searchText = searchField.getText().toLowerCase();
        List<Bird> filteredList = birdManager.getBirdList().stream()
                .filter(bird -> bird.getScientificName().toLowerCase().contains(searchText) ||
                        bird.getCommonName().toLowerCase().contains(searchText) ||
                        bird.getFamily().toLowerCase().contains(searchText) ||
                        bird.getHabitat().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        birdTable.setItems(FXCollections.observableArrayList(filteredList));
    }

    private VBox createInputArea(Stage primaryStage) {
        TextField txtScientificName = new TextField();
        txtScientificName.setPromptText("Scientific Name");
        TextField txtCommonName = new TextField();
        txtCommonName.setPromptText("Common Name");
        TextField txtFamily = new TextField();
        txtFamily.setPromptText("Family");
        TextField txtHabitat = new TextField();
        txtHabitat.setPromptText("Habitat");

        Button showCollectorsPageButton = new Button("Collector's Page");
        showCollectorsPageButton.setOnAction(event -> {
            VBox collectorsPage = createCollectorsPage(primaryStage);
            Scene collectorScene = new Scene(collectorsPage, 800, 600);
            primaryStage.setScene(collectorScene);
        });

        Button btnAddBird = new Button("Add Bird");
        btnAddBird.setOnAction(event -> addBird(txtScientificName, txtCommonName, txtFamily, txtHabitat));

        Button btnRemoveBird = new Button("Remove Selected Bird");
        btnRemoveBird.setOnAction(event -> removeSelectedBird());

        Button btnImportCSV = new Button("Import from CSV");
        btnImportCSV.setOnAction(event -> {
            try {
                birdManager.importBirdsFromCSV(CSV_IMPORT_PATH);
                birdTable.setItems(FXCollections.observableArrayList(birdManager.getBirdList()));
            } catch (IOException e) {
                showAlert("Import Error", "Failed to import from CSV.");
            }
            totalBirdsLabel.setText("Total Birds: " + birdManager.getTotalBirdCount());
        });

        Button btnExportCSV = new Button("Export to CSV");
        btnExportCSV.setOnAction(event -> {
            try {
                birdManager.exportBirdsToCSV(CSV_EXPORT_PATH);
            } catch (IOException e) {
                showAlert("Export Error", "Failed to export to CSV.");
            }
        });

        searchField = new TextField();
        searchField.setPromptText("Search birds...");
        searchField.setOnKeyReleased(event -> filterBirds());

        totalBirdsLabel = new Label("Total Birds: " + birdManager.getTotalBirdCount());
        totalBirdsLabel.setFont(new Font("Arial", 16));

        VBox inputArea = new VBox(10, txtScientificName, txtCommonName, txtFamily, txtHabitat,
                btnAddBird, btnRemoveBird, btnImportCSV, btnExportCSV,
                searchField, totalBirdsLabel, showCollectorsPageButton);
        inputArea.setPadding(new Insets(10));
        return inputArea;
    }

    private VBox createFilterArea() {
        ComboBox<String> familyNameComboBox = new ComboBox<>();
        ComboBox<String> habitatComboBox = new ComboBox<>();
        try {
            Map<String, Set<String>> uniqueData = birdManager.getUniqueFamilyNamesAndHabitats(CSV_IMPORT_PATH);
            familyNameComboBox.setItems(FXCollections.observableArrayList(uniqueData.get("familyNames")));
            habitatComboBox.setItems(FXCollections.observableArrayList(uniqueData.get("habitats")));
        } catch (IOException e) {
            showAlert("Error", "Failed to load unique family names and habitats.");
        }
        Button btnFilter = new Button("Filter");
        btnFilter.setOnAction(event -> filterBirds(familyNameComboBox.getValue(), habitatComboBox.getValue()));

        Button btnClearFilter = new Button("Clear Filter");
        btnClearFilter.setOnAction(event -> {
            familyNameComboBox.setValue(null);
            habitatComboBox.setValue(null);
            filterBirds(null, null);
        });

        HBox filterBox = new HBox(10, new Label("Family:"), familyNameComboBox, new Label("Habitat:"), habitatComboBox,
                btnFilter, btnClearFilter);
        filterBox.setAlignment(Pos.CENTER);
        filterBox.setPadding(new Insets(10));

        VBox filterArea = new VBox(filterBox);
        return filterArea;
    }

    private void addBird(TextField txtScientificName, TextField txtCommonName, TextField txtFamily,
            TextField txtHabitat) {
        String scientificName = txtScientificName.getText();
        String commonName = txtCommonName.getText();
        String family = txtFamily.getText();
        String habitat = txtHabitat.getText();

        if (scientificName.isEmpty() || commonName.isEmpty() || family.isEmpty() || habitat.isEmpty()) {
            showAlert("Incomplete Data", "Please fill in all fields to add a bird.");
        } else {
            Bird newBird = new Bird(scientificName, commonName, family, habitat);
            birdManager.addBird(newBird);
            birdTable.getItems().add(newBird);
            txtScientificName.clear();
            txtCommonName.clear();
            txtFamily.clear();
            txtHabitat.clear();
        }
        totalBirdsLabel.setText("Total Birds: " + birdManager.getTotalBirdCount());
    }

    private void removeSelectedBird() {
        Bird selectedBird = birdTable.getSelectionModel().getSelectedItem();
        if (selectedBird != null) {
            birdManager.removeBird(selectedBird);
            birdTable.getItems().remove(selectedBird);
        } else {
            showAlert("No Bird Selected", "Please select a bird to remove.");
        }
        totalBirdsLabel.setText("Total Birds: " + birdManager.getTotalBirdCount());
    }

    private TableView<Bird> createBirdTable() {
        TableView<Bird> birdTable = new TableView<>();
        birdTable.setItems(FXCollections.observableArrayList(birdManager.getBirdList()));

        TableColumn<Bird, String> scientificNameCol = new TableColumn<>("Scientific Name");
        scientificNameCol.setCellValueFactory(new PropertyValueFactory<>("scientificName"));
        scientificNameCol.setPrefWidth(100);

        TableColumn<Bird, String> commonNameCol = new TableColumn<>("Common Name");
        commonNameCol.setCellValueFactory(new PropertyValueFactory<>("commonName"));

        TableColumn<Bird, String> familyCol = new TableColumn<>("Family");
        familyCol.setCellValueFactory(new PropertyValueFactory<>("family"));
        familyCol.setPrefWidth(50); // Example fixed width

        TableColumn<Bird, String> habitatCol = new TableColumn<>("Habitat");
        habitatCol.setCellValueFactory(new PropertyValueFactory<>("habitat"));

        birdTable.getColumns().addAll(scientificNameCol, commonNameCol, familyCol, habitatCol);

        birdTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Set resizing policy

        return birdTable;
    }

    // showAlert method - displays an alert box with a message
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

// End of the class
