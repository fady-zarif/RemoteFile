package com.resideo.filetransferfrompanel;


public interface ITransferFileFromPanel {
    void getFromPanel(String srcPath, String destPath, CompletionHandler completionHandler);
}
