package nfc;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.Label;

public class generateReport {
	
	private final VBox root;

	public generateReport() {
		root = new VBox();
		root.setAlignment(Pos.CENTER);
		root.setSpacing(24);
		
		//Title untuk generate report tu
		Label title = new Label("Generate Report");
		title.setStyle("-fx-text-fill: #222; -fx-font-weight: bold;");
		title.setTextAlignment(TextAlignment.CENTER);
        VBox.setMargin(title, new Insets(0, 0, 16, 0));
        
        // Card for buttons
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(20);
        card.setPadding(new Insets(40));
        card.setMaxWidth(320);
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

        root.getChildren().addAll(title, card);
    }
	
	
	public Node getRoot() {
        return root;
    }
}
