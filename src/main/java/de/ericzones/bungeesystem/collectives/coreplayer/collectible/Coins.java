// Created by Eric B. 09.02.2021 11:15
package de.ericzones.bungeesystem.collectives.coreplayer.collectible;

public class Coins {

    private int currentCoins;

    public Coins(int previousCoins) {
        this.currentCoins = previousCoins;
    }

    public int getCurrentCoins() {
        return this.currentCoins;
    }

    public void addCoins(int coins) {
        currentCoins += coins;
    }

    public void setCoins(int coins) {
        currentCoins = coins;
    }

    public boolean removeCoins(int coins) {
        if(currentCoins < coins)
            return false;
        currentCoins -= coins;
        return true;
    }

    public void resetCoins() {
        currentCoins = 0;
    }

}
