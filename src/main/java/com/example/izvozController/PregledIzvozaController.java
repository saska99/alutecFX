package com.example.izvozController;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.example.alutecSpring.model.IzvezeniArtikli;
import com.example.alutecSpring.model.IzvozFaktura;
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

public class PregledIzvozaController {

    @FXML
    private ComboBox<String> izvozCombo;

    @FXML
    private Button prikaziBtn;

    @FXML
    private TableView<IzvezeniArtikli> izvozTable = new TableView<>(); //ovo sluzi da se tabela prikaze prilikom inicijalizacije
    
	@FXML
	private TableColumn<IzvezeniArtikli, Date> datumC;

	@FXML
	private TableColumn<IzvezeniArtikli, Integer> izvozC;

	@FXML
	private TableColumn<IzvezeniArtikli, String> artikalC;

	@FXML
	private TableColumn<IzvezeniArtikli, Double> netoC;

	@FXML
	private TableColumn<IzvezeniArtikli, Integer> komC;

	@FXML
	private TableColumn<IzvezeniArtikli, Double> cenaC;
	
	@FXML
    private Label labelPoruka;

    
    @FXML
	private void initialize() {
		popuniComboBox();
		
		izvozTable.setVisible(false); //prilikom inicijalizacije tabela bude nevidljiva
		
		//izvozC.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIzvozFaktura().getBrojFakture()));
		//datumC
		artikalC.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIzlazniArtikal().getSifraIzlaznogArtikla()));
	    netoC.setCellValueFactory(new PropertyValueFactory<>("neto"));
	    komC.setCellValueFactory(new PropertyValueFactory<>("kom"));
	    cenaC.setCellValueFactory(new PropertyValueFactory<>("ukupnaCena"));
		
    }
    
    private void popuniComboBox() {
		try {
			HttpClient httpClient = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/izvoz/sifreIzvoza"))
					.GET().build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
	            List<Integer> sifreIzvozaList = new ObjectMapper().readValue(response.body(), new TypeReference<>() {});

	            // tu konvertujemo listu integera u listu stringova
	            List<String> stringSifreIzvozaList = sifreIzvozaList.stream().map(Object::toString).collect(Collectors.toList());

	            izvozCombo.setItems(FXCollections.observableList(stringSifreIzvozaList));

			} else {
				System.out.println("HTTP Error: " + response.statusCode());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    
    @FXML
    private void prikaziTabelu(ActionEvent event) {
        String izabranaSifraIzvoza = izvozCombo.getValue();
        Integer izabranaSifraInt = Integer.parseInt(izabranaSifraIzvoza);
        System.out.println("Izabrana sifra izvoza: " + izabranaSifraIzvoza);

        if (izabranaSifraIzvoza != null) {
            try {
                IzvozFaktura izvoz = dohvatiIzvoz(izabranaSifraInt);
                
                if (izvoz != null) {
                    List<IzvezeniArtikli> izvezeniArtikli = dohvatiIzvezeniArtikli(izvoz.getBrojFakture());

                    if (izvezeniArtikli != null) {
                        ObservableList<IzvezeniArtikli> observableIzvezeniArtikli = FXCollections.observableArrayList(izvezeniArtikli);

                        labelPoruka.setText("Izabran je izvoz: " + izvoz.getBrojFakture() + ", datum: " + izvoz.getDatumIzvoza());
                        izvozCombo.getSelectionModel().clearSelection();

                        for (IzvezeniArtikli artikal : izvezeniArtikli) {
                            System.out.println("Izvezeni artikal: " + artikal);
                        }

                        izvozTable.setItems(observableIzvezeniArtikli);
                        izvozTable.setVisible(true); //i sad je vracamo da bude vidljiva
                    } else {
                        System.out.println("Nije bilo moguće dohvatiti IzvezeniArtikli.");
                    }
                } else {
                    System.out.println("Nije bilo moguće dohvatiti Izvoz.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Izaberite sifru izvoza!");
            //labelPoruka.setText("Izaberite sifru izvoza!");
            prikaziAlert();
        }
    }
    
    private void prikaziAlert() {
    	Alert alert = new Alert(AlertType.WARNING);
    	alert.setTitle("Upozorenje");
    	alert.setHeaderText("Nije izabrana sifra izvoza");
    	alert.showAndWait();
    }

    private IzvozFaktura dohvatiIzvoz(Integer sifraIzvoza) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest izvozRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/izvoz/findByBrojFakture/" + sifraIzvoza))
                .GET().build();

        HttpResponse<String> izvozResponse = httpClient.send(izvozRequest, HttpResponse.BodyHandlers.ofString());

        if (izvozResponse.statusCode() == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(izvozResponse.body(), IzvozFaktura.class);
        } else {
            System.out.println("HTTP Error for Uvoz: " + izvozResponse.statusCode());
            return null;
        }
    }

    private List<IzvezeniArtikli> dohvatiIzvezeniArtikli(Integer sifraIzvoza) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest izvezeniArtikliRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/izvezeniartikli/findByIzvozFaktura/" + sifraIzvoza))
                .GET().build();

        HttpResponse<String> izvezeniArtikliResponse = httpClient.send(izvezeniArtikliRequest, HttpResponse.BodyHandlers.ofString());

        if (izvezeniArtikliResponse.statusCode() == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(izvezeniArtikliResponse.body(), new TypeReference<>() {});
        } else {
            System.out.println("HTTP Error: " + izvezeniArtikliResponse.statusCode());
            return null;
        }
    }
}

