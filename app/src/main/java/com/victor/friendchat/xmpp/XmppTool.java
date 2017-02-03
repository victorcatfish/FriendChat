package com.victor.friendchat.xmpp;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.victor.friendchat.R;
import com.victor.friendchat.domain.XmppUser;
import com.victor.friendchat.uitl.LogUtils;
import com.victor.friendchat.uitl.SharedPreferencesUtil;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.OfflineMessageManager;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.bytestreams.ibb.provider.CloseIQProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.OpenIQProvider;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.search.UserSearchManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.R.attr.name;

/**
 * Created by Victor on 2016/12/26.
 */
public class XmppTool {

    private String tag = "XmppTool";
    private static XmppTool sXmppTool;
    public static final String HOST = "192.168.2.103";
    public static final int PORT = 5222;
    private static XMPPConnection sConn;
    private Context mContext;
    public List<Message> mOffMsgs;

    public enum STATUS {
        ONLINE,
        QME,
        BUSY,
        NOBOTHER,
        LEAVE,
        INVISIBLE
    }


    public static XmppTool getInstance() {
        if (sXmppTool == null) {
            synchronized (XmppTool.class) {
                if (sXmppTool == null) {
                    sXmppTool = new XmppTool();
                }
            }
        }
        return sXmppTool;
    }

