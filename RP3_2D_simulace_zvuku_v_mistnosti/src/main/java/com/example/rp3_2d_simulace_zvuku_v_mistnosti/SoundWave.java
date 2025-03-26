package com.example.rp3_2d_simulace_zvuku_v_mistnosti;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.*;


/**
 * TODO udelej hledani bodu vice efektivni zkus Bresenham’s Circle Drawing Algorithm
 */
public class SoundWave extends Circle {
    private double x;          // Starting X position
    private double y;          // Starting Y position
    private int outerRadius;   // The current radius of the wave
    private final int deltaR = 60;   // Fixed difference between radii (donut thickness)
    private int innerRadius = 0;      // Inner circle radius (calculated as currentRadius - deltaR)

    private Circle innerCircle; // Circle object to represent the inner boundary (when it appears)
    private boolean innerCircleIsCreated = false;


    private int amplitude;
    PixelManager pixelManager;
    Pixel[][] pixelGrid;
    private int xMin, xMax, yMin, yMax;

    //nezapomen zmenit periodu i v soundWave
    public  int PERIODA = 5;
    private int PIXELSIZE = 3;
    private int okamzitaVychylka;

    /**
     * Constructor initializes the SoundWave object, its position, and relevant fields.
     * It also generates the mathematical representation of the sound wave circle.
     */
    public SoundWave(double x, double y, BaseRoomControllerInterface controller, int radius,  int amplitude, int direction) {
        super(x, y, 0); // Initialize circle with position (x, y) and radius 0
        this.x = x;
        this.y = y;
        this.outerRadius = radius;
        this.amplitude = amplitude;
        this.direction = direction;

        // Initialize the room and geometric properties
        this.controller = controller;
        roomWalls = controller.getRoomWalls();
        center = new Point(x, y);
        this.creationTime = System.currentTimeMillis(); // Store creation time
        initializeLines(x, y);


        this.xMin = controller.getXMin();
        this.xMax = controller.getXMax();
        this.yMin = controller.getYMin();
        this.yMax = controller.getYMax();


        // Initialize other properties
        this.setStroke( Color.BLACK);
        this.setStrokeWidth(1);          // Outline thickness
        this.setFill(Color.TRANSPARENT); // Transparent fill
        this.setMouseTransparent(true);

        topLeft = controller.getRoomCorners().get(0);
        bottomLeft = controller.getRoomCorners().get(1);
        bottomRight = controller.getRoomCorners().get(2);
        topRight = controller.getRoomCorners().get(3);
        intersections = getIntersectionsWithWalls(x, y);


    }

    private void createInnerCircle(double x , double y, int innerRadius) {
        innerCircle = new Circle(x, y, innerRadius);
        innerCircle.setMouseTransparent(true);
        innerCircle.setStroke(Color.RED);
        innerCircle.setStrokeWidth(1);
        innerCircle.setFill(null);
    }

    public Circle getInnerCircle() {
        if (outerRadius >deltaR)return innerCircle;
        else return null;
    }


    public void setPixelManager(PixelManager pixelManager){
        this.pixelManager = pixelManager;
        pixelGrid = pixelManager.getPixelGrid();
    }
    

    public int getAmplitude() {
        return amplitude;
    }



    // New field to control the direction (1 for normal, -1 for opposite/progressing backwards)
    private int direction;

    public int getDirection() {
        return direction;
    }

    // promene na cas
    private long creationTime; // Timestamp when the wave was created
    private long elapsedPausedTime = 0; // Time we've spent paused
    private long pauseStartTime; // When the wave was paused
    private boolean isPaused = false; // Is the wave currently paused?


    // Getter to calculate elapsedTime dynamically
    public double getElapsedTime() {
        if (isPaused) {
            // If paused, return the time until the pause started
            return (pauseStartTime - creationTime - elapsedPausedTime) / 1000.0; // Convert ms to seconds
        } else {
            // If not paused, return total elapsed time
            return (System.currentTimeMillis() - creationTime - elapsedPausedTime) / 1000.0; // Convert ms to seconds
        }
    }


    private Calculator calculator = new Calculator();
    private BaseRoomControllerInterface controller;



    private List<Line> waveLines;
    private List<Line> roomWalls;
    private Point center;
    private Point[] intersections;
    private Point topLeft;
    private Point topRight;
    private Point bottomLeft;
    private Point bottomRight;

    private void initializeLines(double x, double y){
        Line vertical = new Line(1,0,-x);
        Line horizontal = new Line(0,1,-y);
        waveLines = List.of(vertical, horizontal);

    }




