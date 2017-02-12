/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.lidinsky.signalserver;

import cz.lidinsky.spinel.RawBinarySpinelMessage;
import cz.lidinsky.tools.Tools;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a distributed map storage.
 */
public abstract class DistributedMap implements Closeable {

  /**
   * Default port which is used for create communication between the serve
   * and the client side.
   */
  public static final int DEFAULT_PORT = 12349;

  public static final Logger logger
          = Logger.getLogger(DistributedMap.class.getName());

  /**
   * Associate given value with the given key;
   *
   * @param key
   * @param value
   * @throws java.io.IOException
   */
  public abstract void put(String key, byte[] value) throws IOException;

  /**
   * Returns value associated with the given key.
   *
   * @param key
   * @return
   * @throws java.io.IOException
   */
  public abstract byte[] get(String key) throws IOException;

  /**
   * Returns instance of the distributed map.
   *
   * @return
   */
  public static DistributedMap getInstance() {
    return getInstance("locathost", DEFAULT_PORT);
  }

  /**
   * Returns instance of the distributed map object.
   *
   * @param host
   * @param port
   * @return
   */
  public static DistributedMap getInstance(final String host, final int port) {
    try {
      // first of all, try to create server
      Server server = new Server(port);
      server.start();
      return server;
    } catch (IOException e) {
      // if it fails, create a client
      return new Client("localhost", DEFAULT_PORT);
    }
  }

  /**
   *
   * @param args the command line arguments
   * @throws java.lang.Exception
   */
  public static void main(String[] args) throws Exception {
    try {
      Server server = new Server(DEFAULT_PORT);
      server.start();
      server.serverThread.join();
    } catch (IOException e) {
      logger.info("Server is probably running");
      Client client = new Client("localhost", DEFAULT_PORT);
      client.open();
      client.put("abcd", new byte[] {1, 2, 3});
      logger.info("put message sent...");
      byte[] buffer = client.get("abcd");
      logger.info("get message sent...");
      System.out.println(Arrays.toString(buffer));

      Tools.sleep(1000);
      client.close();
    }
  }


  //----------------------------------------------- Server side implementation.

  /**
   * Server side implementation of the distributed map.
   *
   * The server accepts following messages:
   * o put - puts message into the internal map
   * o get - return message that is associated with given key
   * o status - return status information
   * o exit - exits
   *
   */
  private static class Server extends DistributedMap {

    /**
     * The storage for the data.
     */
    private final Map<String, byte[]> map;

    /**
     * Port number, the server is waiting for new clients.
     */
    private final int port;

    /**
     * If true, it is a signal for the server to stop.
     */
    private boolean stop;

    private int clientCounter;

    /**
     * A set of all of the active clients;
     */
    private final Set<ServerClient> clients;

    private boolean running;

    private ServerSocket serverSocket;

    private Thread serverThread;

    /**
     * Initialize the instance, but the server is not started.
     *
     * @param port
     *            port number where the server is
     */
    public Server(int port) {
      this.port = port;
      this.map = new HashMap<>();
      this.clients = new HashSet<>();
      this.stop = false;
      this.running = false;
    }

    public void start() throws IOException {

      serverSocket = new ServerSocket(port);
      logger.info("Server socket has been successfuly created, going to enter the accept loop...");

      serverThread = new Thread(() -> {

        logger.info("Server thread has been executed...");
        try {
          while (!stop) {
            running = true;
            Socket socket = serverSocket.accept();
            logger.info("New client has been accepted...");
            ServerClient client = new ServerClient(socket, clientCounter++);
            new Thread(client).start();
            add(client);
          }
        } catch (IOException e) {
          logger.severe(e.getMessage());
        } finally {
          running = false;
          Tools.close(serverSocket);
          serverSocket = null;
          logger.info("Leaving the accept loop...");
        }

      });
      serverThread.setDaemon(true);
      serverThread.start();
    }

    private synchronized void add(ServerClient client) {
      clients.add(client);
    }

    private synchronized void remove(ServerClient client) {
      clients.remove(client);
    }

    @Override
    public synchronized void put(String key, byte[] value) {
      if (running) {
        map.put(key, value);
      } else {
        throw new IllegalStateException();
      }
    }

