package info.kgeorgiy.ja.pulnikova.hello;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

public class Util {
    public static void setByte(DatagramPacket packet, byte[] bytes) {
        packet.setData(bytes);
    }

    public static byte[] stringForByte(String string){
        return string.getBytes(StandardCharsets.UTF_8);
    }

    public static String packetForString(DatagramPacket packet){
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }
}
