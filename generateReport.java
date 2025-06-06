package nfc;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class generateReport {
	
	private final BorderPane root;

	public generateReport() {
	    // Header Bar (same as your code)
	    HBox headerBar = new HBox(18);
	    headerBar.setAlignment(Pos.CENTER);
	    headerBar.setPrefHeight(70);
	    headerBar.setMaxWidth(Double.MAX_VALUE);
	    headerBar.setStyle(
	        "-fx-background-color: linear-gradient(to bottom, #FFD600 90%, #FFC107 100%);" +
	        "-fx-border-color: #f4b400; -fx-border-width: 0 0 3 0;" +
	        "-fx-background-image: repeating-linear-gradient(to bottom, transparent, transparent 12px, #FECF4D 12px, #FECF4D 15px);"
	    );
	    ImageView honeyPot = new ImageView(new Image(getClass().getResource("/nfc/bee-buku.png").toExternalForm()));
	    honeyPot.setFitWidth(54);
	    honeyPot.setFitHeight(54);

	    Label headerTitle = new Label("Generate Report");
	    headerTitle.setStyle("-fx-font-family: Impact; -fx-font-size: 44px; -fx-font-weight: bold; -fx-text-fill: #181818;");
	    headerTitle.setAlignment(Pos.TOP_CENTER);

	    Region headerSpacer = new Region();
	    HBox.setHgrow(headerSpacer, Priority.ALWAYS);

	    headerBar.getChildren().addAll(honeyPot, headerTitle, headerSpacer);

	    // Card for buttons (same as your code)
	    VBox card = new VBox();
	    card.setAlignment(Pos.CENTER);
	    card.setSpacing(20);
	    card.setPadding(new Insets(40));
	    card.setMaxWidth(320);
	    card.setMaxHeight(200);

	    card.setStyle("-fx-background-color: #fff8dc; -fx-border-radius: 24; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box,rgba(100,80,10,0.06),12,0,0,8); -fx-border-color: #ffd966; -fx-border-width: 3;");

	    Button dailyBtn = new Button("Daily");
	    dailyBtn.setFont(Font.font("Poppins", 22));
	    dailyBtn.setPrefWidth(200);
	    dailyBtn.setPrefHeight(60);
	    dailyBtn.setStyle("-fx-background-color: #ffecb3; -fx-border-radius: 16; -fx-background-radius: 16; -fx-font-weight: bold; -fx-text-fill: #3e2723; -fx-border-color: #ffe082; -fx-border-width: 2;");
	    dailyBtn.setOnAction(e -> AdminDashboard.getInstance().showDailyReportView());

	    Button monthlyBtn = new Button("Monthly");
	    monthlyBtn.setFont(Font.font("Poppins", 22));
	    monthlyBtn.setPrefWidth(200);
	    monthlyBtn.setPrefHeight(60);
	    monthlyBtn.setStyle("-fx-background-color: #ffecb3; -fx-border-radius: 16; -fx-background-radius: 16; -fx-font-weight: bold; -fx-text-fill: #3e2723; -fx-border-color: #ffe082; -fx-border-width: 2;");
	    monthlyBtn.setOnAction(e -> AdminDashboard.getInstance().showMonthlyReportView());

	    card.getChildren().addAll(dailyBtn, monthlyBtn);

	    // --- Main layout as BorderPane
	    BorderPane root = new BorderPane();
	    root.setTop(headerBar);

	    // Card centered vertically and horizontally
	    StackPane centerPane = new StackPane(card);
	    root.setCenter(centerPane);

	  
	    this.root = root; //!
	}
	
	
	public Node getRoot() {
        return root;
    }
}
