package com.example.uvozController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.alutecSpring.model.UlazniArtikal;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class NoviArtikalController {

	
	@FXML
    private TextField sifraArtiklaTF;

    @FXML
    private TextField netoPoKomTF;

    @FXML
    private TextField cenaPoKomTF;
    
    @FXML
    private Label labelPoruka;
	
    private void prikaziAlert() {
    	Alert alert = new Alert(AlertType.WARNING);
    	alert.setTitle("Upozorenje");
    	alert.setHeaderText("Popunite sva polja");
    	alert.showAndWait();
    }
    
	@FXML
    private void saveArtikal(ActionEvent event) {
		
		if (sifraArtiklaTF.getText().isEmpty() || netoPoKomTF.getText().isEmpty() || cenaPoKomTF.getText().isEmpty()) {
    	    //labelPoruka.setText("Popunite sva polja!");
			prikaziAlert();
    	    return;
    	}
		
        String sifraArtikla = sifraArtiklaTF.getText();
        Double netoPoKom = Double.parseDouble(netoPoKomTF.getText());
        Double cenaPoKom = Double.parseDouble(cenaPoKomTF.getText());
        
        UlazniArtikal artikal = new UlazniArtikal(sifraArtikla, netoPoKom, cenaPoKom);
        
        System.out.println("Sifra artikla: " + sifraArtikla);
        System.out.println("Neto po kom: " + netoPoKom);
        System.out.println("Cena po kom: " + cenaPoKom);
     
        posaljiNaBackend(artikal);
    }
	
	public void posaljiNaBackend(UlazniArtikal artikal) {
        String url = "http://localhost:8080/ulazniartikal/dodaj";
        
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<UlazniArtikal> responseEntity = restTemplate.postForEntity(url, artikal, UlazniArtikal.class);

        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
            
            System.out.println("Artikal uspesno dodat!");
            labelPoruka.setText("Uspesno dodat artikal sa sifrom: "+ artikal.getSifraArtikla());
            
            sifraArtiklaTF.clear();
            netoPoKomTF.clear();
            cenaPoKomTF.clear();

        } else {
        	
            System.err.println("Greka prilikom dodavanja artikla!");
            System.err.println("HTTP Status: " + responseEntity.getStatusCode());
            System.err.println("Odgovor servera: " + responseEntity.getBody());
            labelPoruka.setText("Greska pri dodavanju artikla: "+ artikal.getSifraArtikla());

        }
    }
}
