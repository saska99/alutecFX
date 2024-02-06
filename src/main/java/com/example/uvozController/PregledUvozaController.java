package com.example.uvozController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;

import com.example.alutecSpring.model.UvezeniArtikli;
import com.example.alutecSpring.model.Uvoz;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@Controller
public class PregledUvozaController {

	@FXML
	private ComboBox<String> uvozCombo;

	@FXML
	private TableView<UvezeniArtikli> tableView = new TableView<>();

	@FXML
	private Button prikaziBtn;

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private TableColumn<UvezeniArtikli, Long> uvozC;

	@FXML
	private TableColumn<UvezeniArtikli, String> postC;

	@FXML
	private TableColumn<UvezeniArtikli, LocalDate> datumC;

	@FXML
	private TableColumn<UvezeniArtikli, Integer> naimC;

	@FXML
	private TableColumn<UvezeniArtikli, String> artikalC;

	@FXML
	private TableColumn<UvezeniArtikli, Double> netoC;

	@FXML
	private TableColumn<UvezeniArtikli, Integer> komC;

	@FXML
	private TableColumn<UvezeniArtikli, Double> cenaC;
	
	@FXML
    private Label labelPoruka;

	@FXML
	private void initialize() {
		popuniComboBox();
		
		tableView.setVisible(false);
		
        // postavimo podatke u tabelu
		//uvozC.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdUvoza().getSifraUvoza()));		
	    postC.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdUvoza().getPost()));
	    //datumC.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getIdUvoza().getDatumUvoza()));
	    naimC.setCellValueFactory(new PropertyValueFactory<>("naim"));
	    artikalC.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUlazniArtikal().getSifraArtikla()));
	    netoC.setCellValueFactory(new PropertyValueFactory<>("neto"));
	    komC.setCellValueFactory(new PropertyValueFactory<>("kom"));
	    cenaC.setCellValueFactory(new PropertyValueFactory<>("ukupnaCena"));
	}

	private void popuniComboBox() {
		try {
			HttpClient httpClient = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/uvoz/sifre-uvoza"))
					.GET().build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				List<String> sifreUvozaList = new ObjectMapper().readValue(response.body(), new TypeReference<>() {
				});

				uvozCombo.setItems(FXCollections.observableList(sifreUvozaList));
			} else {
				System.out.println("HTTP Error: " + response.statusCode());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void prikaziTabelu(ActionEvent event) {
	    String izabranaSifraUvoza = uvozCombo.getValue();
	    System.out.println("Izabrana sifra uvoza: " + izabranaSifraUvoza);

	    if (izabranaSifraUvoza != null) {
	        try {
	            // Prvo dobavite Uvoz objekat koji odgovara izabranoj sifri uvoza
	            HttpClient httpClient = HttpClient.newHttpClient();
	            HttpRequest uvozRequest = HttpRequest.newBuilder()
	                    .uri(URI.create("http://localhost:8080/uvoz/findBySifraUvoza/" + izabranaSifraUvoza))
	                    .GET().build();

	            HttpResponse<String> uvozResponse = httpClient.send(uvozRequest, HttpResponse.BodyHandlers.ofString());

	            if (uvozResponse.statusCode() == 200) {
	                ObjectMapper objectMapper = new ObjectMapper();
	                Uvoz uvoz = objectMapper.readValue(uvozResponse.body(), Uvoz.class);

	                // Zatim, koristite dobijeni Uvoz objekat za dohvatanje povezanih UvezeniArtikli
	                HttpRequest uvezeniArtikliRequest = HttpRequest.newBuilder()
	                        .uri(URI.create("http://localhost:8080/uvezeniartikli/findByUvoz/" + uvoz.getSifraUvoza()))
	                        .GET().build();

	                HttpResponse<String> uvezeniArtikliResponse = httpClient.send(uvezeniArtikliRequest, HttpResponse.BodyHandlers.ofString());

	                if (uvezeniArtikliResponse.statusCode() == 200) {

	                	List<UvezeniArtikli> uvezeniArtikli = objectMapper.readValue(uvezeniArtikliResponse.body(), new TypeReference<>() {});

	                    // Pomoću ObservableList možete dinamički ažurirati tabelu
	                    ObservableList<UvezeniArtikli> observableUvezeniArtikli = FXCollections.observableArrayList(uvezeniArtikli);
	                    
	                   
	                    labelPoruka.setText("Izabran je uvoz: "+ uvoz.getSifraUvoza() + ", datum: " + uvoz.getDatumUvoza());
	                    uvozCombo.getSelectionModel().clearSelection();
	                    
	                    for (UvezeniArtikli artikal : uvezeniArtikli) {
	                        System.out.println("Uvezeni artikal: " + artikal);
	                    }
	                    
	                    tableView.setItems(observableUvezeniArtikli);
	                    tableView.setVisible(true);
	                    
	                } else {
	                    System.out.println("HTTP Error for UvezeniArtikli: " + uvezeniArtikliResponse.statusCode());
	                }
	            } else {
	                System.out.println("HTTP Error for Uvoz: " + uvozResponse.statusCode());
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    } else {
	        System.out.println("Izaberite sifru uvoza!");
	        //labelPoruka.setText("Izaberite sifru uvoza!");
	        prikaziAlert();
	    }
	}
	
	private void prikaziAlert() {
    	Alert alert = new Alert(AlertType.WARNING);
    	alert.setTitle("Upozorenje");
    	//alert.setContentText("Nije izabrana sifra uvoza");
    	alert.setHeaderText("Nije izabrana sifra uvoza");
    	alert.showAndWait();
    }

}
