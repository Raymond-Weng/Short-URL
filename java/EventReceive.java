package com.raymondweng.demo;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class EventReceive implements EventListener {
    @Override
    public void onEvent(GenericEvent genericEvent) {
        if (genericEvent instanceof MessageReceivedEvent && ((MessageReceivedEvent) genericEvent).isFromGuild() && !((MessageReceivedEvent) genericEvent).getMember().getId().equals(genericEvent.getJDA().getSelfUser().getId())) {
            try {
                new URL(((MessageReceivedEvent) genericEvent).getMessage().getContentRaw());
            } catch (MalformedURLException e) {
                ((MessageReceivedEvent) genericEvent).getMessage().reply("Invalid URL").queue();
                return;
            }
            try {
                Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
                String id = getNewId();
                PreparedStatement ps = connection.prepareStatement("INSERT INTO LINKS (KEY, LINK) VALUES (?, ?)");
                ps.setString(1, id);
                ps.setString(2, ((MessageReceivedEvent) genericEvent).getMessage().getContentRaw());
                ps.executeUpdate();
                ps.close();
                connection.close();
                ((MessageReceivedEvent) genericEvent).getMessage().reply("https://link.rwc.us.kg/" + id).queue();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (genericEvent instanceof GuildVoiceUpdateEvent) {
            if (((GuildVoiceUpdateEvent) genericEvent).getChannelLeft() != null) {
                List<String> deleteProtect = Arrays.asList("1315302736333639811");
                if (!deleteProtect.contains(((GuildVoiceUpdateEvent) genericEvent).getChannelLeft().getId()) && ((GuildVoiceUpdateEvent) genericEvent).getChannelLeft().getMembers().isEmpty()) {
                    ((GuildVoiceUpdateEvent) genericEvent).getChannelLeft().delete().queue();
                }
            }
            if (((GuildVoiceUpdateEvent) genericEvent).getChannelJoined() != null) {
                if (((GuildVoiceUpdateEvent) genericEvent).getChannelJoined().getId().equals("1315302736333639811")) {
                    genericEvent.getJDA()
                            .getCategoryById("1315210365386227714")
                            .createVoiceChannel(((GuildVoiceUpdateEvent) genericEvent).getMember().getUser().getEffectiveName() + "的語音頻道")
                            .queue(channel -> {
                                channel.getManager().putMemberPermissionOverride(Long.parseLong(((GuildVoiceUpdateEvent) genericEvent).getMember().getId()), List.of(Permission.VIEW_CHANNEL), Collections.emptyList()).queue();
                                ((GuildVoiceUpdateEvent) genericEvent).getGuild().moveVoiceMember(((GuildVoiceUpdateEvent) genericEvent).getMember(), channel).queue();
                            });
                }
            }
        }
    }

    public String getNewId() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/keys.db");
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM KEYS");
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        st.close();
        if(count < 50){
            st = connection.createStatement();
            rs = st.executeQuery("SELECT KEY FROM KEYS ORDER BY ID DESC LIMIT 1");
            rs.next();
            String id = rs.getString(1);
            rs.close();
            st.close();
            int[] now = stringToIntArray(id);
            for(int i = 0; i < 100-count; i++){
                now = next(now, now.length-1);
                st = connection.createStatement();
                st.execute("INSERT INTO KEYS (KEY) VALUES (\"" + intArrayToString(now) + "\")");
                st.close();
            }
            count = 100;
        }
        st = connection.createStatement();
        rs = st.executeQuery("SELECT * FROM (SELECT * FROM KEYS ORDER BY ID ASC LIMIT " + new Random().nextInt(count-1) + ") ORDER BY ID DESC LIMIT 1");
        rs.next();
        int id = rs.getInt(1);
        String key = rs.getString(2);
        rs.close();
        st.close();
        st = connection.createStatement();
        st.execute("DELETE FROM KEYS WHERE ID = " + id);
        st.close();
        connection.close();
        return key;
    }

    private int[] stringToIntArray(String s) {
        int[] array = new int[s.length()];
        for (int i = 0; i < array.length; i++) {
            array[i] = s.charAt(i);
        }
        return array;
    }

    private int[] next(int[] array, int pos) {
        if(pos < 0){
            int[] newArray = new int[array.length + 1];
            newArray[0] = 'A';
            System.arraycopy(array, 0, newArray, 1, array.length);
            return newArray;
        }
        array[pos]++;
        if(array[pos] > 'z'){
            array[pos] = 'A';
            return next(array, pos-1);
        } else if (array[pos] == 'Z'+1) {
            array[pos] = 'a';
        }
        return array;
    }

    private String intArrayToString(int[] array) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            stringBuilder.append((char)(array[i]));
        }
        return stringBuilder.toString();
    }
}
