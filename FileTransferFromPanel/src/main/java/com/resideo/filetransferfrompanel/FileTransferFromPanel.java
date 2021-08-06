package com.resideo.filetransferfrompanel;


import com.resideo.mqttoverintentlib.IMQTTIntentReceiver;
import com.resideo.mqttoverintentlib.IMQTTReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class FileTransferFromPanel implements ITransferFileFromPanel, IMQTTReceiver {
    private final String TOPIC_GET_FILE_REQUEST = "@/GET/RESIDEO/MediaToCloud/RequestFile";
    private final String TOPIC_FILE_RESPONSE = "/RESIDEO/MediaToCloud/FileResponse";
    private final String TOPIC_FILE_RESPONSE_IDENTIFIER = "/Custom";
    private final String TOPIC_FILE_RESPONSE_REQUEST_ID_IDENTIFIER = "/+";
    private final String MAC_ADDRESS_HEADER = "@:";
    private final int QOS = 1;
    private String macAddress;
    private IMQTTIntentReceiver imqttIntentReceiver;
    private ISaveFile iSaveFile;
    private String destPath;
    private String srcPath;
    private int requestId = 0;
    private HashMap<Integer, CompletionHandler> completionHandlerHashMap;

    public FileTransferFromPanel(IMQTTIntentReceiver imqttIntentReceiver, String macAddress, ISaveFile iSaveFile) {
        this.iSaveFile = iSaveFile;
        this.imqttIntentReceiver = imqttIntentReceiver;
        this.macAddress = macAddress;
        this.imqttIntentReceiver.setReceiver(this);
        this.completionHandlerHashMap = new HashMap<>();
    }

    @Override
    public void getFromPanel(String srcPath, String destPath, CompletionHandler completionHandler) {
        completionHandlerHashMap.put(++requestId, completionHandler);
        this.destPath = destPath;
        this.srcPath = srcPath;
        imqttIntentReceiver.subscribe(getCustomFileResponseHeader(macAddress) + TOPIC_FILE_RESPONSE_REQUEST_ID_IDENTIFIER + TOPIC_FILE_RESPONSE, QOS);
        imqttIntentReceiver.publish(TOPIC_GET_FILE_REQUEST, buildRequestFileMessage(srcPath), QOS);
    }

    private String buildRequestFileMessage(String fileName) {
        return "{\"filename\":\"" + fileName + "\"," +
                "\"$uri\":\"" + getCustomFileResponseHeader(macAddress) + "/" + requestId + "\"}";
    }

    private String getCustomFileResponseHeader(String macAddress) {
        return MAC_ADDRESS_HEADER + macAddress + TOPIC_FILE_RESPONSE_IDENTIFIER;
    }

    @Override
    public void messageArrived(String topic, String message) {
        if(topic.contains(TOPIC_FILE_RESPONSE))
            handleBase64FileResponse(getRequestIdFromTopic(topic), message);
    }

    private int getRequestIdFromTopic(String topic) {
        String[] topicArray = topic.split("/");
        return Integer.parseInt(topicArray[2]);
    }

    protected void handleBase64FileResponse(int id, String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            String base64 = jsonObject.getString("data");
            String fileName = jsonObject.getString("filename");
            iSaveFile.convert(base64, destPath, fileName, completionHandlerHashMap.get(id));
        } catch (JSONException e) {
            completionHandlerHashMap.get(id).onFailed(e.toString());
        }
    }
}
