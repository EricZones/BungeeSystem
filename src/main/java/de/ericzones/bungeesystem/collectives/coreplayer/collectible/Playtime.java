// Created by Eric B. 08.02.2021 20:24
package de.ericzones.bungeesystem.collectives.coreplayer.collectible;

public class Playtime {

    private final long previousPlaytime;
    private final long joinTime;

    private long totalIdleTime;
    private long currentIdleTime;

    public Playtime(Long previousPlaytime) {
        this.previousPlaytime = previousPlaytime;
        this.joinTime = System.currentTimeMillis();
    }

    public void setIdle() {
        if(currentIdleTime == 0)
            currentIdleTime = System.currentTimeMillis();
    }

    public void setActive() {
        if(currentIdleTime == 0)
            return;
        totalIdleTime += getCurrentIdleTime();
        currentIdleTime = 0;
    }

    public long getCurrentPlaytime() {
        return System.currentTimeMillis() - joinTime - totalIdleTime - getCurrentIdleTime();
    }

    public String getCurrentPlaytimeName() {
        long playtimeMillis = getCurrentPlaytime();
        long seconds = 0, minutes = 0, hours = 0, days = 0;
        while(playtimeMillis > 1000) {
            playtimeMillis-=1000;
            seconds++;
        }
        while(seconds > 60) {
            seconds-=60;
            minutes++;
        }
        while(minutes > 60) {
            minutes-=60;
            hours++;
        }
        while(hours > 24) {
            hours-=24;
            days++;
        }
        return days + "d " + hours + "h " + minutes + "m";
    }

    public long getTotalPlaytime() {
        return getCurrentPlaytime() + previousPlaytime;
    }

    public String getTotalPlaytimeName() {
        long playtimeMillis = getTotalPlaytime();
        long seconds = 0, minutes = 0, hours = 0, days = 0;
        while(playtimeMillis > 1000) {
            playtimeMillis-=1000;
            seconds++;
        }
        while(seconds > 60) {
            seconds-=60;
            minutes++;
        }
        while(minutes > 60) {
            minutes-=60;
            hours++;
        }
        while(hours > 24) {
            hours-=24;
            days++;
        }
        return days + "d " + hours + "h " + minutes + "m";
    }

    private long getCurrentIdleTime() {
        if(currentIdleTime != 0)
            return System.currentTimeMillis() - currentIdleTime;
        return 0;
    }

}