    @Override
    public synchronized byte[] get(String key) {
      if (running) {
        return map.get(key);
      } else {
        throw new IllegalStateException();
      }
    }

    @Override
    public void close() {
      this.stop = true;
    }

    //---------------------------------------------- Client class implementation.

    private class ServerClient implements Runnable {

      private final Socket socket;

      private final int clientNumber;

      /**
       *
       * @param socket
       * @param clientNumber
       *            a number which is used in messages for identification purposes
       */
      public ServerClient(Socket socket, int clientNumber) {
        this.socket = socket;
        this.clientNumber = clientNumber;
      }

      @Override
      public void run() {
        logger.info(String.format("Client #%d has been started...", clientNumber));
        try (
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                ) {
          logger.info(String.format("Client #%d: Going to enter request-response loop...", clientNumber));
          while (!stop) {
            RawBinarySpinelMessage message = RawBinarySpinelMessage.read(is);
            logger.fine("Message received");
            System.out.println(message.toString());
            int command = message.getData()[0];
            byte[] buffer = MessageUtils.unwrapCommand(message.getData());
            switch (command) {
              case MessageUtils.GET_INST:
                String key = MessageUtils.getKey(buffer);
                System.out.println(key);
                buffer = get(key);
                if (buffer == null) {
                  MessageUtils.NO_VALUE_RESPONSE.send(os);
                } else {
                  buffer = MessageUtils.wrap(MessageUtils.ACK_OK, buffer);
                  (new RawBinarySpinelMessage(0xB0, buffer)).send(os);
                }
                break;
              case MessageUtils.PUT_INST:
                key = MessageUtils.getKey(buffer);
                buffer = MessageUtils.unwrapKey(buffer);
                System.out.println(key);
                put(key, buffer);
                MessageUtils.OK_RESPONSE.send(os);
                break;
              default:
                logger.warning("Unknown command!");
                MessageUtils.BAD_COMMAND_RESPONSE.send(os);
                break;
            }
          }
        } catch (EOFException ex) {
          // just end of file, do nothing
        } catch (IOException ex) {
          logger.log(Level.SEVERE, null, ex);
        } finally {
          logger.info("Client going down...");
        }

      }

    }
  }

  //---------------------------------------------------- Client implementation.

  /**
   *
   */
  private static class Client extends DistributedMap {

    private final String host;
    private final int port;
    private InputStream inputStream;
    private OutputStream outputStream;

    public Client(String host, int port) {
      this.host = host;
      this.port = port;
    }

    public void open() throws IOException {
      Socket socket = new Socket(host, port);
      inputStream = socket.getInputStream();
      outputStream = socket.getOutputStream();
    }

    @Override
    public void put(String key, byte[] value) throws IOException {
      // wrap together with the key
      byte[] buffer = MessageUtils.wrap(key, value);
      // wrap together with the instruction
      buffer = MessageUtils.wrap(MessageUtils.PUT_INST, buffer);
      // wrap into the spinel message
      RawBinarySpinelMessage message = new RawBinarySpinelMessage(0xB0, buffer);
      // send it
      message.send(outputStream);
      // wait for response
      message = RawBinarySpinelMessage.read(inputStream);
      // if something wrong, throw an exception
      if (message.getData()[0] != MessageUtils.ACK_OK) {
        throw new IOException();
      }
    }

    @Override
    public byte[] get(String key) throws IOException {
      // wrap the key
      byte[] buffer = MessageUtils.wrap(key, new byte[] {});
      // wrap together with the instruction
      buffer = MessageUtils.wrap(MessageUtils.GET_INST, buffer);
      // wrap into the spinel message
      RawBinarySpinelMessage message = new RawBinarySpinelMessage(0xB0, buffer);
      // send it
      message.send(outputStream);
      // wait for response
      message = RawBinarySpinelMessage.read(inputStream);
      buffer = message.getData();
      // if something went wrong, throw an exception
      if (buffer[0] != MessageUtils.ACK_OK) {
        throw new IOException(Byte.valueOf(buffer[0]).toString());
      }
      // unwrap and return
      return MessageUtils.unwrapCommand(buffer);
    }

    @Override
    public void close() throws IOException {
      Tools.close(inputStream);
      Tools.close(outputStream);
    }

  }
}
