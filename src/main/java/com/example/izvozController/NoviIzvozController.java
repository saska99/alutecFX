package com.example.izvozController;

import java.sql.Date;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.alutecSpring.model.IzvozFaktura;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class NoviIzvozController {


    @FXML
    private DatePicker datumIzvozaTF;

    @FXML
    private TextField sifraIzvozaTF;

    @FXML
    private Button sacuvajIzvozBtn;
    
    @FXML
    private Label labelPoruka;

    private void prikaziAlert() {
    	Alert alert = new Alert(AlertType.WARNING);
    	alert.setTitle("Upozorenje");
    	alert.setHeaderText("Popunite sva polja");
    	alert.showAndWait();
    }
    
    @FXML
    private void saveIzvoz() {
    	int sifraIzvoza = Integer.parseInt(sifraIzvozaTF.getText());
        LocalDate datumIzvoza = datumIzvozaTF.getValue();
        
        IzvozFaktura izvoz = new IzvozFaktura(sifraIzvoza, Date.valueOf(datumIzvoza));
        
      //validacija
    	if (sifraIzvozaTF.getText().isEmpty() || datumIzvoza == null) {
    	    //labelPoruka.setText("Popunite sva polja!");
    		prikaziAlert();
    	    return;
    	}
        
        System.out.println("Dodavanje novog izvoza");
        System.out.println("Sifa izvoza: " + sifraIzvoza);
        System.out.println("Datum izvoza: " + datumIzvoza);
        
        posaljiNaBackend(izvoz);
        
    }
    
    public void posaljiNaBackend(IzvozFaktura izvoz) {
        String url = "http://localhost:8080/izvoz/dodaj"; 

       
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<IzvozFaktura> responseEntity = restTemplate.postForEntity(url, izvoz, IzvozFaktura.class);

        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
            // Uspesno dodato
            System.out.println("Izvoz uspesno dodat!");
            labelPoruka.setText("Uspesno dodat izvoz sa sifrom: "+ izvoz.getBrojFakture());
            
          //da se obrisu vrednosti iz TF
            sifraIzvozaTF.clear();
            datumIzvozaTF.setValue(null);

        } else {
            // Neuspesno dodato, obradi gresku
            System.err.println("Greska prilikom dodavanja uvoza!");
            System.err.println("HTTP Status: " + responseEntity.getStatusCode());
            System.err.println("Odgovor servera: " + responseEntity.getBody());
            labelPoruka.setText("Greska pri dodavanju izvoza: " +izvoz.getBrojFakture());

        }
    }
}
