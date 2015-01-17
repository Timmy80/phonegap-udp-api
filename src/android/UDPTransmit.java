/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

// This plugin sends UDP packets.

package edu.uic.udptransmit;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class UDPTransmit extends CordovaPlugin {
    
    DatagramSocket datagramSocket;
    
    CallbackContext receivingCallbackContext = null;
    
    // Constructor
    public UDPTransmit() {
    }
    
    // Handles and dispatches "exec" calls from the JS interface (udptransmit.js)
    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if("initialize".equals(action)) {
            final int port = args.getInt(0);
            // Run the UDP transmitter initialization on its own thread (just in case, see sendMessage comment)
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    this.initialize(port, callbackContext);
                }
                private void initialize(int port, CallbackContext callbackContext) {
                    // create socket
                    try {
                        datagramSocket = new DatagramSocket(port);
                        callbackContext.success("Success initializing UDP transmitter using datagram socket");
                        
                    } catch (SocketException e) {
                        callbackContext.error("Error initializing UDP transmitter using datagram socket");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
            return true;
        }
        else if("sendMessage".equals(action)) {
            final String host = args.getString(0);
            final int destPort = Integer.parseInt(args.getString(1));
            final String message = args.getString(2);
            // Run the UDP transmission on its own thread (it fails on some Android environments if run on the same thread)
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try{
                        final InetAddress addr = getHostAddress(host);
                        this.sendMessage(addr,destPort,message, callbackContext);
                    }
                    catch (UnknownHostException e) {
                        callbackContext.error("Error: Unknown Host");
                        e.printStackTrace();
                    }
                    catch (IOException e){
                        callbackContext.error("Error: fail to transmit UDP packet");
                        e.printStackTrace();
                    }
                }
                private InetAddress getHostAddress(String hostStr) throws UnknownHostException{
                    return InetAddress.getByName(hostStr);
                }
                private void sendMessage(InetAddress addr,int destPort,String data, CallbackContext callbackContext) throws IOException {
                    byte[] bytes = data.getBytes();
                    DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length,addr, destPort);
                    datagramSocket.send(datagramPacket);
                    callbackContext.success("Success transmitting UDP packet");
                }
            });
            return true;
        }
        else if("onReceive".equals(action)) {
            receivingCallbackContext = callbackContext; // save the callback context
            
            // execute asynchronous task
            cordova.getThreadPool().execute(new Runnable(){
                public void run(){
                    while(true){
                        try {
                            JSONObject json  = new JSONObject();
                            byte[] receiveData = new byte[1024];
                            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            
                            datagramSocket.receive(receivePacket);
                            
                            String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                            InetAddress sender = receivePacket.getAddress();
                            Integer senderPort = receivePacket.getPort();
                            
                            json.put("message", message);
                            json.put("sender", sender.toString());
                            json.put("senderPort", senderPort.toString());
                            
                            
                            PluginResult result = new PluginResult(PluginResult.Status.OK, json.toString());
                            result.setKeepCallback(true);           // keep callback after this call
                            receivingCallbackContext.sendPluginResult(result);
                        }
                        catch(JSONException e){
                            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Error: fail to create json object");
                            result.setKeepCallback(true);           // keep callback after this call
                            receivingCallbackContext.sendPluginResult(result);
                            e.printStackTrace();
                        }
                        catch(IOException e){
                            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Error: fail to receive UDP packet");
                            result.setKeepCallback(true);           // keep callback after this call
                            receivingCallbackContext.sendPluginResult(result);
                            e.printStackTrace();
                        }
                    }
                }
            });
            
            // create a plugin result with no result argument
            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true); // keep callback in order to call it later
            this.receivingCallbackContext.sendPluginResult(result);
            return true;
        }
        return false;
    }
}