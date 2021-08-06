package com.resideo.filetransferfrompanel;

import com.resideo.mqttoverintentlib.IMQTTIntentReceiver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class FileTransferFromPanelTest {
    private IMQTTIntentReceiver imqttIntentReceiver;
    private final String macAddress = "000af5977508";
    private ISaveFile iSaveFile;
    private FileTransferFromPanel unitUnderTest;

    @Before
    public void setUp() {
        imqttIntentReceiver = Mockito.mock(IMQTTIntentReceiver.class);
        iSaveFile = Mockito.mock(ISaveFile.class);
        unitUnderTest = new FileTransferFromPanel(imqttIntentReceiver, macAddress, iSaveFile);
    }

    @Test
    public void getFromPanel_shouldPublishMQTTFileRequest() {
        String srcPath = "/honeywell/runtime/voicetoken/customVoice.mp3";
        String destPath = "/sdcard/honeywell/voicetoken/";
        CompletionHandler completionHandler = Mockito.mock(CompletionHandler.class);

        unitUnderTest.getFromPanel(srcPath, destPath, completionHandler);

        Mockito.verify(imqttIntentReceiver).publish("@/GET/RESIDEO/MediaToCloud/RequestFile",
                "{\"filename\":\"" + srcPath + "\"," +
                        "\"$uri\":\"@:" + macAddress+"/Custom/1" + "\"}", 1);
    }

    @Test
    public void onMessageReceived_shouldInvokeIConvert_WhenReceivedValidFileResponse() {
        String topic = "@:000af5977508/Custom/1/RESIDEO/MediaToCloud/FileResponse";
        String data = "iVBORw0KGgoAAAANSUhEUgAABgwAAAFaCAYAAADCYHb6AAAABGdBTUEAALGPC";
        String message = "{\"crc\":\"0\",\"data\":\"" + data + "\",\"filename\":\"customVoice.mp3\",\"filesize\":46732,\"format\":\"base64\"}";
        String srcPath = "/honeywell/runtime/voicetoken/customVoice.mp3";
        String destPath = "/sdcard/honeywell/voicetoken/";
        CompletionHandler completionHandler = Mockito.mock(CompletionHandler.class);
        unitUnderTest.getFromPanel(srcPath, destPath, completionHandler);

        unitUnderTest.messageArrived(topic, message);

        Mockito.verify(iSaveFile, Mockito.times(1)).convert(data, destPath, "customVoice.mp3", completionHandler);
    }

    @Test
    public void onMessageReceived_shouldInvokeOnCompletionFailed_WhenReceivedNotValidFileResponse() {
        String topic = "@:000af5977508/Custom/1/RESIDEO/MediaToCloud/FileResponse";
        String message = "FileNotFound!";
        String srcPath = "/honeywell/runtime/voicetoken/customVoice.mp3";
        String destPath = "/sdcard/honeywell/voicetoken/";
        CompletionHandler completionHandler = Mockito.mock(CompletionHandler.class);
        unitUnderTest.getFromPanel(srcPath, destPath, completionHandler);

        unitUnderTest.messageArrived(topic, message);

        Mockito.verify(completionHandler, Mockito.times(1)).onFailed(Mockito.anyString());
    }
}