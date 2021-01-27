package OneAIO;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AioTcpServer implements Runnable {
	private AsynchronousChannelGroup asyncChannelGroup;
	private AsynchronousServerSocketChannel listener;

	public AioTcpServer(int port) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(5);
		asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(executor);
		listener = AsynchronousServerSocketChannel.open(asyncChannelGroup).bind(new InetSocketAddress(port));
	}

	@Override
	public void run() {
		try {

			AioAcceptHandler acceptHandler = new AioAcceptHandler();
			listener.accept(listener, acceptHandler);
			Thread.sleep(400000);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("finished server");
		}
	}

}