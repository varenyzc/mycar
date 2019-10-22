package io.varenyzc.mycar.utils;

public interface ICallback {
    abstract  void callback(boolean reqSuccess, String statusCode, String data);
}
