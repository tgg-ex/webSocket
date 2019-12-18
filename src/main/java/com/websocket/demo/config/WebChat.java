package com.websocket.demo.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * websocket
 *
 * @author zyz
 */
@ServerEndpoint("/webSocket/{senderId}") // 该注解用来指定一个URI，客户端可以通过这个URI来连接到WebSocket。类似Servlet的注解mapping。无需在web.xml中配置。
@Component
public class WebChat {
    // 用来存放每个客户端对应的ChatAnnotation对象，实现服务端与单一客户端通信的话，使用Map来存放，其中Key可以为用户标识，hashtable比hashmap线程安全
    private static Map<String, WebChat> webSocketMap = new Hashtable();
    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    /**
     * 连接建立成功调用的方法
     *
     * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(@PathParam(value = "senderId") String senderId, Session session) {
        this.session = session;
        webSocketMap.put(senderId, this);//加入map中
        System.out.println(senderId + "连接加入！当前在线人数为" + getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam(value = "senderId") String userId) {
        webSocketMap.remove(userId);
        System.out.println(userId + "关闭连接！当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(@PathParam(value = "senderId") String senderId, String message, Session session) {
        // 群发消息
        try {
            //将前端发送的 JSON 字符串转换为 JSON 对象
            JSONObject jsonMessge = JSON.parseObject(message);
            //获取接收者ID列表
            JSONArray list = jsonMessge.getJSONArray("userList");
            //获取发送者的聊天对象
            WebChat userMap = webSocketMap.get(senderId);
            //获取发送的消息
            String mess = jsonMessge.getString("mess");
            //为自己发送一条消息
            userMap.sendMessage(mess);
            //遍历消息接受者列表
            for (Object receiverId : list) {
                //获取消息接受者
                WebChat receiver = webSocketMap.get(receiverId);
                //调用session的发送消息方法  将消息发送到客户端
                receiver.sendMessage(mess);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(@PathParam(value = "userId") String userId, Session session, Throwable error) {
        System.out.println(userId + "发生错误");
        error.printStackTrace();
    }

    /**
     * 发送消息
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 发送文件
     *
     * @throws IOException
     */
    public void sendFile(File file) throws IOException {
        this.session.getAsyncRemote().sendObject(file);
    }

    public static synchronized int getOnlineCount() {
        return webSocketMap.size();
    }

}

