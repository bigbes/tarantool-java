package org.tarantool.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;

import org.tarantool.core.exception.CommunicationException;
import org.tarantool.core.exception.TarantoolException;

public class ByteChannelTransport implements Transport {
	ByteChannel channel;
	static final int HEADER_SIZE = 12;

	@Override
	public Response execute(Request request) {
		write(request);
		return read();
	}
	
	

	public ByteChannelTransport(ByteChannel channel) {
		super();
		this.channel = channel;
	}



	protected Response read() {
		ByteBuffer headers = read(HEADER_SIZE);
		Response response = new Response(headers.getInt(), headers.getInt(), headers.getInt());
		if (response.getSize() > 0) {
			ByteBuffer body = read(response.size);
			response.setRet(body.getInt());
			if (response.getRet() != 0) {
				handleErrorMessage(response, body);
			}
			if (body.remaining() > 4) {
				byte[] answer = new byte[body.remaining()];
				body.get(answer);
				response.setBody(answer);
			} else {
				response.setCount(body.getInt());
			}
		}
		return response;
	}

	protected void handleErrorMessage(Response response, ByteBuffer body) {
		byte[] message = new byte[body.capacity() - 4];
		body.get(message);
		throw new TarantoolException(response.getRet(), new String(message));
	}

	protected ByteBuffer read(int size) {
		ByteBuffer buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
		int res = 0;
		try {
			while (buffer.hasRemaining() && (res = channel.read(buffer)) > -1) {
			}
		} catch (IOException e) {
			throw new CommunicationException("Can't read data", e);
		}
		if (res == -1) {
			throw new CommunicationException("Connection lost");
		}
		buffer.flip();
		return buffer;
	}

	protected void write(Request request) {
		ByteBuffer recvBuffer = request.pack();
		while (recvBuffer.hasRemaining()) {
			try {
				channel.write(recvBuffer);
			} catch (IOException e) {
				throw new CommunicationException("Can't write packet to channel", e);
			}
		}
	}

	@Override
	public void close() throws IOException {
		if (channel.isOpen()) {
			channel.close();
		}
	}

}