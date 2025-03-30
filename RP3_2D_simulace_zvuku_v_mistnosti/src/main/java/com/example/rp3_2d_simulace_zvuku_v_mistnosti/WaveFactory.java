package com.example.rp3_2d_simulace_zvuku_v_mistnosti;

public class WaveFactory {
    /**
    * Returns new SoundWave.
     */
    public SoundWave createWave(double x, double y, BaseRoomControllerInterface controller, int radius, int amplitude) {
        return new SoundWave(x, y, controller, radius, amplitude);
    }
}
