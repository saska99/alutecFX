package com.example.uvozController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.alutecSpring.model.UlazniArtikal;
import com.example.alutecSpring.model.UvezeniArtikli;
import com.example.alutecSpring.model.Uvoz;
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

public class ArtikliUvozController {

    @FXML
    private ComboBox<String> uvozCombo;

    @FXML
    private ComboBox<String> artikalCombo;

    @FXML
    private TextField netoTF;

    @FXML
    private TextField komTF;
    
    @FXML
    private TextField naimTF;
    
    @FXML
    private Button buttonDodajArtikal;
    
    @FXML
    private Label labelPoruka;

    @FXML
    private void initialize() {
		popuniComboBoxUvoz();
		popuniComboBoxArtikal();
		
	}
    private void popuniComboBoxUvoz() {
		try {
			HttpClient httpClient = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/uvoz/sifre-uvoza"))
					.GET().build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				List<String> sifreUvozaList = new ObjectMapper().readValue(response.body(), new TypeReference<>() {	});

				uvozCombo.setItems(FXCollections.observableList(sifreUvozaList));
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
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/ulazniartikal/sifreArtikala"))
					.GET().build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				List<String> sifreArtiklaList = new ObjectMapper().readValue(response.body(), new TypeReference<>() {
				});

				// Postavljanje šifri u ComboBox
				artikalCombo.setItems(FXCollections.observableList(sifreArtiklaList));
			} else {
				// Handlujte greške ako je odgovor drugačiji od 200 OK
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
    private void dodajArtikalUUvoz() {
    	
        String sifraUvoza = uvozCombo.getValue();
        String sifraArtikla = artikalCombo.getValue();
        
        if (sifraUvoza == null || sifraArtikla == null || netoTF.getText().isEmpty() || komTF.getText().isEmpty() || naimTF.getText().isEmpty()) {
            //labelPoruka.setText("Popunite sva polja!");
        	prikaziAlert();
            return;
        }
        
        int naim = Integer.parseInt(naimTF.getText());
        double neto = Double.parseDouble(netoTF.getText());
        int kom = Integer.parseInt(komTF.getText());
        
        Uvoz uvoz = getUvozBySifra(sifraUvoza);
        
        UlazniArtikal artikal = getUlazniArtikalBySifra(sifraArtikla);
        if (uvoz != null && artikal != null) {
        	double cenaPoKom = artikal.getCenaPoKom();
        	
        	double ukupnaCena = cenaPoKom * kom;
        	
        	UvezeniArtikli uvezeniArtikal = new UvezeniArtikli(uvoz, artikal, naim, neto, kom, ukupnaCena);
        	posaljiNaBackend(uvezeniArtikal);
        } else {
            System.out.println("Nisu pronadjeni odgovarajuci podaci");
        }       
    }
    
 
    private Uvoz getUvozBySifra(String sifraUvoza) {
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/uvoz/findBySifraUvoza/" + sifraUvoza))
                    .GET().build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return new ObjectMapper().readValue(response.body(), Uvoz.class);
            } else {
                System.out.println("HTTP Error: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private UlazniArtikal getUlazniArtikalBySifra(String sifraArtikla) {
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/ulazniartikal/findBySifraArtikla/" + sifraArtikla))
                    .GET()
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

        return null; // Vratite null ili uradite nešto drugo ako ne možete dobiti objekat UlazniArtikal
    }
    
    public void posaljiNaBackend(UvezeniArtikli uvezeniArtikal) {
        String url = "http://localhost:8080/uvezeniartikli/dodaj"; // Prilagodi URL-u svojoj putanji
        //ovo sam dodala, to nije obavezno
        System.out.println("URL zahteva: " + url);
        System.out.println("Telo zahteva: " + uvezeniArtikal);
        
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<UvezeniArtikli> responseEntity = restTemplate.postForEntity(url, uvezeniArtikal, UvezeniArtikli.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            
            System.out.println("Dodali ste artikal u uvoz!");
            
            labelPoruka.setText("Artikal sa sifrom: "+ uvezeniArtikal.getUlazniArtikal().getSifraArtikla() +" dodat u uvoz!");
           
          
            naimTF.clear();
            netoTF.clear();
            komTF.clear();
            uvozCombo.getSelectionModel().clearSelection();
            artikalCombo.getSelectionModel().clearSelection();
            
        } else {
            // Neuspesno dodato, obradi gresku
            System.err.println("Greska prilikom dodavanja artikla u uvoz!");
            System.err.println("HTTP Status: " + responseEntity.getStatusCode());
            System.err.println("Odgovor servera: " + responseEntity);
            labelPoruka.setText("Artikal nije dodat u uvoz!");
        }
    }
}

