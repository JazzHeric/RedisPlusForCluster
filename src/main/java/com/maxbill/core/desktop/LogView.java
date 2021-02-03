package com.maxbill.core.desktop;

import com.maxbill.tool.DateUtil;
import com.maxbill.tool.ItemUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.util.StringUtils;

import java.util.Date;

import static com.maxbill.tool.ItemUtil.DESKTOP_APP_LOGO;
import static com.maxbill.tool.ItemUtil.LOGVIEW_STYLE;

public class LogView {

    private static Stage logStage;

    private static TextArea logTextArea;

    public static void display() {
        logStage = new Stage();
        logStage.setTitle("RedisPlus");
        logStage.initStyle(StageStyle.TRANSPARENT);
        logStage.initModality(Modality.WINDOW_MODAL);
        logStage.setScene(new Scene(getLogMainBox(), 600, 600));
        logStage.getIcons().add(new Image(DESKTOP_APP_LOGO));
        logStage.setResizable(false);
        logStage.setFullScreen(false);
        logStage.setAlwaysOnTop(false);
        logStage.centerOnScreen();
    }

    /**
     * 日志窗口主体
     */
    private static BorderPane getLogMainBox() {
        BorderPane mainBox = new BorderPane();
        mainBox.setId("log-main-box");
        mainBox.getStylesheets().add(LOGVIEW_STYLE);
        mainBox.setTop(getLogTopsView());
        mainBox.setCenter(getLogBodyView());
        return mainBox;
    }

    /**
     * 日志顶部标题栏
     */
    private static GridPane getLogTopsView() {
        GridPane boxView = new GridPane();
        boxView.setId("log-tops-box");
        boxView.setHgap(10);
        Label boxTitle = new Label();
        Label boxImage = new Label();
        boxTitle.setText("日志信息");
        boxTitle.setTranslateX(-15);
        boxImage.setId("log-tops-box-image");
        boxTitle.setId("log-tops-box-title");
        boxImage.setPrefSize(27, 23);
        boxView.add(boxImage, 0, 0);
        boxView.add(boxTitle, 1, 0);
        boxView.setPadding(new Insets(5));
        boxView.setAlignment(Pos.CENTER_LEFT);
        boxImage.setGraphic(new ImageView(new Image(ItemUtil.DESKTOP_DAILY_IMAGE_NO)));
        GridPane.setHgrow(boxTitle, Priority.ALWAYS);
        return boxView;
    }


    /**
     * 日志内容窗体
     */
    private static TextArea getLogBodyView() {
        logTextArea = new TextArea();
        logTextArea.setId("log-body-box");
        logTextArea.setWrapText(true);
        logTextArea.setEditable(false);
        return logTextArea;
    }

    /**
     * 获取日志窗体
     */
    public static Stage getLogStage() {
        if (null == logStage) {
            display();
        }
        return logStage;
    }

    /**
     * 设置日志内容
     */
    public static void setLogView(boolean isInfo, String info) {
        if (StringUtils.isEmpty(info)) {
            return;
        }
        if (null == logStage) {
            display();
        }
        Platform.runLater(() -> {
            logTextArea.appendText(">>>>>>>>>>" + DateUtil.formatDate(new Date(), DateUtil.DATE_STR_DETAIL) + "\r\n");
        });
        if (isInfo) {
            Platform.runLater(() -> {
                logTextArea.appendText(info + "\r\n");
                logTextArea.appendText("\r\n\r\n");
            });
        } else {
            Platform.runLater(() -> {
                logTextArea.appendText(info);
                logTextArea.appendText("\r\n\r\n");
            });
        }
    }

}