package com.example.magacinController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


import com.example.alutecSpring.model.UlazniArtikal;
import com.example.alutecSpring.model.UvezeniArtikli;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class MagacinController {

    @FXML
    private ComboBox<String> artikalCombo;

    @FXML
    private Button prikaziBtn;
    
    @FXML
    private TableView<UvezeniArtikli> artikalTable = new TableView<>();

    @FXML
    private TableColumn<UvezeniArtikli, String> artikalC;

    @FXML
    private TableColumn<UvezeniArtikli, Double> netoC;

    @FXML
    private TableColumn<UvezeniArtikli, Integer> komC;

    @FXML
    private TableColumn<UvezeniArtikli, Double> cenaUkupnoC;

    
    @FXML
    private void initialize() {
		popuniComboBox();
		
		artikalTable.setVisible(false);
		
		artikalC.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUlazniArtikal().getSifraArtikla()));
	    netoC.setCellValueFactory(new PropertyValueFactory<>("neto"));
	    komC.setCellValueFactory(new PropertyValueFactory<>("kom"));
	    cenaUkupnoC.setCellValueFactory(new PropertyValueFactory<>("ukupnaCena"));
		
    }
    
    @FXML
    private void prikaziArtikal() {
        String izabraniArtikal = artikalCombo.getValue();
        System.out.println("Izabran je artikal: " + izabraniArtikal);

        if (izabraniArtikal != null) {
            try {
                UlazniArtikal ulazniArtikal = dohvatiUlazniArtikal(izabraniArtikal);
                System.out.println("dohvacen ulazni artikal!");
                if (ulazniArtikal != null) {
                    List<UvezeniArtikli> uvezeniArtikli = dohvatiUvezeniArtikli(ulazniArtikal.getSifraArtikla());

                    if (uvezeniArtikli != null) {
                        prikaziUvezeniArtikleNaTabeli(uvezeniArtikli);
                    } else {
                        System.out.println("Nije bilo moguce dohvatiti UvezeniArtikli.");
                    }
                } else {
                    System.out.println("Nije bilo moguce dohvatiti UlazniArtikal.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Izaberite sifru artikla!");
            prikaziAlert();
        }
    }

    private void prikaziAlert() {
    	Alert alert = new Alert(AlertType.WARNING);
    	alert.setTitle("Upozorenje");
    	alert.setHeaderText("Nije izabrana sifra artikla");
    	alert.showAndWait();
    }
    
    private UlazniArtikal dohvatiUlazniArtikal(String sifraArtikla) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/ulazniartikal/findBySifraArtikla/" + sifraArtikla))
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response.body(), UlazniArtikal.class);
        } else {
            System.out.println("HTTP Error u metodi dohvati ul art: " + response.statusCode());
            return null;
        }
    }

    private List<UvezeniArtikli> dohvatiUvezeniArtikli(String sifraArtikla) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest artRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/uvezeniartikli/findByUlazniArtikal/" + sifraArtikla))
                .GET().build();

        HttpResponse<String> artResponse = httpClient.send(artRequest, HttpResponse.BodyHandlers.ofString());

        if (artResponse.statusCode() == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(artResponse.body(), new TypeReference<>() {});
        } else {
            System.out.println("HTTP Error: " + artResponse.statusCode());
            return null;
        }
    }

    private void prikaziUvezeniArtikleNaTabeli(List<UvezeniArtikli> uvezeniArtikli) {
        ObservableList<UvezeniArtikli> observableUvezeniArtikli = FXCollections.observableArrayList(uvezeniArtikli);

        for (UvezeniArtikli artikal : uvezeniArtikli) {
            System.out.println("Uvezeni artikal: " + artikal);
        }

        artikalTable.setItems(observableUvezeniArtikli);
        artikalTable.setVisible(true);
        artikalCombo.getSelectionModel().clearSelection();
    }

    
    private void popuniComboBox() {
		try {
			HttpClient httpClient = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/uvezeniartikli/sifreUvezenihArtikala"))
					.GET().build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				List<String> sifreUvezenihArtiklaList = new ObjectMapper().readValue(response.body(), new TypeReference<>() {
				});

				artikalCombo.setItems(FXCollections.observableList(sifreUvezenihArtiklaList));
			} else {
				System.out.println("HTTP Error: " + response.statusCode());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
