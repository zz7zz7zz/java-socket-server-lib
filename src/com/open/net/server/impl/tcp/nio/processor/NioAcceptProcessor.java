package com.open.net.server.impl.tcp.nio.processor;

import com.open.net.server.impl.tcp.nio.NioClient;
import com.open.net.server.object.AbstractServerMessageProcessor;
import com.open.net.server.object.ServerConfig;
import com.open.net.server.object.ServerLock;
import com.open.net.server.object.ServerLog;
import com.open.net.server.pools.ClientsPool;
import com.open.net.server.utils.ExceptionUtil;
import com.open.net.server.utils.TextUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :  客户连接处理类
 */

public final class NioAcceptProcessor implements Runnable
{
	public static String TAG = "NioAcceptProcessor";
	
    private ServerConfig mServerInfo;
    private ServerLock mServerLock;
    private ConcurrentLinkedQueue<NioClient> mClientQueen;
    public AbstractServerMessageProcessor mMessageProcessor;

    public NioAcceptProcessor(ServerConfig mServerInfo, ServerLock mServerLock, ConcurrentLinkedQueue<NioClient> mClientQueen,AbstractServerMessageProcessor mMessageProcessor) {
        this.mServerInfo = mServerInfo;
        this.mServerLock = mServerLock;
        this.mClientQueen = mClientQueen;
        this.mMessageProcessor = mMessageProcessor;
    }

    public void run() {

        try {
            ServerSocketChannel mServerSocket = ServerSocketChannel.open();

            if(mServerInfo.port > 0 ){
                if(mServerInfo.connect_backlog > 0 ){
                    if(!TextUtils.isEmpty(mServerInfo.host)){
                        mServerSocket.bind(new InetSocketAddress(mServerInfo.host,mServerInfo.port),mServerInfo.connect_backlog);
                    }else{
                        mServerSocket.bind(new InetSocketAddress(mServerInfo.port),mServerInfo.connect_backlog);
                    }
                }else {
                    mServerSocket.bind(new InetSocketAddress(mServerInfo.port));
                }
            }else{

                while (true){
                    try {
                        int port = (int)(1024+Math.random()*(0xffff-1024+1));
                        mServerSocket.bind(new InetSocketAddress(port));
                        break;
                    }catch (Exception e){
                        e.printStackTrace();
                        continue;
                    }
                }
            }

            while (true){
                SocketChannel mSocketClient = mServerSocket.accept();
                if(null != mSocketClient){
                	
                    String [] clientInfo = mSocketClient.getRemoteAddress().toString().replace("/", "").split(":");
                    String mHost = clientInfo[0];
                    int mPort = Integer.valueOf(clientInfo[1]);
                    
                    NioClient mClient = (NioClient) ClientsPool.get();
                    if(null != mClient){
                        mClient.init(mHost,mPort,mSocketClient,mMessageProcessor);
                        mClientQueen.add(mClient);
                    }else{
                    	ServerLog.getIns().log(TAG, "accept client success but ClientsPool.get() null Host "+ mHost + " port " + mPort );
                    }
                }else{
                	ServerLog.getIns().log(TAG, "accept client null");
                }
            }
        } catch (IOException e) {
        	ServerLog.getIns().log(TAG, ExceptionUtil.getStackTraceString(e));
        }

        mServerLock.notifytEnding();
    }
}