package com.example.uvozController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import com.example.alutecSpring.model.IzlazniArtikal;
import com.example.alutecSpring.model.UlazniArtikal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

@Controller
public class ObradaArtikalaController {

	// @FXML
	// private TextField sifraUlaznogArtiklaTF;

	@FXML
	private ComboBox<String> ulazniArtikalCombo;

	@FXML
	private TextField sifraIzlaznogArtiklaTF;

	@FXML
	private Label labelPoruka;

	@FXML
	private void initialize() {
		popuniComboBox();
	}

	private void popuniComboBox() {
		try {
			HttpClient httpClient = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("http://localhost:8080/ulazniartikal/sifreArtikala")).GET().build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				List<String> sifreArtiklaList = new ObjectMapper().readValue(response.body(), new TypeReference<>() {
				});

				ulazniArtikalCombo.setItems(FXCollections.observableList(sifreArtiklaList));
			} else {

				System.out.println("HTTP Error: " + response.statusCode());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void obrada() {

		String sifraUlaznogArtikla = ulazniArtikalCombo.getValue();
		String sifraIzlaznogArtikla = sifraIzlaznogArtiklaTF.getText();

		if (sifraUlaznogArtikla == null || sifraIzlaznogArtikla.isEmpty()) {
			labelPoruka.setText("Popunite sva polja!");
			return;
		}

		System.out.println("Sifra ulaznog artikla: " + sifraUlaznogArtikla);

		UlazniArtikal ulazniArtikal = getUlazniArtikalBySifra(sifraUlaznogArtikla);

		if (ulazniArtikal == null) {
			System.out.println("ulazni artikal je null");
		}
		IzlazniArtikal izlazniArtikal = new IzlazniArtikal(sifraIzlaznogArtikla, ulazniArtikal);

		posaljiNaBackend(izlazniArtikal);
	}

	private UlazniArtikal getUlazniArtikalBySifra(String sifraArtikla) {
		try {

			HttpClient httpClient = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("http://localhost:8080/ulazniartikal/findBySifraArtikla/" + sifraArtikla)).GET()
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				return new ObjectMapper().readValue(response.body(), UlazniArtikal.class);
			} else {
				System.out.println("HTTP Error: " + response.statusCode());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void posaljiNaBackend(IzlazniArtikal izlazniArtikal) {

		String url = "http://localhost:8080/izlazniartikal/dodaj";

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<IzlazniArtikal> responseEntity = restTemplate.postForEntity(url, izlazniArtikal,
				IzlazniArtikal.class);

		if (responseEntity.getStatusCode().is2xxSuccessful()) {

			System.out.println("Obradili ste artikal!");

			labelPoruka
					.setText("Artikal sa sifrom: " + izlazniArtikal.getSifraIzlaznogArtikla() + " uspesno obradjen!");

			sifraIzlaznogArtiklaTF.clear();
			ulazniArtikalCombo.getSelectionModel().clearSelection();

		} else {
			System.err.println("Greska!");
			System.err.println("HTTP Status: " + responseEntity.getStatusCode());
			System.err.println(responseEntity);
			labelPoruka.setText("Artikal nije obradjen!");
		}
	}

}
