package OneAIO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AioAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {
	public ConvertCode convertCode;

	public void cancelled(AsynchronousServerSocketChannel attachment) {
		System.out.println("cancelled");
	}

	public AioAcceptHandler() {
		// TODO Auto-generated constructor stub
		convertCode = new ConvertCode();
	}

	@Override
	public void completed(AsynchronousSocketChannel socket, AsynchronousServerSocketChannel attachment) {
		try {
			System.out.println("AioAcceptHandler.completed called");
			attachment.accept(attachment, this);
			System.out.println("有客户端连接:" + socket.getRemoteAddress().toString());
			startRead(socket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
		exc.printStackTrace();
	}

	public void startRead(AsynchronousSocketChannel socket) {
		ByteBuffer clientBuffer = ByteBuffer.allocate(4096);
		socket.read(clientBuffer, clientBuffer, new AioReadHandler(socket));
		try {

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
