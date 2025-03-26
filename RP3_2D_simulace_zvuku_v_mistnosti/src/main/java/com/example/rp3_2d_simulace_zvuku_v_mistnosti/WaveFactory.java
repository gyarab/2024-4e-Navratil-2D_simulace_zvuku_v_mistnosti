package com.example.rp3_2d_simulace_zvuku_v_mistnosti;

public class WaveFactory {
    /**
     * Creates a new SoundWave at the given position.
     *
     * @param x         The starting X coordinate of the wave.
     * @param y         The starting Y coordinate of the wave.
     * @return A new instance of SoundWave.
     */
    public SoundWave createWave(double x, double y, BaseRoomControllerInterface controller, int radius, int amplitude, int direction) {




        return new SoundWave(x, y, controller, radius, amplitude, direction);
    }


}
