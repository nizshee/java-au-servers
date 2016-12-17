package com.github.nizshee.ui;


import com.github.nizshee.Message;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class Main extends Application {

    public static final String TCP_CONT_THREAD = "TcpContThread";
    public static final String TCP_CONT_POOL = "TcpContPool";
    public static final String TCP_CONT_NONBLOCK = "TcpContNonblock";
    public static final String TCP_CONT_ASYNC = "TcpContAsync";
    public static final String TCP_NEW = "TcpNew";
    public static final String UDP_THREAD = "UdpThread";
    public static final String UDP_POOL = "UdpPool";

    public static final String DELTA = "delta";
    public static final String SIZE = "size";
    public static final String COUNT = "count";

    private static InetAddress serverHost;
    private static InetAddress clientHost;
    private static int serverPort;
    private static int clientPort;
    private static String server;
    private static int port;

    private int requestCount = 50; // 10 100 10

    private int clientCount = 50;
    private int deltaValue = 50; // 10 100 10
    private int sizeValue = 1000; // 400 2400 200

    private int fromValue = 10;
    private int toValue = 100;
    private int stepValue = 10;

    private Message.Arch archValue = null;
    private Tester.Parameter parameterValue = null;

    @Override public void start(Stage stage) {

        TextField request = new TextField(Integer.toString(requestCount));
        HBox requestBox = new HBox();
        requestBox.getChildren().addAll(request, new Text("requestCount"));

        TextField client = new TextField(Integer.toString(clientCount));
        HBox clientBox = new HBox();
        clientBox.getChildren().addAll(client, new Text("clientCount"));

        TextField delta = new TextField(Integer.toString(deltaValue));
        HBox deltaBox = new HBox();
        deltaBox.getChildren().addAll(delta, new Text("delta"));

        TextField size = new TextField(Integer.toString(sizeValue));
        HBox sizeBox = new HBox();
        sizeBox.getChildren().addAll(size, new Text("arraySize"));

        TextField from = new TextField(Integer.toString(fromValue));
        TextField to = new TextField(Integer.toString(toValue));
        TextField step = new TextField(Integer.toString(stepValue));
        HBox box = new HBox();
        box.getChildren().addAll(from, to, step, new Text("from, to, step"));

        ChoiceBox<String> arch = new ChoiceBox<>(FXCollections.observableArrayList(
                TCP_CONT_THREAD, TCP_CONT_POOL, TCP_CONT_NONBLOCK, TCP_CONT_ASYNC, TCP_NEW, UDP_THREAD, UDP_POOL
        ));
        arch.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case TCP_CONT_THREAD:
                    archValue = Message.Arch.TcpContThread;
                    break;
                case TCP_CONT_POOL:
                    archValue = Message.Arch.TcpContPool;
                    break;
                case TCP_CONT_NONBLOCK:
                    archValue = Message.Arch.TcpContNonblock;
                    break;
                case TCP_CONT_ASYNC:
                    archValue = Message.Arch.TcpAssync;
                    break;
                case TCP_NEW:
                    archValue = Message.Arch.TcpNew;
                    break;
                case UDP_THREAD:
                    archValue = Message.Arch.UdpThread;
                    break;
                case UDP_POOL:
                    archValue = Message.Arch.UdpPool;
                    break;
            }
        });
        arch.getSelectionModel().select(TCP_CONT_THREAD);

        ChoiceBox<String> parameter = new ChoiceBox<>(FXCollections.observableArrayList(
                DELTA, SIZE, COUNT
        ));
        parameter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case DELTA:
                    parameterValue = Tester.Parameter.DELTA;
                    break;
                case SIZE:
                    parameterValue = Tester.Parameter.SIZE;
                    break;
                case COUNT:
                    parameterValue = Tester.Parameter.COUNT;
                    break;
            }
        });
        parameter.getSelectionModel().select(DELTA);

        Button save = new Button("save");
        save.setOnAction(event -> {
            try {
                requestCount = Integer.parseInt(request.getText());
            } catch (Exception e) {
                request.setText(Integer.toString(requestCount));
            }
            try {
                clientCount = Integer.parseInt(client.getText());
            } catch (Exception e) {
                client.setText(Integer.toString(clientCount));
            }
            try {
                deltaValue = Integer.parseInt(delta.getText());
            } catch (Exception e) {
                delta.setText(Integer.toString(deltaValue));
            }
            try {
                sizeValue = Integer.parseInt(size.getText());
            } catch (Exception e) {
                size.setText(Integer.toString(sizeValue));
            }
            try {
                fromValue = Integer.parseInt(from.getText());
            } catch (Exception e) {
                from.setText(Integer.toString(fromValue));
            }
            try {
                toValue = Integer.parseInt(to.getText());
            } catch (Exception e) {
                to.setText(Integer.toString(toValue));
            }
            try {
                stepValue = Integer.parseInt(step.getText());
            } catch (Exception e) {
                step.setText(Integer.toString(stepValue));
            }
        });

        Button start = new Button("start");
        start.setOnAction(event -> {
            stage.setScene(new Scene(new FlowPane(new Text("processing..."))));

            new Thread(() -> {
                Tester tester = new Tester(serverHost, clientHost, serverPort, clientPort);
                try {
                    List<Result> results = tester.start(fromValue, toValue, stepValue, parameterValue, archValue,
                            requestCount, clientCount, deltaValue, sizeValue, server, port);

                    List<Integer> x = new ArrayList<>();
                    for (int i = fromValue; i <= toValue; i += stepValue) {
                        x.add(i);
                    }
                    List<Integer> sortY = new ArrayList<>(x.size());
                    List<Integer> requestY = new ArrayList<>(x.size());
                    List<Integer> clientY = new ArrayList<>(x.size());
                    for (int i = 0; i < x.size(); ++i) {
                        Result result = results.get(i);
                        sortY.add(result.sort);
                        requestY.add(result.request);
                        clientY.add(result.client);
                    }

                    LineChart<Number, Number> lc = createLineChart("sort", x, sortY);
                    LineChart<Number, Number> lc1 = createLineChart("sort", x, requestY);
                    LineChart<Number, Number> lc2 = createLineChart("request", x, clientY);

                    FlowPane root = new FlowPane();
                    root.getChildren().addAll(lc, lc1, lc2);

                    Platform.runLater(() -> stage.setScene(new Scene(root, 900, 300)));

                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> stage.setScene(new Scene(new FlowPane(new Text(e.getMessage())))));
                }

            }).start();

        });

        VBox vBox = new VBox();
        vBox.getChildren().addAll(requestBox, clientBox, deltaBox, sizeBox, arch, parameter, box, save, start);

        Scene scene = new Scene(vBox);

        stage.setTitle("Test");
        stage.setScene(scene);
        stage.show();
    }

    private LineChart<Number, Number> createLineChart(String name, List<Integer> x, List<Integer> y) {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(name);

        final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

        lineChart.setMinWidth(300);
        lineChart.setMaxWidth(300);
        lineChart.setMinHeight(300);
        lineChart.setMaxHeight(300);

        XYChart.Series<Number, Number> series = new LineChart.Series<>();

        for (int i = 0; i < x.size(); i++) {
            XYChart.Data<Number, Number> data = new LineChart.Data<>(x.get(i), y.get(i));
            series.getData().add(data);
        }
        lineChart.getData().add(series);
        return lineChart;
    }

    public static void main(String[] args) throws Exception {
        serverHost = InetAddress.getByName(args.length > 0 ? args[0] : "localhost");
        serverPort = args.length > 1 ? Integer.parseInt(args[1]) : 12345;
        clientHost = InetAddress.getByName(args.length > 2 ? args[2] : "localhost");
        clientPort = args.length > 3 ? Integer.parseInt(args[3]) : 12346;
        server = args.length > 4 ? args[4] : "localhost";
        port = args.length > 5 ? Integer.parseInt(args[5]) : 12347;
        launch(args);
    }
}
