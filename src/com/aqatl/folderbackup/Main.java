package com.aqatl.folderbackup;

import com.aqatl.folderbackup.archive.Archiver;
import com.aqatl.folderbackup.archive.CompressionLevel;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.util.Properties;

/**
 * @author Maciej on 29.01.2018.
 */
public class Main extends Application {
	private Properties properties;

	@Override
	public void start(Stage primaryStage) {
		FXMLLoader loader = new FXMLLoader(Main.class.getResource("res/MainView.fxml"));
		Scene mainScene = null;

		try {
			mainScene = new Scene(loader.load());

			Archiver archiver = new Archiver();
			archiver.setCompressionLevel(CompressionLevel.DEFAULT);
			properties = loadProperties();
			loader.<MainView>getController().init(primaryStage, archiver, properties);

			EventHandler<WindowEvent> onCloseRequest = primaryStage.getOnCloseRequest();
			primaryStage.setOnCloseRequest(event -> {
				onCloseRequest.handle(event);
				onWindowsClose(event);
			});
		}
		catch (Exception e) {
			throwExceptionAndExit(e);
		}


		primaryStage.setTitle("Folder backup");
		primaryStage.setResizable(false);
		primaryStage.setScene(mainScene);
		primaryStage.show();
	}

	private void onWindowsClose(WindowEvent event) {
		saveProperties(properties);
		System.exit(0);
	}

	private Properties loadProperties() throws IOException {
		Properties properties = new Properties();

		properties.setProperty("last-input-file", "");

		File propertiesFile = new File("app.properties");
		if (propertiesFile.exists()) {
			properties.load(new FileInputStream(propertiesFile));
		}

		return properties;
	}

	private void saveProperties(Properties properties) {
		File propertiesFile = new File("app.properties");
		if (!propertiesFile.exists()) {
			try {
				propertiesFile.createNewFile();
			}
			catch (IOException e) {
				throwExceptionAndExit(e);
			}
		}

		try (FileOutputStream fout = new FileOutputStream(propertiesFile)) {
			properties.store(fout, null);
		}
		catch (IOException e) {
			throwExceptionAndExit(e);
		}
	}

	public static void throwExceptionAndExit(Exception e) {
		e.printStackTrace();

		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setResizable(true);
		alert.setTitle("Error");
		alert.setHeaderText("Application crashed");
		alert.setContentText(e.getMessage());

		StringWriter stackTrace = new StringWriter();
		e.printStackTrace(new PrintWriter(stackTrace));

		TextArea stackTraceArea = new TextArea(stackTrace.toString());
		stackTraceArea.setEditable(false);
		stackTraceArea.setWrapText(true);

		alert.getDialogPane().setExpandableContent(stackTraceArea);

		alert.showAndWait();
		System.exit(1);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
