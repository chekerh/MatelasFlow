package com.warehouse.ui;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Simple shimmer skeleton placeholder used while tables load data asynchronously.
 */
public class SkeletonPane extends StackPane {
    private final Rectangle shimmer = new Rectangle();

    private SkeletonPane(double prefWidth, double prefHeight) {
        getStyleClass().add("skeleton-pane");
        setMinSize(prefWidth, prefHeight);
        setPrefSize(prefWidth, prefHeight);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Region base = new Region();
        base.getStyleClass().add("skeleton-base");
        base.setMinSize(prefWidth, prefHeight);
        base.setPrefSize(prefWidth, prefHeight);

        shimmer.widthProperty().bind(base.widthProperty().multiply(0.35));
        shimmer.heightProperty().bind(base.heightProperty());
        shimmer.setFill(Color.web("#ffffff33"));
        shimmer.setArcWidth(24);
        shimmer.setArcHeight(24);

        getChildren().addAll(base, shimmer);

        TranslateTransition transition = new TranslateTransition(Duration.millis(1400), shimmer);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        transition.setFromX(-prefWidth);
        transition.setToX(prefWidth);
        transition.setCycleCount(TranslateTransition.INDEFINITE);
        transition.play();
    }

    public static SkeletonPane forTable(double width, double height) {
        return new SkeletonPane(width, height);
    }
}

