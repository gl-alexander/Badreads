package bg.sofia.uni.fmi.mjt.goodreads;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import bg.sofia.uni.fmi.mjt.goodreads.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.goodreads.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.goodreads.user.Session;

public class Server {
    private static final String HOST = "localhost";
    private static final String KILL_COMMAND = "killcommand";

    private final CommandExecutor commandExecutor;

    private final int port;
    private boolean isServerWorking;

    private Selector selector;
    private final Map<SocketChannel, Session> sessionMap;

    public Server(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;
        sessionMap = new HashMap<>();
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            isServerWorking = true;
            System.out.println("Server started on port " + port);
            while (isServerWorking) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }
                    handleClients(selector);
                } catch (IOException e) {
                    System.out.println("Error occurred while processing client request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("failed to start server", e);
        }
    }

    public void stop() {
        this.isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void handleClients(Selector selector) throws IOException {
        Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if (key.isReadable()) {
                SocketChannel clientChannel = (SocketChannel) key.channel();
                String clientInput = getClientInput(clientChannel);
                System.out.println(clientInput);
                if (clientInput == null) {
                    continue;
                }
                if (clientInput.equals(KILL_COMMAND)) {
                    stop();
                }
                Session userSession = sessionMap.get(clientChannel);

                String output = commandExecutor.execute(CommandCreator.newCommand(clientInput), userSession);
                writeClientOutput(clientChannel, output);
            } else if (key.isAcceptable()) {
                accept(selector, key);
            }

            keyIterator.remove();
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, this.port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        ByteBuffer buffer = sessionMap.get(clientChannel).getBuffer();
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        ByteBuffer buffer = sessionMap.get(clientChannel).getBuffer();
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        clientChannel.write(buffer);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        sessionMap.put(accept, new Session());
        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

}