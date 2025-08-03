import javax.sound.sampled.*;
import java.net.*;
import java.util.function.BooleanSupplier;

public class VoiceCall {
    public static void startSend(String remoteIP, int remotePort, BooleanSupplier running) throws Exception {
        AudioFormat format = new AudioFormat(8000f, 16, 1, true, false);
        TargetDataLine mic = AudioSystem.getTargetDataLine(format);
        mic.open(format);
        mic.start();

        DatagramSocket socket = new DatagramSocket();
        InetAddress addr = InetAddress.getByName(remoteIP);
        byte[] buffer = new byte[1024];

        try {
            while (running.getAsBoolean()) {
                int count = mic.read(buffer, 0, buffer.length);
                if (count > 0) {
                    DatagramPacket packet = new DatagramPacket(buffer, count, addr, remotePort);
                    socket.send(packet);
                }
            }
        } finally {
            mic.close();
            socket.close();
        }
    }

    public static void startReceive(int localPort, BooleanSupplier running) throws Exception {
        AudioFormat format = new AudioFormat(8000f, 16, 1, true, false);
        SourceDataLine speakers = AudioSystem.getSourceDataLine(format);
        speakers.open(format);
        speakers.start();

        DatagramSocket socket = new DatagramSocket(localPort);
        byte[] buffer = new byte[1024];

        try {
            while (running.getAsBoolean()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                speakers.write(packet.getData(), 0, packet.getLength());
            }
        } finally {
            speakers.close();
            socket.close();
        }
    }
}
