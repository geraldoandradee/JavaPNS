package javapns.notification;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for reading response packets from an APNS connection.
 * See Apple's documentation on enhanced notification format.
 *
 * @author Sylvain Pedneault
 */
class ResponsePacketReader {
    /* The number of seconds to wait for a response */
    private static final int TIMEOUT = 5 * 1000;

    private ResponsePacketReader() {
    }

    /**
     * Read response packets from the current APNS connection and process them.
     *
     * @param notificationManager
     * @return the number of response packets received and processed
     */
    public static int processResponses(final PushNotificationManager notificationManager) {
        final List<ResponsePacket> responses = readResponses(notificationManager.getActiveSocket());
        handleResponses(responses, notificationManager);
        return responses.size();
    }

    /**
     * Read raw response packets from the provided socket.
     * <p>
     * Note: this method automatically sets the socket's timeout
     * to TIMEOUT, so not to block the socket's input stream.
     *
     * @param socket
     * @return
     */
    private static List<ResponsePacket> readResponses(final Socket socket) {
        final List<ResponsePacket> responses = new ArrayList<>();
        int previousTimeout = 0;
        try {
      /* Set socket timeout to avoid getting stuck on read() */
            try {
                previousTimeout = socket.getSoTimeout();
                socket.setSoTimeout(TIMEOUT);
            } catch (final Exception e) {
                // empty
            }
            final InputStream input = socket.getInputStream();
            while (true) {
                final ResponsePacket packet = readResponsePacketData(input);
                if (packet != null) {
                    responses.add(packet);
                } else {
                    break;
                }
            }

        } catch (final Exception e) {
      /* Ignore exception, as we are expecting timeout exceptions because Apple might not reply anything */
        }
    /* Reset socket timeout, just in case */
        try {
            socket.setSoTimeout(previousTimeout);
        } catch (final Exception e) {
            // empty
        }
        return responses;
    }

    private static void handleResponses(final List<ResponsePacket> responses, final PushNotificationManager notificationManager) {
        for (final ResponsePacket response : responses) {
            response.linkToPushedNotification(notificationManager);
        }
    }

    private static ResponsePacket readResponsePacketData(final InputStream input) throws IOException {
        final int command = input.read();
        if (command < 0) {
            return null;
        }
        final int status = input.read();
        if (status < 0) {
            return null;
        }

        final int identifierByte1 = input.read();
        if (identifierByte1 < 0) {
            return null;
        }
        final int identifierByte2 = input.read();
        if (identifierByte2 < 0) {
            return null;
        }
        final int identifierByte3 = input.read();
        if (identifierByte3 < 0) {
            return null;
        }
        final int identifierByte4 = input.read();
        if (identifierByte4 < 0) {
            return null;
        }
        final int identifier = (identifierByte1 << 24) + (identifierByte2 << 16) + (identifierByte3 << 8) + (identifierByte4);
        return new ResponsePacket(command, status, identifier);
    }
}
