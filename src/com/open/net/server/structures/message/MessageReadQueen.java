package com.open.net.server.structures.message;

import com.open.net.server.structures.AbstractClient;

import java.util.HashMap;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   读队列
 */

public final class MessageReadQueen {

    public MessageBuffer   mReadMessageBuffer  = new MessageBuffer();
    public HashMap<Long,Message> mMessageMap = new HashMap<>(1024);//真正的消息队列

    public Message build(byte[] src , int offset , int length){
        Message msg = mReadMessageBuffer.build(src,offset,length);
        return msg;
    }

    public void put(AbstractClient client, Message msg){
        mMessageMap.put(msg.msgId,msg);
        client.addReadMessageId(msg.msgId);
    }

    public void remove(AbstractClient mClient, Message msg){
        mMessageMap.remove(msg.msgId);
        mClient.removeReadMessageId(msg.msgId);

        mReadMessageBuffer.release(msg);
    }
}