    private XmppTool() {
        configure(ProviderManager.getInstance());
        ConnectionConfiguration connConfig = new ConnectionConfiguration(HOST, PORT, "");
        connConfig.setSASLAuthenticationEnabled(false);
        //设置安全类型
        connConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        connConfig.setSendPresence(false);
        sConn = new XMPPConnection(connConfig);
        LogUtils.sf("-------------------创建连接-----------------");

        sConn.DEBUG_ENABLED = true;
        SmackConfiguration.setPacketReplyTimeout(30000);// 设置超时时间
        SmackConfiguration.setKeepAliveInterval(-1);
        SmackConfiguration.setDefaultPingInterval(0);

        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    if (sConn.isConnected()) {// 首先判断是否还连接着服务器，需要先断开
                        try {
                            sConn.disconnect();
                        } catch (Exception e) {
                            LogUtils.i(tag, "conn.disconnect() failed: " + e);
                        }
                    }
                    sConn.connect();


                } catch (XMPPException e) {
                    LogUtils.e(tag, Log.getStackTraceString(e));
                }
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        sConn.addConnectionListener(new ConnectionListener() {

            @Override
            public void reconnectionSuccessful() {
                // TODO Auto-generated method stub
                LogUtils.i(tag, "重连成功");
            }

            @Override
            public void reconnectionFailed(Exception arg0) {
                // TODO Auto-generated method stub
                LogUtils.i(tag, "重连失败");
                //                    User mUser = SaveUserUtil.loadAccount(context);
                //                    login(mUser.getUser(), mUser.getPassword());


            }

            @Override
            public void reconnectingIn(int arg0) {
                // TODO Auto-generated method stub
                LogUtils.i(tag, "重连中");
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                // TODO Auto-generated method stub
                LogUtils.i(tag, "连接出错");
                if (e.getMessage().contains("conflict")) {
                    LogUtils.i(tag, "被挤掉了");
                    disConnectServer();

                }
                //                    User mUser = SaveUserUtil.loadAccount(context);
                //                    login(mUser.getUser(), mUser.getPassword());
            }

            @Override
            public void connectionClosed() {
                // TODO Auto-generated method stub
                LogUtils.i(tag, "连接关闭");
            }
        });

    }

    /**
     * 是否与服务器连接上
     *
     * @return
     */
    public boolean isConnection() {
        if (sConn != null) {
            return (sConn.isConnected() && sConn.isAuthenticated());
        }
        return false;
    }

    public boolean login(String user, String password, Context context) {
        try {
            sConn.login(user, password);
            // 获取离线消息
            OfflineMessageManager offlineMsgManager = new OfflineMessageManager(sConn);
            getOfflineMessage(offlineMsgManager);
            Presence presence = new Presence(Presence.Type.available);
            sConn.sendPacket(presence);
            int ordinal = SharedPreferencesUtil.getInt(context, "status", name + "status");
            STATUS status = STATUS.values()[ordinal];
            setPresence(status);
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.e("MessagingService", "Already Logged in as " + sConn.getUser());
            return true;
        }
        return false;
    }

    /**
     * 获取离线消息
     * @param manager
     */
    public void getOfflineMessage(OfflineMessageManager manager){

        if (manager != null){
            try {
                //int num = manager.getMessageCount();
                mOffMsgs = new ArrayList<>();
                Iterator<Message> it = manager.getMessages();
                while (it.hasNext()) {
                    Message msg = it.next();
                    mOffMsgs.add(msg);
                }
                manager.deleteMessages();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 添加好友
     *
     * @param
     * @param userName
     * @param name
     * @param groupName 是否有分组
     * @return
     */
    public boolean addUser(String userName, String name, String groupName) {
        Roster roster = sConn.getRoster();
        try {
            roster.createEntry(userName, name, null == groupName ? null
                    : new String[]{groupName});
            return true;
        } catch (XMPPException e) {
            LogUtils.e(tag, Log.getStackTraceString(e));
        }
        return false;
    }

    /**
     * 断开连接
     */
    public static void disConnectServer() {
        if (null != sConn && sConn.isConnected()) {

            new Thread() {
                public void run() {
                    sConn.disconnect();
                    sConn = null;
                }
            }.start();
        }
        if (sXmppTool != null) {
            sXmppTool = null;
        }

    }

    /**
     * 删除好友
     *
     * @param userName
     * @return
     */
    public boolean removeUser(String userName) {
        Roster roster = sConn.getRoster();
        try {
            RosterEntry entry = roster.getEntry(userName);
            if (null != entry) {
                roster.removeEntry(entry);
            }
            return true;
        } catch (XMPPException e) {
            LogUtils.e(tag, Log.getStackTraceString(e));
        }
        return false;
    }


    /**
     * 判断是否是好友
     *
     * @param
     * @param user
     * @return
     */
    public boolean isFriendly(String user) {


        Roster roster = getConn().getRoster();
        List<RosterEntry> list = new ArrayList<RosterEntry>();
        list.addAll(roster.getEntries());
        for (int i = 0; i < list.size(); i++) {
            Log.i("xmppttttttttt", list.get(i).getUser().toUpperCase() + "\t" + user);
            if (list.get(i).getUser().contains(user.toLowerCase())) {
                if (list.get(i).getType().toString().equals("both")) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;

    }

    /**
     * 添加到分组
     *
     * @param
     * @param userName
     * @param groupName
     */
    public void addUserToGroup(String userName, String groupName) {
        Roster roster = sConn.getRoster();
        RosterGroup group = roster.getGroup(groupName);
        if (null == group) {
            group = roster.createGroup(groupName);
        }
        RosterEntry entry = roster.getEntry(userName);
        if (entry != null) {
            try {
                group.addEntry(entry);
            } catch (XMPPException e) {
                LogUtils.e(tag, Log.getStackTraceString(e));
            }
        }

    }


    /**
     * 获取所有分组
     *
     * @param
     * @return
     */
    public List<RosterGroup> getGroups() {
        Roster roster = getConn().getRoster();
        List<RosterGroup> list = new ArrayList<RosterGroup>();
        list.addAll(roster.getGroups());
        return list;
    }

    /**
     * 获取某一个分组的成员
     *
     * @param
     * @param groupName
     * @return
     */
    public List<RosterEntry> getEntrysByGroup(String groupName) {

        Roster roster = getConn().getRoster();
        List<RosterEntry> list = new ArrayList<RosterEntry>();
        RosterGroup group = roster.getGroup(groupName);
        Collection<RosterEntry> rosterEntiry = group.getEntries();
        Iterator<RosterEntry> iter = rosterEntiry.iterator();
        while (iter.hasNext()) {
            RosterEntry entry = iter.next();
            LogUtils.i("xmpptool", entry.getUser() + "\t" + entry.getName() + entry.getType().toString());
            if (entry.getType().toString().equals("both")) {
                list.add(entry);
            }

        }
        return list;

    }

    /**
     * 修改密码
     *
     * @param pwd
     * @return
     */
    public boolean changePassword(String pwd) {
        try {
            sConn.getAccountManager().changePassword(pwd);
            return true;
        } catch (XMPPException e) {
            LogUtils.e(tag, Log.getStackTraceString(e));
        }
        return false;
    }

    /**
     * 设置状态
     *
     * @param state
     */
    public void setPresence(STATUS state) {
        Presence presence;
        switch (state) {
            //0.在线 1.Q我吧 2.忙碌 3.勿扰 4.离开 5.隐身 6.离线
            case ONLINE:
                presence = new Presence(Presence.Type.available);
                sConn.sendPacket(presence);
                LogUtils.e(tag, "设置在线");
                break;
            case QME:
                presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.chat);
                sConn.sendPacket(presence);
                LogUtils.e(tag, "Q我吧");
                LogUtils.e(tag, presence.toXML());
                break;
            case BUSY:
                presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.dnd);
                sConn.sendPacket(presence);
                LogUtils.e(tag, "忙碌");
                LogUtils.e(tag, presence.toXML());
                break;
            case NOBOTHER:
                presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.xa);
                sConn.sendPacket(presence);
                LogUtils.e(tag, "勿扰");
                LogUtils.e(tag, presence.toXML());
                break;
            case LEAVE:
                presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.away);
                sConn.sendPacket(presence);
                LogUtils.e(tag, "离开");
                LogUtils.e(tag, presence.toXML());
                break;
            case INVISIBLE:
                Roster roster = sConn.getRoster();
                Collection<RosterEntry> entries = roster.getEntries();
                for (RosterEntry entity : entries) {
                    presence = new Presence(Presence.Type.unavailable);
                    presence.setPacketID(Packet.ID_NOT_AVAILABLE);
                    presence.setFrom(sConn.getUser());
                    presence.setTo(entity.getUser());
                    sConn.sendPacket(presence);
                    LogUtils.e(tag, presence.toXML());
                }
                LogUtils.e(tag, "告知其他用户-隐身");

                break;
            //            case 6:
            //                presence = new Presence(Presence.Type.unavailable);
            //                con.sendPacket(presence);
            //                LogUtils.e(tag, "离线");
            //                LogUtils.e(tag, presence.toXML());
            //                break;
            //            default:
            //                break;
        }
    }

    public void setPresence(ImageView iv, ImageView iv_me, Context context, String name) {

        int status = SharedPreferencesUtil.getInt(context, "status", name + "status");
        switch (status) {
            //0.在线 1.Q我吧 2.忙碌 3.勿扰 4.离开 5.隐身 6.离线
            case 0:
                iv.setImageResource(R.mipmap.status_online);
                iv_me.setImageResource(R.mipmap.status_online);
                break;
            case 1:
                iv.setImageResource(R.mipmap.status_qme);
                iv_me.setImageResource(R.mipmap.status_qme);
                break;
            case 2:
                iv.setImageResource(R.mipmap.status_busy);
                iv_me.setImageResource(R.mipmap.status_busy);
                break;
            case 3:
                iv.setImageResource(R.mipmap.status_shield);
                iv_me.setImageResource(R.mipmap.status_shield);
                break;
            case 4:
                iv.setImageResource(R.mipmap.status_leave);
                iv_me.setImageResource(R.mipmap.status_leave);
                break;
            case 5:
                iv.setImageResource(R.mipmap.status_invisible);
                iv_me.setImageResource(R.mipmap.status_invisible);
                break;

        }
    }

    /**
     * 查找用户
     *
     * @param
     * @param userName
     * @return
     */
    public List<XmppUser> searchUsers(String userName) {
        List<XmppUser> list = new ArrayList<XmppUser>();
        UserSearchManager userSearchManager = new UserSearchManager(sConn);
        try {
            Form searchForm = userSearchManager.getSearchForm("search."
                    + sConn.getServiceName());
            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("Username", true);
            answerForm.setAnswer("Name", true);
            answerForm.setAnswer("search", userName);
            ReportedData data = userSearchManager.getSearchResults(answerForm,
                    "search." + sConn.getServiceName());
            Iterator<ReportedData.Row> rows = data.getRows();
            while (rows.hasNext()) {
                XmppUser user = new XmppUser(null, null);
                ReportedData.Row row = rows.next();
                user.userName = row.getValues("Username").next().toString();
                user.name = row.getValues("Name").next().toString();
                list.add(user);
            }
        } catch (XMPPException e) {
            LogUtils.e(tag, Log.getStackTraceString(e));
        }
        return list;
    }

    public XMPPConnection getConn() {
        if (!sConn.isConnected()) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        sConn.connect();
                        LogUtils.sf("-------------创建连接--------------------");
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                }
            };
            try {
                thread.start();
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        return sConn;
    }

    /**
     * 获取离线消息
     */
    private void getMessage() {
        OfflineMessageManager offlineManager = new OfflineMessageManager(getConn());
        try {
            Iterator<org.jivesoftware.smack.packet.Message> it = offlineManager
                    .getMessages();
            Log.i("service", offlineManager.supportsFlexibleRetrieval() + "");
            Log.i("service", "离线消息数量: " + offlineManager.getMessageCount());
            Map<String, ArrayList<Message>> offlineMsgs = new HashMap<String, ArrayList<Message>>();
            while (it.hasNext()) {
                org.jivesoftware.smack.packet.Message message = it.next();
                Log.i("service", "收到离线消息, Received from 【" + message.getFrom()
                        + "】 message: " + message.getBody());
                String fromUser = message.getFrom().split("/")[0];
                if (offlineMsgs.containsKey(fromUser)) {
                    offlineMsgs.get(fromUser).add(message);
                } else {
                    ArrayList<Message> temp = new ArrayList<Message>();
                    temp.add(message);
                    offlineMsgs.put(fromUser, temp);
                }
            }
            //在这里进行处理离线消息集合......
            Set<String> keys = offlineMsgs.keySet();
            Iterator<String> offIt = keys.iterator();
            while (offIt.hasNext()) {
                String key = offIt.next();
                ArrayList<Message> ms = offlineMsgs.get(key);

                for (int i = 0; i < ms.size(); i++) {
                    Log.i("serviceeeeeeeeeeeee", "收到离线消息, Received from 【" + ms.get(i).getFrom()
                            + "】 message: " + ms.get(i).getBody());
                }
            }
            offlineManager.deleteMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * 对Provider进行配置
     * @param pm
     */
    public void configure(ProviderManager pm) {

        try {
            Class.forName("org.jivesoftware.smack.ReconnectionManager");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Private Data Storage
        pm.addIQProvider("query", "jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());

        // Time
        try {
            pm.addIQProvider("query", "jabber:iq:time", Class.forName("org.jivesoftware.smackx.packet.Time"));
        } catch (ClassNotFoundException e) {
            Log.w("TestClient", "Can't load class for org.jivesoftware.smackx.packet.Time");
        }

        // Roster Exchange
        pm.addExtensionProvider("x", "jabber:x:roster", new RosterExchangeProvider());

        // Message Events
        pm.addExtensionProvider("x", "jabber:x:event", new MessageEventProvider());

        // Chat State
        pm.addExtensionProvider("active", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("composing", "http://jabber.org/protocol/chatstates",
                new ChatStateExtension.Provider());
        pm.addExtensionProvider("paused", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("inactive", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("gone", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());

        // XHTML
        pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im", new XHTMLExtensionProvider());

        // Group Chat Invitations
        pm.addExtensionProvider("x", "jabber:x:conference", new GroupChatInvitation.Provider());

        // Service Discovery # Items
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());

        // Service Discovery # Info
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());

        // Data Forms
        pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

        // MUC User
        pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());

        // MUC Admin
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin", new MUCAdminProvider());

        // MUC Owner
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());

        // Delayed Delivery
        pm.addExtensionProvider("x", "jabber:x:delay", new DelayInformationProvider());

        // Version
        try {
            pm.addIQProvider("query", "jabber:iq:version", Class.forName("org.jivesoftware.smackx.packet.Version"));
        } catch (ClassNotFoundException e) {
            // Not sure what's happening here.
        }

        // VCard
        pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

        // Offline Message Requests
        pm.addIQProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());

        // Offline Message Indicator
        pm.addExtensionProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());

        // Last Activity
        pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

        // User Search
        pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

        // SharedGroupsInfo
        pm.addIQProvider("sharedgroup", "http://www.jivesoftware.org/protocol/sharedgroup",
                new SharedGroupsInfo.Provider());

        // JEP-33: Extended Stanza Addressing
        pm.addExtensionProvider("addresses", "http://jabber.org/protocol/address", new MultipleAddressesProvider());

        // FileTransfer
        pm.addIQProvider("si", "http://jabber.org/protocol/si", new StreamInitiationProvider());
        pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
        pm.addIQProvider("open", "http://jabber.org/protocol/ibb", new OpenIQProvider());
        pm.addIQProvider("close", "http://jabber.org/protocol/ibb", new CloseIQProvider());
        pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb", new DataPacketProvider());

        // Privacy
        pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
        pm.addIQProvider("command", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider());
        pm.addExtensionProvider("malformed-action", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.MalformedActionError());
        pm.addExtensionProvider("bad-locale", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadLocaleError());
        pm.addExtensionProvider("bad-payload", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadPayloadError());
        pm.addExtensionProvider("bad-sessionid", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadSessionIDError());
        pm.addExtensionProvider("session-expired", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.SessionExpiredError());
    }
}
