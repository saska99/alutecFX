package com.example.izvozController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.alutecSpring.model.IzlazniArtikal;
import com.example.alutecSpring.model.IzvezeniArtikli;
import com.example.alutecSpring.model.IzvozFaktura;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class ArtikliIzvozController {

	@FXML
	private ComboBox<String> izvozCombo;

	@FXML
	private ComboBox<String> artikalCombo;

	@FXML
	private TextField netoTF;

	@FXML
	private TextField komTF;

	@FXML
	private Button dodajBtn;

	@FXML
	private Label labelPoruka;

	@FXML
	private void initialize() {
		popuniComboBoxIzvoz();
		popuniComboBoxArtikal();

	}

	private void popuniComboBoxIzvoz() {
		try {
			HttpClient httpClient = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/izvoz/sifreIzvoza"))
					.GET().build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				List<String> sifreIzvozaList = new ObjectMapper().readValue(response.body(), new TypeReference<>() {
				});

				
				izvozCombo.setItems(FXCollections.observableList(sifreIzvozaList));
			} else {
				
				System.out.println("HTTP Error: " + response.statusCode());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void popuniComboBoxArtikal() {
		try {
			HttpClient httpClient = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("http://localhost:8080/izlazniartikal/sifreArtikala")).GET().build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				List<String> sifreArtiklaList = new ObjectMapper().readValue(response.body(), new TypeReference<>() {
				});

				artikalCombo.setItems(FXCollections.observableList(sifreArtiklaList));

			} else {

				System.out.println("HTTP Error: " + response.statusCode());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void prikaziAlert() {
    	Alert alert = new Alert(AlertType.WARNING);
    	alert.setTitle("Upozorenje");
    	alert.setHeaderText("Popunite sva polja");
    	alert.showAndWait();
    }
	
	@FXML
	private void dodajArtikalUIzvoz() {
		Integer brojFakture = Integer.parseInt(izvozCombo.getValue());
		String sifraArtikla = artikalCombo.getValue();
		
		if (brojFakture == null || sifraArtikla == null || netoTF.getText().isEmpty() || komTF.getText().isEmpty()) {
	       // labelPoruka.setText("Popunite sva polja!");
			prikaziAlert();
	        return;
	    }
		
		double neto = Double.parseDouble(netoTF.getText());
		int kom = Integer.parseInt(komTF.getText());

		IzvozFaktura izvoz = getIzvozBySifra(brojFakture);

		IzlazniArtikal artikal = getIzlazniArtikalBySifra(sifraArtikla);

		if (izvoz != null && artikal != null) {

			double cenaPoKom = artikal.getSifraUlaznogArtikla().getCenaPoKom();
			double ukupnaCena = cenaPoKom * kom;

			IzvezeniArtikli izvezeniArtikal = new IzvezeniArtikli(izvoz, artikal, neto, kom, ukupnaCena);

			posaljiNaBackend(izvezeniArtikal);

		} else {
			System.out.println("Nisu pronadjeni odgovarajuci podaci");
		}

	}

	private IzvozFaktura getIzvozBySifra(Integer brojFakture) {
		try {
			HttpClient httpClient = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("http://localhost:8080/izvoz/findByBrojFakture/" + brojFakture)).GET().build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				return new ObjectMapper().readValue(response.body(), IzvozFaktura.class);
			} else {
				System.out.println("HTTP Error: " + response.statusCode());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null; // Vratite null ili uradite nešto drugo ako ne možete dobiti objekat Uvoz
	}

	private IzlazniArtikal getIzlazniArtikalBySifra(String sifraArtikla) {
		try {
			HttpClient httpClient = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("http://localhost:8080/izlazniartikal/findBySifraArtikla/" + sifraArtikla)).GET()
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				return new ObjectMapper().readValue(response.body(), IzlazniArtikal.class);
			} else {
				System.out.println("HTTP Error: " + response.statusCode());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void posaljiNaBackend(IzvezeniArtikli izvezeniArtikal) {
		String url = "http://localhost:8080/izvezeniartikli/dodaj";

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<IzvezeniArtikli> responseEntity = restTemplate.postForEntity(url, izvezeniArtikal,
				IzvezeniArtikli.class);

		if (responseEntity.getStatusCode().is2xxSuccessful()) {

			System.out.println("Dodali ste artikal u ivoz!");

			labelPoruka.setText("Artikal sa sifrom: " + izvezeniArtikal.getIzlazniArtikal().getSifraIzlaznogArtikla()
					+ " dodat u izvoz!");

			netoTF.clear();
			komTF.clear();
			izvozCombo.getSelectionModel().clearSelection();
			artikalCombo.getSelectionModel().clearSelection();

		} else {

			System.err.println("Greska prilikom dodavanja artikla u izvoz!");
			System.err.println("HTTP Status: " + responseEntity.getStatusCode());
			System.err.println("Odgovor servera: " + responseEntity);
			labelPoruka.setText("Artikal nije dodat u uvoz!");
		}
	}

}