    public int[] getReflectionDistances(){

        Point topLeft = controller.getRoomCorners().get(0);
        Point bottomLeft = controller.getRoomCorners().get(1);
        Point bottomRight = controller.getRoomCorners().get(2);
        Point topRight = controller.getRoomCorners().get(3);

        //jestli je to v mistnosti
        if (isInRectangle(x,y) ) {

            int[] distances = new int[4];
            distances[0] = (int) center.distance(getIntersectionsWithWalls(x,y)[0]);
            distances[1] = (int) center.distance(getIntersectionsWithWalls(x,y)[1]);
            distances[2] = (int) center.distance(getIntersectionsWithWalls(x,y)[2]);
            distances[3] = (int) center.distance(getIntersectionsWithWalls(x,y)[3]);

            return distances;

            //mezi lefou a pravou primkou ale nad horni primnkou
        } else if ( isAboveRectangle(x,y)){
            int[] distances = new int[1];
            distances[0] = (int) center.distance(getIntersectionsWithWalls(x,y)[0]);
            return distances;

            //mezi levou a pravou primkou ale pod spodni primkou
        } else if (isBellowRectangle(x,y)) {
            int[] distances = new int[1];
            distances[0] = (int) center.distance(getIntersectionsWithWalls(x,y)[0]);
            return distances;

            //nalevo od leve primky ale mezi horni a spodni primkou
        } else if ( isLeftOfRectangle(x,y)) {
            int[] distances = new int[1];
            distances[0] = (int) center.distance(getIntersectionsWithWalls(x,y)[0]);
            return distances;

            //napravo od prave primky ale mezi horni a spodni primkou
        } else if ( isRightOfRectangle(x,y)) {
            int[] distances = new int[1];
            distances[0] = (int) center.distance(getIntersectionsWithWalls(x,y)[0]);
            return distances;

            //napravo nahore
        } else if (isAboveOnTheRightOfRectangle(x,y)){
            int[] distances = new int[3];
            distances[0] = (int) center.distance(topLeft);
            distances[1] = (int) center.distance(bottomLeft);
            distances[2] = (int) center.distance(bottomRight);
            return distances;

            //napravo dole
        }else if (isBellowOnTheRightOfRectangle(x,y)){
            int[] distances = new int[3];
            distances[0] = (int) center.distance(topRight);
            distances[1] = (int) center.distance(topLeft);
            distances[2] = (int) center.distance(bottomLeft);
            return distances;

            //nalevo nahore
        }else if (isAboveOnTheLeftOfRectangle(x,y)){
            int[] distances = new int[3];
            distances[0] = (int) center.distance(bottomLeft);
            distances[1] = (int) center.distance(bottomRight);
            distances[2] = (int) center.distance(topRight);
            return distances;

            //nalevo dole
        }else if (isBellowOnTheLeftOfRectangle(x,y)){
            int[] distances = new int[3];
            distances[0] = (int) center.distance(bottomRight);
            distances[1] = (int) center.distance(topRight);
            distances[2] = (int) center.distance(topLeft);
            return distances;

        } else{

            System.err.println("Error: The x and y coordinates are not valid.");
            System.out.println("x coordinate: " + x);
            System.out.println("y coordinate: " + y);
            System.err.println("Error: The x and y coordinates are not valid.");
            return null;
        }
    }

    //ziskani pruseciku
    private Point[] getIntersectionsWithWalls(double x, double y){

        //jestli souradnice stredu jsou v mistnosti
        if (isInRectangle(x,y)) {

            Point topIntersection = calculator.calculateIntersection(waveLines.get(0), roomWalls.get(0));
            Point bottomIntersection = calculator.calculateIntersection(waveLines.get(0), roomWalls.get(1));
            Point leftIntersection = calculator.calculateIntersection(waveLines.get(1), roomWalls.get(2));
            Point rightIntersection = calculator.calculateIntersection(waveLines.get(1), roomWalls.get(3));

            return new Point[]{topIntersection, bottomIntersection, leftIntersection, rightIntersection};

            //mezi lefou a pravou primkou ale nad horni primnkou
        } else if ( isAboveRectangle(x,y)){

            Point bottomIntersection = calculator.calculateIntersection(waveLines.get(0), roomWalls.get(1));

            return new Point[]{bottomIntersection};

            //mezi levou a pravou primkou ale pod spodni primkou
        } else if ( isBellowRectangle(x,y)) {

            Point topIntersection = calculator.calculateIntersection(waveLines.get(0), roomWalls.get(0));
            return new Point[]{topIntersection};

            //nalevo od leve primky ale mezi horni a spodni primkou
        } else if ( isLeftOfRectangle(x,y)) {

            Point rightIntersection = calculator.calculateIntersection(waveLines.get(1), roomWalls.get(3));
            return new Point[]{rightIntersection};

            //napravo od prave primky ale mezi horni a spodni primkou
        } else if ( isRightOfRectangle(x,y)) {

            Point leftIntersection = calculator.calculateIntersection(waveLines.get(1), roomWalls.get(2));
            return new Point[]{leftIntersection};
        }else {
            return null;
        }
    }


