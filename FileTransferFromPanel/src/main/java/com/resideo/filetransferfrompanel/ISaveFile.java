package com.resideo.filetransferfrompanel;


public interface ISaveFile {
    void convert(String encodedString, String destPath, String fileName, CompletionHandler completionHandler);
}
