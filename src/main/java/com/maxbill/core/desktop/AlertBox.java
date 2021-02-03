package com.maxbill.core.desktop;

import com.maxbill.MainApplication;
import com.maxbill.base.controller.OtherController;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import netscape.javascript.JSObject;

import static com.maxbill.tool.ItemUtil.*;

public class AlertBox {

    public void display(String title) {
        Stage window = new Stage();
        window.setTitle("RedisPlus");
        window.initStyle(StageStyle.TRANSPARENT);
        window.initModality(Modality.APPLICATION_MODAL);
        window.setScene(new Scene(getMainBox(window, title), 600, 600));
        window.getIcons().add(new Image(DESKTOP_APP_LOGO));
        window.setResizable(false);
        window.setFullScreen(false);
        window.setAlwaysOnTop(false);
        window.centerOnScreen();
        window.show();
    }

    /**
     * 窗口主体
     */
    private BorderPane getMainBox(Stage winStage, String title) {
        BorderPane mainBox = new BorderPane();
        mainBox.setId("main-box");
        mainBox.getStylesheets().add(ALERTBOX_STYLE);
        String imgSrc = "";
        String htmlSrc = "";
        switch (title) {
            case "设置":
                imgSrc = TOP_MENU_SETTING;
                htmlSrc = ALERT_BOX_SETTING;
                break;
            case "反馈":
                imgSrc = TOP_MENU_ADVICE;
                htmlSrc = ALERT_BOX_ADVICE;
                break;
            case "帮助":
                imgSrc = TOP_MENU_HELP;
                htmlSrc = ALERT_BOX_HELP;
                break;
            case "版本":
                imgSrc = TOP_MENU_VERSION;
                htmlSrc = ALERT_BOX_VERSION;
                break;
            case "关于":
                imgSrc = TOP_MENU_ABOUT;
                htmlSrc = ALERT_BOX_ABOUT;
                break;
        }
        mainBox.setTop(getTopsView(winStage, title, imgSrc));
        mainBox.setCenter(getBodyView(title, htmlSrc));
        return mainBox;
    }

    /**
     * 顶部标题栏
     */
    private GridPane getTopsView(Stage winStage, String title, String imgSrc) {
        GridPane boxView = new GridPane();
        boxView.setId("tops-box");
        boxView.setHgap(10);
        Label boxTitle = new Label();
        Label boxImage = new Label();
        Label boxClose = new Label();
        boxTitle.setText(title);
        boxTitle.setTranslateX(-15);
        boxImage.setId("tops-box-image");
        boxTitle.setId("tops-box-title");
        boxClose.setId("tops-box-close");
        boxImage.setPrefSize(27, 23);
        boxClose.setPrefSize(27, 23);
        boxView.add(boxImage, 0, 0);
        boxView.add(boxTitle, 1, 0);
        boxView.add(boxClose, 2, 0);
        boxView.setPadding(new Insets(5));
        boxView.setAlignment(Pos.CENTER_LEFT);
        boxImage.setGraphic(new ImageView(new Image(imgSrc)));
        GridPane.setHgrow(boxTitle, Priority.ALWAYS);
        boxClose.setOnMouseClicked(event -> winStage.close());
        return boxView;
    }


    /**
     * 内容窗体
     */
    private VBox getBodyView(String title, String htmlSrc) {
        WebView webView = new WebView();
        webView.setCache(true);
        webView.setContextMenuEnabled(false);
        webView.setFontSmoothingType(FontSmoothingType.GRAY);
        WebEngine webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        String url = Desktop.class.getResource(htmlSrc).toExternalForm();
        webEngine.load(url);
        ReadOnlyObjectProperty<Worker.State> woker = webEngine.getLoadWorker().stateProperty();
        woker.addListener((obs, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject jsObject = (JSObject) webEngine.executeScript("window");
                OtherController otherController = MainApplication.context.getBean(OtherController.class);
                jsObject.setMember("otherRouter", otherController);
            }
        });
        return new VBox(webView);
    }


}