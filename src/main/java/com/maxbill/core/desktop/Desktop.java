package com.maxbill.core.desktop;

import com.maxbill.MainApplication;
import com.maxbill.base.controller.*;
import com.maxbill.tool.DataUtil;
import com.maxbill.tool.FileUtil;
import com.sun.javafx.webkit.WebConsoleListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import netscape.javascript.JSObject;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import java.io.File;

import static com.maxbill.tool.ItemUtil.*;


public class Desktop extends Application {

    private double x = 0.00;
    private double y = 0.00;

    private double xOffset = 0;
    private double yOffset = 0;

    private double width = 0.00;
    private double height = 0.00;

    private double resizeWidth = 5.00;
    private double minWidth = 1000.00;
    private double minHeight = 600.00;

    //是否处于右边界调整窗口状态
    private boolean isRight;
    //是否处于下边界调整窗口状态
    private boolean isBottom;
    //是否处于右下角调整窗口状态
    private boolean isBottomRight;
    //是否处于最大化调整窗口状态
    private boolean isMax = false;

    private static Stage stage;
    private static WebView webView;
    private static GridPane topsView;
    private static WebEngine webEngine;
    private static BorderPane mainView;

    //上下文对象
    public static ConfigurableApplicationContext context = null;

    //注入的JS对象
    private static ConfController confController = null;
    private static OtherController otherController = null;
    private static ConnectController connectController = null;
    private static DataSinglesController dataSinglesController = null;
    private static DataClusterController dataClusterController = null;
    private static InfoSinglesController infoSinglesController = null;
    private static InfoClusterController infoClusterController = null;


    @Override
    public void start(Stage winStage) {

        //设置窗口信息
        winStage.centerOnScreen();
        winStage.setTitle(DESKTOP_APP_NAME);
        winStage.setAlwaysOnTop(false);
        winStage.initStyle(StageStyle.TRANSPARENT);
        winStage.getIcons().add(new Image(DESKTOP_APP_LOGO));

        //启动扫描服务
        context = SpringApplication.run(MainApplication.class);
        if (null != context) {
            initWebObject();
            otherController.initSystems();
        } else {
            return;
        }

        //加载数据窗口
        mainView = getMainView(winStage);
        winStage.setScene(new Scene(mainView, minWidth, minHeight));
        winStage.show();

        //监听窗口事件
        doWinStage(winStage);
        doWinRaise(winStage);
        doWinState(winStage, mainView);

        stage = winStage;
    }


    /**
     * 窗口主体
     */
    private BorderPane getMainView(Stage winStage) {
        BorderPane mainView = new BorderPane();
        mainView.setId("main-view");
        mainView.getStylesheets().add(DESKTOP_STYLE);
        mainView.setTop(getTopsView(winStage));
        mainView.setCenter(getBodyView());
        mainView.setBottom(getEndsView());
        return mainView;
    }


    /**
     * 顶部标题栏
     */
    private GridPane getTopsView(Stage winStage) {

        topsView = new GridPane();
        topsView.setId("tops-view");
        topsView.setHgap(10);

        Label topImage = new Label();
        Label topTitle = new Label();
        Label topItems = new Label();
        Label topAbate = new Label();
        Label topRaise = new Label();
        Label topClose = new Label();

        topTitle.setText(DESKTOP_APP_NAME);
        topImage.setId("tops-view-image");
        topTitle.setId("tops-view-title");
        topItems.setId("tops-view-items");
        topAbate.setId("tops-view-abate");
        topRaise.setId("tops-view-raise");
        topClose.setId("tops-view-close");

        topImage.setPrefSize(27, 23);
        topItems.setPrefSize(27, 23);
        topAbate.setPrefSize(27, 23);
        topRaise.setPrefSize(27, 23);
        topClose.setPrefSize(27, 23);

        topsView.add(topImage, 0, 0);
        topsView.add(topTitle, 1, 0);
        topsView.add(topItems, 2, 0);
        topsView.add(topAbate, 3, 0);
        topsView.add(topRaise, 4, 0);
        topsView.add(topClose, 5, 0);

        topsView.setPadding(new Insets(5));
        topsView.setAlignment(Pos.CENTER_LEFT);
        GridPane.setHgrow(topTitle, Priority.ALWAYS);
        String themeColor = otherController.getSetting(SETTING_THEME_COLOR);
        Color backgroundColor = Color.web(themeColor, 1.0);
        BackgroundFill backgroundFill = new BackgroundFill(backgroundColor, null, null);
        topsView.setBackground(new Background(backgroundFill));
        //事件监听
        //1.监听操作选项事件
        topItems.setOnMouseClicked(event -> doWinItems(topItems));
        //2.监听窗口最小事件
        topAbate.setOnMouseClicked(event -> doWinAbate(winStage));
        //3.监听窗口最大事件
        topRaise.setOnMouseClicked(event -> doWinRaise(winStage));
        //4.监听窗口关闭事件
        topClose.setOnMouseClicked(event -> doWinClose(winStage));

        return topsView;
    }