    //calculates the okamzita vychylka based on elapsed time and the phaze of the wave
    public int getokamzitaVychylka() {
        if (direction == 1) {
            // Ensure amplitude is positive
            amplitude = Math.max(amplitude, 0);

            // Cycle duration (perioda)
            double cycleDuration =  PERIODA;

            // Total number of steps (for full cycle)
            int stepsToAmplitude = amplitude;             // 0 to +amplitude
            int stepsToNegativeAmplitude = 2 * amplitude; // +amplitude to -amplitude
            int stepsToZero = amplitude;                  // -amplitude back to 0
            int totalSteps = stepsToAmplitude + stepsToNegativeAmplitude + stepsToZero;

            // Derive time per step dynamically
            double timePerStep = cycleDuration / totalSteps;

            // Normalize elapsedTime to the cycle (loop every cycleDuration)
            double timeInCycle = getElapsedTime() % cycleDuration;

            // Calculate which step we are in based on elapsed time
            int currentStep = (int) (timeInCycle / timePerStep);

            // Determine which phase of the cycle we're in
            if (currentStep < stepsToAmplitude) {
                // Phase 1: 0 to +amplitude
                return currentStep;
            } else if (currentStep < stepsToAmplitude + stepsToNegativeAmplitude) {
                // Phase 2: +amplitude to -amplitude
                return amplitude - (currentStep - stepsToAmplitude);
            } else {
                // Phase 3: -amplitude back to 0
                return -amplitude + (currentStep - (stepsToAmplitude + stepsToNegativeAmplitude));
            }

        }else{
            // Ensure amplitude is positive
            amplitude = Math.max(amplitude, 0);

            // Cycle duration (perioda) is fixed to 1 second
            double cycleDuration =  PERIODA;

            // Total number of steps (for full cycle)
            int stepsToAmplitude = amplitude;             // 0 to -amplitude
            int stepsToPositiveAmplitude = 2 * amplitude; // -amplitude to +amplitude
            int stepsToZero = amplitude;                  // +amplitude back to 0
            int totalSteps = stepsToAmplitude + stepsToPositiveAmplitude + stepsToZero;

            // Derive time per step dynamically
            double timePerStep = cycleDuration / totalSteps;

            // Normalize elapsedTime to the cycle (loop every cycleDuration)
            double timeInCycle = getElapsedTime() % cycleDuration;

            // Calculate which step we are in based on elapsed time
            int currentStep = (int) (timeInCycle / timePerStep);

            // Determine which phase of the cycle we're in
            if (currentStep < stepsToAmplitude) {
                // Phase 1: 0 to -amplitude
                return -currentStep;
            } else if (currentStep < stepsToAmplitude + stepsToPositiveAmplitude) {
                // Phase 2: -amplitude to +amplitude
                return -amplitude + (currentStep - stepsToAmplitude);
            } else {
                // Phase 3: +amplitude back to 0
                return amplitude - (currentStep - (stepsToAmplitude + stepsToPositiveAmplitude));
            }
        }

    }



    public Point getCenter() {
        return center;
    }

    public int getOuterRadius() {
        return outerRadius;
    }


    public void pause() {
        if (!isPaused) {
            pauseStartTime = System.currentTimeMillis();
            isPaused = true;
        }
    }

    public void resume() {
        if (isPaused) {
            long now = System.currentTimeMillis();
            elapsedPausedTime += (now - pauseStartTime); // Add the time spent paused
            isPaused = false;
        }
    }


    public boolean isInRectangle(double x, double y){
        return x > controller.getXMin() && x < controller.getXMax() && y > controller.getYMin() && y < controller.getYMax();
    }


    public boolean isBellowRectangle(double x, double y){
        return x > controller.getXMin() && x < controller.getXMax() && y > controller.getYMax();
    }

