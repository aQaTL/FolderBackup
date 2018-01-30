package com.aqatl.folderbackup;

import com.aqatl.folderbackup.archive.Archiver;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.sf.sevenzipjbinding.ArchiveFormat;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static net.sf.sevenzipjbinding.ArchiveFormat.SEVEN_ZIP;


/**
 * @author Maciej on 29.01.2018.
 */
public class MainView {
	@FXML
	private VBox rootPane;
	@FXML
	private TextField inputPath, outputPath;
	@FXML
	private Button inputSelectButton;
	@FXML
	private Button outputSelectButton;
	@FXML
	private Button archiveButton;
	@FXML
	private ProgressBar archivingProgressBar;

	private Stage stage;

	private DirectoryChooser directoryChooser;
	private FileChooser fileChooser;
	private File inputFile, outputFile;
	private Map<ArchiveFormat, FileChooser.ExtensionFilter> archiveExtensions;

	private Archiver archiver;
	private Task<Boolean> archiveTask;

	private Properties properties;

	public void init(Stage stage, Archiver archiver, Properties props) throws Exception {
		this.stage = stage;
		this.archiver = archiver;

		this.properties = props;

		fileChooser = new FileChooser();
		fileChooser.setTitle("Select file");
		directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Select folder");
		archiveExtensions = new TreeMap<>();

		List<ArchiveFormat> supportedFormats = archiver.getSupportedFormats();
		if (supportedFormats.size() == 0) {
			throw new Exception("No supported formats found");
		}

		supportedFormats.forEach(format -> archiveExtensions.put(
				format,
				new FileChooser.ExtensionFilter(
						format.getMethodName(),
						"*." + format.getMethodName().toLowerCase())));

		fileChooser.getExtensionFilters().setAll(archiveExtensions.values());
		fileChooser.setSelectedExtensionFilter(
				archiver.supportsFormat(SEVEN_ZIP) ?
						archiveExtensions.get(SEVEN_ZIP) :
						archiveExtensions.get(supportedFormats.get(0)));

		Image folderOpenIcon = new Image(
				Main.class.getResourceAsStream("res/folder_open.png"));
		inputSelectButton.setGraphic(new ImageView(folderOpenIcon));
		outputSelectButton.setGraphic(new ImageView(folderOpenIcon));

		inputPath.setText(properties.getProperty("last-input-file"));
		if (!inputPath.getText().equals("")) {
			inputFile = new File(inputPath.getText());
			updateOutputFile();
		}
		outputPath.setEditable(false);

		stage.setOnCloseRequest(event -> {
			if (archiveTask != null)
				archiveTask.cancel();
		});
	}

	@FXML
	private void selectInputFile(MouseEvent event) {
		inputFile = directoryChooser.showDialog(stage);
		if (inputFile != null) {
			inputPath.setText(inputFile.getAbsolutePath());
			updateOutputFile();
		}
	}

	private void updateOutputFile() {
		outputFile = new File(inputFile.getAbsolutePath() +
				"_backup_" +
				LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")) +
				"." + selectedArchiveFormat().getMethodName().toLowerCase());

		outputPath.setText(outputFile.getAbsolutePath());
	}

	@FXML
	private void selectOutputFile(MouseEvent event) {
		outputFile = fileChooser.showSaveDialog(stage);
		if (outputFile != null) {
			outputPath.setText(outputFile.getAbsolutePath());
		}
	}

	@FXML
	private void archive(MouseEvent event) {
		archiveTask = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				return archiver.archive(
						inputFile, outputFile,
						selectedArchiveFormat(),
						this::updateProgress);
			}
		};
		archiveTask.setOnSucceeded(workerStateEvent -> {
			archivingProgressBar.progressProperty().unbind();
			showArchivingCompletedAlert(archiveTask.getValue());
		});
		archiveTask.setOnFailed(workerStateEvent -> {
			archivingProgressBar.progressProperty().unbind();
			Main.throwExceptionAndExit((Exception) archiveTask.getException());
		});
		archiveTask.setOnCancelled(workerStateEvent ->
				archivingProgressBar.progressProperty().unbind());

		archivingProgressBar.progressProperty().bind(archiveTask.progressProperty());

		new Thread(archiveTask).start();
	}

	private void showArchivingCompletedAlert(boolean success) {
		Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
		infoAlert.setTitle("Archiving completed");

		if (success) {
			infoAlert.setHeaderText("Archiving completed successfully");
			properties.setProperty("last-input-file", inputPath.getText());
		} else {
			infoAlert.setHeaderText("Archiving failed");
		}

		infoAlert.showAndWait();
	}

	private ArchiveFormat selectedArchiveFormat() {
		return archiveExtensions.entrySet().stream().
				filter(entry ->
						entry.getValue() == fileChooser.getSelectedExtensionFilter()).
				findFirst().
				get().
				getKey();
	}

	public File getInputFile() {
		return inputFile;
	}

	public File getOutputFile() {
		return outputFile;
	}
}
