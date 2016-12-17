package com.github.nizshee.ui;


@SuppressWarnings("all")
public class Result {
    public final int request;
    public final int sort;
    public final int client;

    public Result(int request, int sort, int client) {
        this.request = request;
        this.sort = sort;
        this.client = client;
    }
}