    public boolean isAboveRectangle(double x, double y){
        return x > controller.getXMin() && x < controller.getXMax() && y < controller.getYMin();
    }

    public boolean isLeftOfRectangle(double x, double y){
        return x < controller.getXMin() && y > controller.getYMin() && y < controller.getYMax();
    }

    public boolean isRightOfRectangle(double x, double y){
        return x > controller.getXMax() && y > controller.getYMin() && y < controller.getYMax();
    }

    public boolean isAboveOnTheRightOfRectangle(double x, double y){
        return x > controller.getXMax() && y < controller.getYMin();
    }

    public boolean isAboveOnTheLeftOfRectangle(double x, double y){
        return x < controller.getXMin() && y < controller.getYMin();
    }

    public boolean isBellowOnTheRightOfRectangle(double x, double y){
        return x > controller.getXMax() && y > controller.getYMax();
    }

    public boolean isBellowOnTheLeftOfRectangle(double x, double y){
        return x < controller.getXMin() && y > controller.getYMax();
    }

    /**
     * Checks if the wave is older than a specified lifetime (in milliseconds).
     * @param maxAge The lifetime in milliseconds (e.g., 5000 for 5 seconds).
     * @return True if the wave is older than the given lifespan, false otherwise.
     */
    public boolean isOlderThan(long maxAge) {
        long now = System.currentTimeMillis();
        long effectiveAge;
        if (isPaused) {
            effectiveAge = (pauseStartTime - creationTime) - elapsedPausedTime; // Paused state
        } else {
            effectiveAge = (now - creationTime) - elapsedPausedTime; // Running state
        }
        return effectiveAge > maxAge;
    }


    public void grow() {
// Increment the outer radius
        outerRadius += 1;  // Example growth increment

        this.setRadius(outerRadius);

        // Handle inner circle creation and growth
        if (outerRadius > deltaR) {
            if (!innerCircleIsCreated) {
                // Create inner circle when threshold is met
                innerRadius = outerRadius - deltaR;
                createInnerCircle(x, y, innerRadius);
                innerCircleIsCreated = true;
            } else {
                // Grow inner circle incrementally as outer radius grows
                innerRadius += 1;
                innerCircle.setRadius(innerRadius);
            }
        }

        // Generate the donut wave dynamically
        if (outerRadius%PIXELSIZE == 0 && !isPaused){
            savedSetOfPixelCoords.clear();
            generateWaveDonut();
            savedSetOfPixelCoords.addAll(activePixelCoordinates);
        }


        // Reset visited and duplicate pixel sets for recalculating transitions
        resetVisitedPixels();
    }


    // Use a Set to track pixels added
    private final Set<PixelCoordinate> visitedPixels = new HashSet<>();
    private final Set<PixelCoordinate> duplicatePixels = new HashSet<>(); // To store duplicates for debugging
    private final Set<PixelCoordinate> activePixelCoordinates = new HashSet<>(); // Track coordinates of active pixels

    private final Set<PixelCoordinate> savedSetOfPixelCoords = new HashSet<>(); // save the active pixel coordinates in order to deliver it to waveManager

    public Set<PixelCoordinate> getactivePixelCoordinates   () {
        return savedSetOfPixelCoords;
    }

    /**
     * Generates concentric circles for the sound wave and updates the `okamzitaVychylka`
     * for every pixel in the donut (between innerRadius and outerRadius).
     */
    public void generateWaveDonut() {
        // Calculate the wave width
        int waveWidth = outerRadius - innerRadius;

        // Calculate the number of circles in the donut
        int numberOfCircles = waveWidth / PIXELSIZE;



        // Loop through all the concentric circles in the donut
        for (int i = 0; i < numberOfCircles; i++) {
            // Calculate the radius for the current circle
            int radius = innerRadius + ((numberOfCircles - i - 1) * PIXELSIZE);

            // Determine amplitude for the current circle
            int amplitudeForCircle = calculateAmplitudeForCircle(i);

            // Debugging output
            //System.out.println("Circle Index: " + i + " | Amplitude: " + amplitudeForCircle);

            // Draw the circle with the calculated instantaneous displacement
            drawCircleWithOkamzitaVychylka((int) x, (int) y, radius, amplitudeForCircle);
        }

        // End cycle debugging output
        //System.out.println("End of donut generation *************************************************");
    }

    /**
     * Resets the sets tracking visited and duplicate pixels for the next update.
     */
    public void resetVisitedPixels() {
        visitedPixels.clear();
        duplicatePixels.clear();
        activePixelCoordinates.clear(); // Reset active pixels as well
    }

