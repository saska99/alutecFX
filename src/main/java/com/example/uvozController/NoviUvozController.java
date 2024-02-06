package com.example.uvozController;

import java.time.LocalDate;
import java.sql.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.alutecSpring.model.Uvoz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class NoviUvozController {

    @FXML
    private TextField sifraUvozaTF;

    @FXML
    private TextField postTF;

    @FXML
    private TextField zpTF;

    @FXML
    private DatePicker datumUvozaTF;
    
    @FXML
    private Label labelPoruka;
   
    private void prikaziAlert() {
    	Alert alert = new Alert(AlertType.WARNING);
    	alert.setTitle("Upozorenje");
    	alert.setHeaderText("Popunite sva polja");
    	alert.showAndWait();
    }
    
    @FXML
    private void saveUvoz(ActionEvent event) {     
    	String sifraUvoza = sifraUvozaTF.getText();
    	String post = postTF.getText();
    	String zp = zpTF.getText();
    	LocalDate datumUvoza = datumUvozaTF.getValue();
    	
    	boolean sifraPostoji = proveriSifruUvoza(sifraUvoza);
    	
    	if(sifraPostoji) {
    		labelPoruka.setText("Sifra uvoza vec postoji, unesite drugu sifru!");
    		sifraUvozaTF.clear();
    	}
    	
    	if (sifraUvoza.isEmpty() || post.isEmpty() || zp.isEmpty() || datumUvoza == null) {
    	    //labelPoruka.setText("Popunite sva polja!");
    		prikaziAlert();
    	    return;
    	}
    	
    	Uvoz uvoz = new Uvoz(sifraUvoza, post, zp, Date.valueOf(datumUvoza));
    	
        posaljiNaBackend(uvoz);
    	
    }
    
    private boolean proveriSifruUvoza(String sifraUvoza) {
        String url = "http://localhost:8080/uvoz/proveriSifruUvoza/" + sifraUvoza;

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, Boolean.class);
    }
    
    
    public void posaljiNaBackend(Uvoz uvoz) {
        String url = "http://localhost:8080/uvoz/dodaj"; 

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Uvoz> responseEntity = restTemplate.postForEntity(url, uvoz, Uvoz.class);

        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
            System.out.println("Uvoz uspesno dodat!");
            labelPoruka.setText("Uspesno dodat uvoz sa sifrom: "+ uvoz.getSifraUvoza());
            
            sifraUvozaTF.clear();
            postTF.clear();
            zpTF.clear();
            datumUvozaTF.setValue(null);
        } else {
            System.err.println("Greka prilikom dodavanja uvoza!");
            System.err.println("HTTP Status: " + responseEntity.getStatusCode());
            labelPoruka.setText("Greska prilikom dodavanja uvoza!");        
        }
    }


}
