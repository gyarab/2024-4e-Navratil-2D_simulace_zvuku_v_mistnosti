package com.example.rp3_2d_simulace_zvuku_v_mistnosti;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WaveManager {
    // List to track all active waves in the room
    private List<SoundWave> activeWaves;
    BaseRoomControllerInterface controller;

    // Reference to a WaveFactory to create new waves
    private WaveFactory waveFactory;
    private PixelManager pixelManager;



    // Flag to track whether waves are currently running
    private boolean isRunning = false;

    @FXML
    private Pane centerPane;

    private Calculator calculator = new Calculator();

    // Initializes a WaveManager instance.
    public WaveManager(WaveFactory waveFactory, Pane centerPane, PixelManager pixelManager) {
        this.waveFactory = waveFactory;
        this.activeWaves = new ArrayList<>();
        this.isRunning = false;
        this.centerPane = centerPane;
        this.pixelManager = pixelManager;

    }

    //Creates a new sound wave at the given coordinates (x, y) with the specified radius.
    public void createWave(double x, double y, BaseRoomControllerInterface controller, int radius, int amplitude) {
        if (centerPane == null) {
            System.err.println("Error: centerPane is null. Cannot add wave.");
            return;
        }
        SoundWave wave = waveFactory.createWave(x, y, controller, radius, amplitude);
        activeWaves.add(wave);
        wave.setPixelManager(pixelManager);
        //centerPane.getChildren().add(wave);

    }

    //Updates the current state of all active waves.
    public void updateWaves(BaseRoomControllerInterface controller) {
        List<SoundWave> wavesToRemove = new ArrayList<>();
        Set<PixelCoordinate> activePixelCoordinates = new HashSet<>(); // Track coordinates of active pixels

        // Temporary set to hold new active pixel coordinates for this update cycle

        for (SoundWave wave : activeWaves) {

            wave.grow();

            if (wave.isOlderThan(8000)) {
                wavesToRemove.add(wave);
            }else if(wave.getAmplitude() <=0 ){
                wavesToRemove.add(wave);
            }

            // Add activePixelCoordinates from the wave to the WaveManager's activePixelCoordinates
            activePixelCoordinates.addAll(wave.getactivePixelCoordinates());

        }

        //Resets all the pixels that are not in the set.
        pixelManager.resetAllInactivePixels(activePixelCoordinates);

        //check all waves if they should be reflected
        checkWavesForReflections(controller);

        //Removal of all inactive waves
        activeWaves.removeAll(wavesToRemove);


    }

    //Handles wave reflections when they hit the walls of the room.
    public void checkWavesForReflections(BaseRoomControllerInterface controller) {
        // Iterate through each wave to determine whether it should be reflected.
        for (SoundWave wave : new ArrayList<>(activeWaves)) {

            Point center = wave.getCenter();

            if (wave.isInRectangle(center.getX(), center.getY())) {
                int[] reflectionDistances = wave.getReflectionDistances();

                // Check if reflectionDistances is null
                if (reflectionDistances == null) {
                    System.err.println("Warning: reflectionDistances is null for wave. Skipping reflection check.");
                    continue;
                }

                int currentRadius = wave.getOuterRadius();

                // Check for reflections on each wall.
                for (int i = 0; i < reflectionDistances.length; i++) {
                    if (currentRadius == reflectionDistances[i]) {

                        // Calculate the symmetric point for reflection.
                        Line reflectingWall = controller.getRoomWalls().get(i);
                        Point symmetricPoint = new Calculator().calculateSymetricPoint(center, reflectingWall);

                        // Create a new wave with reduced amplitude at the reflection point.
                        createWave(symmetricPoint.getX(), symmetricPoint.getY(), controller, wave.getOuterRadius(), wave.getAmplitude()-50);
                        break;
                    }
                }
            } else {
                // Handle cases where the wave is outside the rectangle boundaries.
                handleOutOfRectangleWave(wave, center, controller);
            }
        }
    }

    //Specialized handling for waves that hit the corners of the rectangle when outside.
    private void handleOutOfRectangleWaveForCorners(SoundWave wave, Point center, BaseRoomControllerInterface controller) {

        int currentRadius = wave.getOuterRadius();

        if (wave.isAboveRectangle(center.getX(), center.getY())) {

            if (currentRadius == (int) wave.getCenter().distance(controller.getRoomCorners().get(0))){
                reflectWave(wave, controller, 2);
            }

            if (wave.getRadius() ==(int) wave.getCenter().distance(controller.getRoomCorners().get(3))) {
                reflectWave(wave, controller, 3);
            }

        } else if (wave.isBellowRectangle(center.getX(), center.getY())) {

            if (currentRadius== (int) wave.getCenter().distance(controller.getRoomCorners().get(1))){
                reflectWave(wave, controller, 2);
            }

            if (wave.getRadius() ==(int) wave.getCenter().distance(controller.getRoomCorners().get(2))) {
                reflectWave(wave, controller, 3);
            }

        } else if (wave.isLeftOfRectangle(center.getX(), center.getY())) {

            if (currentRadius== (int) wave.getCenter().distance(controller.getRoomCorners().get(1))){
                reflectWave(wave, controller, 1);
            }
            if (wave.getRadius() == (int)wave.getCenter().distance(controller.getRoomCorners().get(0))) {
                reflectWave(wave, controller, 0);
            }

        } else if (wave.isRightOfRectangle(center.getX(), center.getY())) {

            if (currentRadius== (int) wave.getCenter().distance(controller.getRoomCorners().get(2))){

                reflectWave(wave, controller, 1);
            }
            if (wave.getRadius() == (int)wave.getCenter().distance(controller.getRoomCorners().get(3))) {

                reflectWave(wave, controller, 0);
            }
        }
    }

// Specialized handling for waves that are outside the rectangle boundaries diagonaly
    private void handleDiagonalWavesForCorners(SoundWave wave, Point center, BaseRoomControllerInterface controller) {
        Point topLeft = controller.getRoomCorners().get(0);
        Point bottomLeft = controller.getRoomCorners().get(1);
        Point bottomRight = controller.getRoomCorners().get(2);
        Point topRight = controller.getRoomCorners().get(3);
        int currentRadius = wave.getOuterRadius();

        //nahore nalevo
        if (wave.isAboveOnTheLeftOfRectangle(center.getX(), center.getY())) {

            if (currentRadius== (int) wave.getCenter().distance(bottomLeft)){
                reflectWave(wave, controller, 1);
            }
            if (wave.getRadius() == (int)wave.getCenter().distance(bottomRight)) {
                Point pomocnyBod = calculator.calculateSymetricPoint(center, controller.getRoomWalls().get(1));
                reflectWave(pomocnyBod, wave.getOuterRadius(), controller, 3, wave.getAmplitude());
            }
            if (currentRadius== (int) wave.getCenter().distance(topRight)){
                reflectWave(wave, controller, 3);
            }

            //dole nalevo
        }else if (wave.isBellowOnTheLeftOfRectangle(center.getX(), center.getY())) {

            if (currentRadius== (int) wave.getCenter().distance(bottomRight)){
                reflectWave(wave, controller, 3);
            }
            if (wave.getRadius() == (int)wave.getCenter().distance(topRight)) {
                Point pomocnyBod = calculator.calculateSymetricPoint(center, controller.getRoomWalls().get(3));
                reflectWave(pomocnyBod, wave.getOuterRadius(), controller, 0, wave.getAmplitude());
            }
            if (currentRadius== (int) wave.getCenter().distance(topLeft)){
                reflectWave(wave, controller, 0);
            }

            //dole napravo
        }else if (wave.isBellowOnTheRightOfRectangle(center.getX(), center.getY())) {

            if (currentRadius== (int) wave.getCenter().distance(topRight)){
                reflectWave(wave, controller, 0);
            }
            if (wave.getRadius() == (int)wave.getCenter().distance(topLeft)) {
                Point pomocnyBod = calculator.calculateSymetricPoint(center, controller.getRoomWalls().get(0));
                reflectWave(pomocnyBod, wave.getOuterRadius(), controller, 2, wave.getAmplitude());
            }
            if (currentRadius== (int) wave.getCenter().distance(bottomLeft)){
                reflectWave(wave, controller, 2);
            }

            //nahore napravo
        }else if (wave.isAboveOnTheRightOfRectangle(center.getX(), center.getY())) {

            if (currentRadius== (int) wave.getCenter().distance(topLeft)){
                reflectWave(wave, controller, 2);
            }
            if (wave.getRadius() == (int)wave.getCenter().distance(bottomLeft)) {
                Point pomocnyBod = calculator.calculateSymetricPoint(center, controller.getRoomWalls().get(2));
                reflectWave(pomocnyBod, wave.getOuterRadius(), controller, 1,  wave.getAmplitude());
            }
            if (currentRadius== (int) wave.getCenter().distance(bottomRight)){
                reflectWave(wave, controller, 1);
            }

        }
    }

    // Handles waves that are outside the boundaries of the rectangle.
    private void handleOutOfRectangleWave(SoundWave wave, Point center, BaseRoomControllerInterface controller) {
        int[] reflectionDistances = wave.getReflectionDistances();

        // Fix: Check if reflectionDistances is null
        if (reflectionDistances == null) {
            System.err.println("Warning: reflectionDistances is null for out-of-rectangle wave. Skipping reflection check.");

        }
        // Handle interactions with corners.
        handleOutOfRectangleWaveForCorners(wave, center, controller);
        handleDiagonalWavesForCorners(wave, center, controller);

        // Check specific areas outside the rectangle (above, below, left, or right).
        int currentRadius = wave.getOuterRadius();

        if (wave.isAboveRectangle(center.getX(), center.getY()) && currentRadius == reflectionDistances[0]) {

            reflectWave(wave, controller, 1);

        } else if (wave.isBellowRectangle(center.getX(), center.getY()) && currentRadius == reflectionDistances[0]) {

            reflectWave(wave, controller, 0);

        } else if (wave.isLeftOfRectangle(center.getX(), center.getY()) && currentRadius == reflectionDistances[0]) {

            reflectWave(wave, controller, 3);

        } else if (wave.isRightOfRectangle(center.getX(), center.getY()) && currentRadius == reflectionDistances[0]) {

            reflectWave(wave, controller, 2);

        }

    }

    //Creates a reflected wave when an existing wave interacts with a wall.
    private void reflectWave(SoundWave wave, BaseRoomControllerInterface controller, int wallIndex) {
        Point center = wave.getCenter();
        Line reflectingWall = controller.getRoomWalls().get(wallIndex);
        Point symmetricPoint = new Calculator().calculateSymetricPoint(center, reflectingWall);
        if (wave.getAmplitude() >0){
            createWave(symmetricPoint.getX(), symmetricPoint.getY(), controller, wave.getOuterRadius(),wave.getAmplitude()-50);
        }
    }

    private void reflectWave(Point center, int currentRadius, BaseRoomControllerInterface controller, int wallIndex, int amplituda) {
        Line reflectingWall = controller.getRoomWalls().get(wallIndex);
        Point symmetricPoint = new Calculator().calculateSymetricPoint(center, reflectingWall);
        if (amplituda>0){
            createWave(symmetricPoint.getX(), symmetricPoint.getY(), controller, currentRadius, amplituda-50);
        }
    }

    public void resumeWaves() {
        isRunning = true;
        for (SoundWave wave : activeWaves) {
            wave.resume();
        }
    }

    public void pauseWaves() {
        isRunning = false;
        for (SoundWave wave : activeWaves) {
            wave.pause();
        }
    }

    public void resetWaves() {
        removeAllWaves(centerPane);
        activeWaves.clear();

    }

    public void removeAllWaves(Pane centerPane){
        for (SoundWave wave : activeWaves) {
            if (centerPane != null) {
                centerPane.getChildren().remove(wave);
                centerPane.getChildren().remove(wave.getInnerCircle());
            }
        }
    }
}