    /**
     * Determines the amplitude (okamzitaVychylka) for the given circle index
     * based on its position in the wave cycle.
     */
    private int calculateAmplitudeForCircle(int circleIndex) {

        if  (circleIndex < 5){
            return amplitude/5;
        }else if (circleIndex < 15){
            return -amplitude/5;
        }else{
            return amplitude/5;
        }
    }

    /**
     * Draws a circle using Bresenham's algorithm and adds `okamzitaVychylka` to each pixel.
     */
    private void drawCircleWithOkamzitaVychylka(int centerX, int centerY, int radius, int okamzitaVychylkaValue) {
        int x = 0;
        int y = radius;
        int d = 3 - 2 * radius;

        // Set initial circle pixels
        setCirclePixelsDisplacement(centerX, centerY, x, y, okamzitaVychylkaValue);
        while (y >= x) {
            x++;
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }

            // Set the pixels for this circle
            setCirclePixelsDisplacement(centerX, centerY, x, y, okamzitaVychylkaValue);
        }
    }


    /**
     * Sets  okamzitaVychylka for all eight symmetric octants of a circle.
     */
    private void setCirclePixelsDisplacement(int centerX, int centerY, int x, int y, int okamzitaVychylkaValue) {

        if (centerY + y > yMin && centerY + y < yMax && centerX + x > xMin && centerX + x < xMax) {
            setPixelDisplacement(centerX + x, centerY + y, okamzitaVychylkaValue);
        }
        if (centerY + y > yMin && centerY + y < yMax && centerX - x > xMin && centerX - x < xMax) {
            setPixelDisplacement(centerX - x, centerY + y, okamzitaVychylkaValue);
        }
        if (centerY - y > yMin && centerY - y < yMax && centerX + x > xMin && centerX + x < xMax) {
            setPixelDisplacement(centerX + x, centerY - y, okamzitaVychylkaValue);
        }
        if (centerY - y > yMin && centerY - y < yMax && centerX - x > xMin && centerX - x < xMax) {
            setPixelDisplacement(centerX - x, centerY - y, okamzitaVychylkaValue);
        }
        if (centerY + x > yMin && centerY + x < yMax && centerX + y > xMin && centerX + y < xMax) {
            setPixelDisplacement(centerX + y, centerY + x, okamzitaVychylkaValue);
        }
        if (centerY + x > yMin && centerY + x < yMax && centerX - y > xMin && centerX - y < xMax) {
            setPixelDisplacement(centerX - y, centerY + x, okamzitaVychylkaValue);
        }
        if (centerY - x > yMin && centerY - x < yMax && centerX + y > xMin && centerX + y < xMax) {
            setPixelDisplacement(centerX + y, centerY - x, okamzitaVychylkaValue);
        }
        if (centerY - x > yMin && centerY - x < yMax && centerX - y > xMin && centerX - y < xMax) {
            setPixelDisplacement(centerX - y, centerY - x, okamzitaVychylkaValue);
        }
    }


    /**
     * Adds the `okamzitaVychylka` displacement to the specified pixel, ensuring boundary constraints are respected
     * and avoiding duplicate updates using `visitedPixels`.
     */
    private void setPixelDisplacement(int x, int y, int okamzitaVychylkaValue) {
        // Check if the pixel is within the grid boundaries
        //if (x >= xMin && x <= xMax && y >= yMin && y <= yMax) {
        int gridX = Pixel.getGridX(x, controller.getXMin());
        int gridY = Pixel.getGridY(y, controller.getYMin());

        if (gridX >= 0 && gridX < pixelGrid.length && gridY >= 0 && gridY < pixelGrid[0].length) {
            // Create a PixelCoordinate object
            PixelCoordinate pixelCoord = new PixelCoordinate(gridX, gridY);

            if (visitedPixels.contains(pixelCoord)) {
                // If already visited, log as a duplicate
                duplicatePixels.add(pixelCoord);
            } else {
                visitedPixels.add(pixelCoord);
                activePixelCoordinates.add(pixelCoord); // Mark as active
                pixelGrid[gridX][gridY].addVychylka(okamzitaVychylkaValue);


                activatePixelCoordinate(gridX, gridY);
            }
        }
        //}
    }




    private void activatePixelCoordinate(int gridX, int gridY) {
        PixelCoordinate coord = new PixelCoordinate(gridX, gridY);

        // Add the coordinate to the active set
        activePixelCoordinates.add(coord);
    }


}
