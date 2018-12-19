package com.kaboomreport;

import android.os.AsyncTask;

/**
 * Async task that executes a worker and calls onSuccess with the result or onError with the exception.
 * @param <T> The type of the result returned by worker.
 */
public class AsyncWorkerTask<T> extends AsyncTask<Void, Void, T> {
    public interface Worker<T> {
        T getResult();
    }

    private final Worker<T> worker;
    private final OnSuccess<T> onSuccess;
    private final OnError onError;
    private Exception exception;

    public AsyncWorkerTask(Worker<T> worker, OnSuccess<T> onSuccess, OnError onError) {
        if (worker == null) {
            throw new IllegalArgumentException("worker");
        }
        if (onSuccess == null) {
            throw new IllegalArgumentException("onSuccess");
        }
        if (onError == null) {
            throw new IllegalArgumentException("onError");
        }

        this.worker = worker;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    @Override
    protected void onPostExecute(T result) {
        super.onPostExecute(result);
        if (exception != null) {
            onError.call(exception);
        } else {
            onSuccess.call(result);
        }
    }

    @Override
    protected T doInBackground(Void... params) {
        try {
            return worker.getResult();
        } catch (Exception e) {
            exception = e;
        }
        return null;
    }
}
