package application;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SampleController {
	
	/*_______________________________-FXML ATTRIBUTES-________________________________________________*/
	
	
	@FXML
	private TextField TF_compress;
	
	@FXML
	private JFXButton btn_browse;
	
	@FXML
	private JFXButton btn_stats;
	
	@FXML
	private JFXButton btn_decomp;
	
	@FXML
	private JFXButton btn_compress;

	@FXML
    private Rectangle loading;
	
    @FXML
    private JFXProgressBar PB;

    @FXML
    private Label lbl_load;

    @FXML
    private JFXButton btn_header;
	
	static /*_______________________________-NONFXML ATTRIBUTES-___________________________________________*/
	FileChooser fileChooser = new FileChooser();
	static long[][] Table = new long[256][3];
    static node root = null;
    static OutputStream outputStream = null;
	static int pointer = 0;
	static long headerSize = 0;
	static long bodySize = 0;
    static FileWriter fstream;
    static BufferedWriter out;
    static int binSize=0;
    static int boutSize=0;
    static File fileo = null;
    static int choose =0; //comp:1, decomp:0 
    /*________________________________-FXML METHODS-_________________________________________________*/
	
	
	@FXML
	void Browse(ActionEvent event) {
		File file = fileChooser.showOpenDialog( (Stage)((Node)event.getSource()).getScene().getWindow() );
		if (file != null) {
			TF_compress.setText(file.getAbsolutePath());
		}
	}
	
	@FXML
	void Compress(ActionEvent event) {
		
		final GetDailyEmpDataService service = new GetDailyEmpDataService();
		
		
			choose = 1;
	        fileo = new File(TF_compress.getText());
	 
	        PB.progressProperty().bind(service.progressProperty());
	        lbl_load.textProperty().bind(service.messageProperty());
	       // veil.visibleProperty().bind(service.runningProperty());
	        PB.visibleProperty().bind(service.runningProperty());
	       
	        service.start();
	        
	        
		
//		try {
//			Huff(new File(TF_compress.getText()));
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
	}
	
	@FXML
	void Decompress(ActionEvent event) throws InterruptedException {
		
		
		
		final GetDailyEmpDataService service = new GetDailyEmpDataService();
		choose = 0;
        fileo = new File(TF_compress.getText());
        lbl_load.textProperty().bind(service.messageProperty());
        PB.progressProperty().bind(service.progressProperty());
       // veil.visibleProperty().bind(service.runningProperty());
        PB.visibleProperty().bind(service.runningProperty());
       
        service.start();
       
		
//		PB.setProgress(0);
//		try {
//			DeHuff(new File(TF_compress.getText()));
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
	}
	
	
	@FXML
	void ShowStats(ActionEvent event) throws IOException {
		StackPane sp = new StackPane();
		TextArea ta = new TextArea();
		ta.setText(String.format("%15s  %15s  %15s  %15s \n","Char", "Code", "Length", "Frequency"));
		for (int i = 0; i < Table.length; i++) {
			ta.setText(ta.getText() + (String.format("%15c  %15s  %15d  %15d \n",(char)i, Integer.toBinaryString((int) Table[i][0]), Table[i][1], Table[i][2])));
		}
		sp.getChildren().add(ta);
		ta.setStyle("-fx-font-size: 24px;");
		Scene sc = new Scene(sp,600,800);
		Stage st = new Stage();
		st.setScene(sc);
		st.show();
		
		
	
	}
	
	   @FXML
	    void ShowHeader(ActionEvent event) throws IOException {
			String name = fileo.getName().substring(0, fileo.getName().indexOf('.')) + "header.huf";
			File head = new File(name);
			Desktop desktop = Desktop.getDesktop(); 
			desktop.open(head);
	    }
	   
	   
    /*______________________________-NONFXML METHODS-________________________________________________*/
	
	public void initialize() {
		// create a File chooser
					
		PB.setProgress(0);
		
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("All Files", "*.*"),
				new FileChooser.ExtensionFilter("Compressed Files", "*.huf")
				);
	}
	
	
}

