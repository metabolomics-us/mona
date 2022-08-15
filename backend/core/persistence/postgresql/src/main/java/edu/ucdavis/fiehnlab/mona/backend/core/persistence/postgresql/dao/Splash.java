package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.dao;

import java.io.Serializable;
import java.util.Objects;

public class Splash implements Serializable {
    private String block1;
    private String block2;
    private String block3;
    private String block4;
    private String splash;

    public Splash() {
    }

    public String getBlock1() {
        return block1;
    }

    public String getBlock2() {
        return block2;
    }

    public String getBlock3() {
        return block3;
    }

    public String getBlock4() {
        return block4;
    }

    public String getSplash() {
        return splash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Splash splash1 = (Splash) o;
        return Objects.equals(block1, splash1.block1) && Objects.equals(block2, splash1.block2) && Objects.equals(block3, splash1.block3) && Objects.equals(block4, splash1.block4) && Objects.equals(splash, splash1.splash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block1, block2, block3, block4, splash);
    }
}
