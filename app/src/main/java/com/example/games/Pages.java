package com.example.games;
//42 v podstate len drží pokope linky nasledujúcej strany a predošlej (čo mi bolo aj tak na hovno)
public class Pages {
    private String next;
    private String previous;

    public Pages(String next, String previous) {
        this.next = next;
        this.previous = previous;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }
}