    /**
     * 内容窗体
     */
    private WebView getBodyView() {

        webView = new WebView();
        webView.setCache(false);
        webEngine = webView.getEngine();
        webView.setContextMenuEnabled(true);
        webEngine.setJavaScriptEnabled(true);
        webView.setFontSmoothingType(FontSmoothingType.GRAY);

        //设置加载的主页
        String rootPagePath = "";
        String connectTheme = otherController.getSetting(SETTING_CONNECT_THEME);
        if (!StringUtils.isEmpty(connectTheme) && connectTheme.equals("1")) {
            rootPagePath = PAGE_CONNECT_QUICKER;
        } else {
            rootPagePath = PAGE_CONNECT_DEFAULT;
        }
        webEngine.load(Desktop.class.getResource(rootPagePath).toExternalForm());

        //设置数据目录
        String basePath = System.getProperty("user.home");
        String dataPath = basePath + "/.redis_plus/temp";
        FileUtil.existsFile(dataPath);
        webEngine.setUserDataDirectory(new File(dataPath));

        //监听事件
        Worker<Void> woker = webEngine.getLoadWorker();
        woker.stateProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject jsObject = (JSObject) webEngine.executeScript("window");
                jsObject.setMember("confRouter", confController);
                jsObject.setMember("otherRouter", otherController);
                jsObject.setMember("connectRouter", connectController);
                jsObject.setMember("dataSinglesRouter", dataSinglesController);
                jsObject.setMember("dataClusterRouter", dataClusterController);
                jsObject.setMember("infoSinglesRouter", infoSinglesController);
                jsObject.setMember("infoClusterRouter", infoClusterController);
            }
        });

        //页面异常事件
        woker.exceptionProperty().addListener((ObservableValue<? extends Throwable> ov, Throwable t0, Throwable t1) -> {
            System.out.println("Received Exception: " + t1.getMessage());
        });

        //控制台监听事件
        WebConsoleListener.setDefaultListener((WebView webView, String message, int lineNumber, String sourceId) -> {
            System.out.println("Console: [" + sourceId + ":" + lineNumber + "] " + message);
        });

        return webView;
    }


    /**
     * 底部窗体
     */
    private GridPane getEndsView() {

        GridPane endsView = new GridPane();
        endsView.setId("ends-view");
        endsView.setHgap(10);

        Label endImage = new Label();
        Label endTitle = new Label();
        Label endOther = new Label();
        Label endOrder = new Label();
        Label endDaily = new Label();

        endTitle.setMinWidth(200.00);
        endOrder.setMinWidth(150.00);
        endTitle.setText(DESKTOP_STATUS_NO);
        endOrder.setText(DESKTOP_VERSION);

        endImage.setId("ends-view-image");
        endTitle.setId("ends-view-title");
        endOther.setId("ends-view-other");
        endOrder.setId("ends-view-order");
        endDaily.setId("ends-view-daily");

        endImage.setPrefSize(27, 23);
        endTitle.setPrefSize(27, 23);
        endOther.setPrefSize(27, 23);
        endOrder.setPrefSize(27, 23);
        endDaily.setPrefSize(27, 23);

        endsView.add(endImage, 0, 0);
        endsView.add(endTitle, 1, 0);
        endsView.add(endOther, 2, 0);
        endsView.add(endOrder, 3, 0);
        endsView.add(endDaily, 4, 0);

        endsView.setPadding(new Insets(3));
        endsView.setAlignment(Pos.BASELINE_RIGHT);
        endTitle.setTextFill(Paint.valueOf("red"));
        endOrder.setTextFill(Paint.valueOf("#1766A2"));
        endOrder.setAlignment(Pos.CENTER_RIGHT);
        endImage.setGraphic(new ImageView(new Image(DESKTOP_STATUS_IMAGE_NO)));
        endDaily.setGraphic(new ImageView(new Image(DESKTOP_DAILY_IMAGE_NO)));
        GridPane.setHgrow(endTitle, Priority.ALWAYS);

        //处理状态栏工具监听事件
        endDaily.setOnMouseClicked(event -> {
            Boolean isLogOn = DataUtil.getOutLogWindowStatus();
            DataUtil.setOutLogWindowStatus(!isLogOn);
            if (isLogOn) {
                //隐藏日志窗口
                setEndsViewDaily(DESKTOP_DAILY_IMAGE_NO);
                LogView.getLogStage().hide();
            } else {
                //显示日志窗口
                setEndsViewDaily(DESKTOP_DAILY_IMAGE_OK);
                LogView.getLogStage().show();
            }
        });

        return endsView;
    }


    /**
     * 监听窗口属性事件
     */
    private void doWinStage(Stage winStage) {
        winStage.xProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue != null && !isMax) {
                x = newValue.doubleValue();
            }
        });
        winStage.yProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue != null && !isMax) {
                y = newValue.doubleValue();
            }
        });
        winStage.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue != null && !isMax) {
                width = newValue.doubleValue();
            }
        });
        winStage.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue != null && !isMax) {
                height = newValue.doubleValue();
            }
        });
    }


    /**
     * 监听窗口操作事件
     */
    private void doWinState(Stage winStage, BorderPane mainView) {
        //监听窗口移动后事件
        mainView.setOnMouseMoved((MouseEvent event) -> {
            event.consume();
            double tx = event.getSceneX();//记录x数据
            double ty = event.getSceneY();//记录y数据
            double tw = winStage.getWidth();//记录width数据
            double th = winStage.getHeight();//记录height数据
            //光标初始为默认类型，若未进入调整窗口状态则保持默认类型
            Cursor cursorType = Cursor.DEFAULT;
            //将所有调整窗口状态重置
            isRight = isBottomRight = isBottom = false;
            if (ty >= th - resizeWidth) {
                if (tx <= resizeWidth) {
                    //左下角调整窗口状态
                } else if (tx >= tw - resizeWidth) {
                    //右下角调整窗口状态
                    isBottomRight = true;
                    cursorType = Cursor.SE_RESIZE;
                } else {
                    //下边界调整窗口状态
                    isBottom = true;
                    cursorType = Cursor.S_RESIZE;
                }
            } else if (tx >= tw - resizeWidth) {
                // 右边界调整窗口状态
                isRight = true;
                cursorType = Cursor.E_RESIZE;
            }
            // 最后改变鼠标光标
            mainView.setCursor(cursorType);
        });

        //监听窗口拖拽后事件
        mainView.setOnMouseDragged((MouseEvent event) -> {
            event.consume();
            if (yOffset != 0) {
                winStage.setX(event.getScreenX() - xOffset);
                if (event.getScreenY() - yOffset < 0) {
                    winStage.setY(0);
                } else {
                    winStage.setY(event.getScreenY() - yOffset);
                }
            }
            double tx = event.getSceneX();
            double ty = event.getSceneY();
            //保存窗口改变后的x、y坐标和宽度、高度，用于预判是否会小于最小宽度、最小高度
            double nextX = winStage.getX();
            double nextY = winStage.getY();
            double nextWidth = winStage.getWidth();
            double nextHeight = winStage.getHeight();
            if (isRight || isBottomRight) {
                // 所有右边调整窗口状态
                nextWidth = tx;
            }
            if (isBottomRight || isBottom) {
                // 所有下边调整窗口状态
                nextHeight = ty;
            }
            if (nextWidth <= minWidth) {
                // 如果窗口改变后的宽度小于最小宽度，则宽度调整到最小宽度
                nextWidth = minWidth;
            }
            if (nextHeight <= minHeight) {
                // 如果窗口改变后的高度小于最小高度，则高度调整到最小高度
                nextHeight = minHeight;
            }
            // 最后统一改变窗口的x、y坐标和宽度、高度，可以防止刷新频繁出现的屏闪情况
            winStage.setX(nextX);
            winStage.setY(nextY);
            winStage.setWidth(nextWidth);
            winStage.setHeight(nextHeight);

        });

        //鼠标点击获取横纵坐标
        mainView.setOnMousePressed(event -> {
            event.consume();
            xOffset = event.getSceneX();
            if (event.getSceneY() > 46) {
                yOffset = 0;
            } else {
                yOffset = event.getSceneY();
            }
        });

    }


    /**
     * 监听窗口选项事件
     */
    private void doWinItems(Label winItems) {
        TopsMenu.getInstance().show(winItems, Side.BOTTOM, 5, 6);
    }


    /**
     * 监听窗口最小事件
     */
    private void doWinAbate(Stage winStage) {
        winStage.setIconified(true);
    }


    /**
     * 监听窗口最大事件
     */
    private void doWinRaise(Stage winStage) {
        Rectangle2D rectangle2d = Screen.getPrimary().getVisualBounds();
        isMax = !isMax;
        if (isMax) {
            // 最大化
            winStage.setX(rectangle2d.getMinX());
            winStage.setY(rectangle2d.getMinY());
            winStage.setWidth(rectangle2d.getWidth());
            winStage.setHeight(rectangle2d.getHeight());
            webView.setPrefSize(rectangle2d.getWidth(), rectangle2d.getHeight());
        } else {
            if (x == 0 && y == 0 && width == 0 && height == 0) {
                winStage.setWidth(minWidth);
                winStage.setHeight(minHeight);
                winStage.centerOnScreen();
                webView.setPrefSize(minWidth, minHeight);
            } else {
                // 缩放回原来的大小
                winStage.setX(x);
                winStage.setY(y);
                winStage.setWidth(width);
                winStage.setHeight(height);
                webView.setPrefSize(width, height);
            }
        }
    }

    /**
     * 监听窗口关闭事件
     */
    private void doWinClose(Stage winStage) {
        winStage.close();
        Platform.exit();
        System.exit(0);
    }

    public static void setEndsViewTitle(String msg, String type) {
        Node node = mainView.getBottom();
        Label label = (Label) node.lookup("#ends-view-title");
        label.setText(msg);
        switch (type) {
            case "no":
                label.setTextFill(Paint.valueOf("red"));
                break;
            case "ok":
                label.setTextFill(Paint.valueOf("green"));
                break;
        }
    }

    public static void setEndsViewImage(String src) {
        Node node = mainView.getBottom();
        Label label = (Label) node.lookup("#ends-view-image");
        label.setGraphic(new ImageView(new Image(src)));
    }

    public static void setEndsViewDaily(String src) {
        Node node = mainView.getBottom();
        Label label = (Label) node.lookup("#ends-view-daily");
        label.setGraphic(new ImageView(new Image(src)));
    }

    public static void setWebViewPage(String url) {
        String utl = Desktop.class.getResource(url).toExternalForm();
        webEngine.load(utl);
    }

    public static Stage getRootStage() {
        return stage;
    }

    public static GridPane getTopsView() {
        return topsView;
    }

    private void initWebObject() {
        confController = context.getBean(ConfController.class);
        otherController = context.getBean(OtherController.class);
        connectController = context.getBean(ConnectController.class);
        dataSinglesController = context.getBean(DataSinglesController.class);
        dataClusterController = context.getBean(DataClusterController.class);
        infoSinglesController = context.getBean(InfoSinglesController.class);
        infoClusterController = context.getBean(InfoClusterController.class);
    }

}
