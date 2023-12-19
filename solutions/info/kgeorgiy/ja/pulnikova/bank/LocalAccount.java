package info.kgeorgiy.ja.pulnikova.bank;

import java.io.Serializable;
import java.util.Objects;

public class LocalAccount implements Account, Serializable {
    private final String id;
    private int amount;

    public LocalAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized int getAmount() {
        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    @Override
    public synchronized void setAmount(final int amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof LocalAccount){
            return Objects.equals(((LocalAccount) o).getId(), this.id)
                    && Objects.equals(((LocalAccount) o).getAmount(), this.amount);
        }else {
            return false;
        }
    }

    @Override
    public int hashCode(){
        return id.hashCode() + amount;
    }
}
