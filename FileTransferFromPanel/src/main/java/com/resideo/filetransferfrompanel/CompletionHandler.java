package com.resideo.filetransferfrompanel;

public interface CompletionHandler {

    void onSuccess(String filePath);

    void onFailed(String errorMessage);
}
