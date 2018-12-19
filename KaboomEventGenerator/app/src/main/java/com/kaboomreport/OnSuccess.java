package com.kaboomreport;

public interface OnSuccess<T> {
    void call(T result);
}
