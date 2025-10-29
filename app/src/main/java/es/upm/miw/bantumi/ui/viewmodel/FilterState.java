package es.upm.miw.bantumi.ui.viewmodel;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class FilterState implements Serializable {
    public enum Outcome { ALL, WINS, LOSSES }
    public enum Order { SEEDS_ASC, SEEDS_DESC }

    public Order order = Order.SEEDS_DESC;
    public Outcome outcome = Outcome.ALL;
    @Nullable public String mode = null;
    @Nullable public String nameContains = null;

    public static FilterState defaults() { return new FilterState(); }
}

