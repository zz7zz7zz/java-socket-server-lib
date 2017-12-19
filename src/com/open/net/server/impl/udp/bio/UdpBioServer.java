package com.open.net.server.impl.udp.bio;

import com.open.net.server.impl.udp.bio.processor.UdpBioReadProcessor;
import com.open.net.server.impl.udp.bio.processor.UdpBioWriteProcessor;
import com.open.net.server.structures.AbstractMessageProcessor;
import com.open.net.server.structures.ServerConfig;
import com.open.net.server.structures.ServerLock;

import java.io.IOException;

/**
 * author       :   Administrator
 * created on   :   2017/12/6
 * description  :
 */

public class UdpBioServer {

    private ServerLock mServerLock;
    private UdpBioReadProcessor mSocketReadProcessor;
    private UdpBioWriteProcessor mSocketWRProcessor;

    public UdpBioServer(ServerConfig mServerInfo ,AbstractMessageProcessor mMessageProcessor) throws IOException {
        this.mServerLock = new ServerLock();
        this.mSocketReadProcessor   = new UdpBioReadProcessor(mServerInfo,mServerLock,mMessageProcessor);
        this.mSocketWRProcessor     = new UdpBioWriteProcessor(mMessageProcessor);
    }

    public void start(){

        Thread mRwProcessorThread       = new Thread(this.mSocketWRProcessor);
        Thread mRProcessorThread       = new Thread(this.mSocketReadProcessor);

        mRwProcessorThread.start();
        mRProcessorThread.start();

        mServerLock.waitEnding();
    }

}
