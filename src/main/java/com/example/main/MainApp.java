package com.example.main;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class MainApp extends Application {
	
	private static Stage primaryStage;
	
	@FXML
	private BorderPane bp;
	
	@FXML
	private ImageView image;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		MainApp.primaryStage = primaryStage;
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Menu.fxml"));
		Parent root = loader.load();
		
		primaryStage.setTitle("Alutec");

	    primaryStage.setMaximized(true);
		
		Scene menuScene = new Scene(root);
		primaryStage.setScene(menuScene);
		
		
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(670);
		
		primaryStage.show();
	}
   
	public void nazadNaMenu() { 
		 try {
		        Parent menuRoot = FXMLLoader.load(getClass().getResource("/fxml/Menu.fxml"));
		        bp.setCenter(((BorderPane) menuRoot).getCenter()); 
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
	}
	
	public void exit(ActionEvent event) {
        Platform.exit();
    }
	
	private void ucitaj(String ime) {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource(ime + ".fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        bp.setCenter(root);
    }
	
	
	@FXML
    private void otvoriPregledUvoza(ActionEvent event) throws IOException {
		ucitaj("/uvoz/pregledUvoza");
    }

	@FXML
    private void otvoriNoviUvoz(ActionEvent event) throws IOException {
		ucitaj("/uvoz/noviUvoz");
    }
	@FXML
    private void otvoriNoviArtikal(ActionEvent event) throws IOException {
		ucitaj("/uvoz/noviArtikal");
    }

    @FXML
    private void otvoriArtikleUUvoz(ActionEvent event) throws IOException {
    	ucitaj("/uvoz/artikliUvoz");
    }

    @FXML
    private void otvoriObradaArtikala(ActionEvent event) throws IOException {
    	ucitaj("/uvoz/obradaArtikala");
    }
    @FXML
    private void otvoriMagacin(ActionEvent event) throws IOException {
    	ucitaj("/magacin/Magacin");
    }

    @FXML
    private void otvoriNoviIzvoz(ActionEvent event) throws IOException {
    	ucitaj("/izvoz/noviIzvoz");
    }
    @FXML
    private void otvoriArtikliIzvoz(ActionEvent event) throws IOException {
        ucitaj("/izvoz/artikliIzvoz");
    }

    @FXML
    private void otvoriPregledIzvoz(ActionEvent event) throws IOException {
        ucitaj("/izvoz/pregledIzvoza");
    }
	

	public static void main(String[] args) {
		launch(args);
	}
}

