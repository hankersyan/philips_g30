package io.hankers.mdi.philips_g30;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import io.hankers.mdi.mdi_utils.MDILog;
import io.hankers.mdi.philips_g30.Models.MessageBase;

public class DataListener extends Thread {
	ServerSocket _server;
	int _port = 8010;

	public DataListener(int port) throws UnknownHostException, IOException {
		_port = port > 0 ? port : 8010;
	}

	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				_server = new ServerSocket(_port);
				MDILog.d("listening on {}", _port);
				Socket socket = _server.accept();
				new Worker(socket).start();
			} catch (IOException e1) {
				MDILog.w(e1);

				try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {
					MDILog.w(e);
					break;
				}
			}
		}

		try {
			_server.close();
		} catch (Exception e) {
			MDILog.e(e);
		}
	}

	static class Worker extends Thread {
		Socket _client;
		byte[] _buf = new byte[2048];
		final static byte BYTE_START = (byte) 0xAA;
		final static byte BYTE_END = (byte) 0xCC;
		MessageBase _cachedMsg = null;

		public Worker(Socket clientSocket) {
			_client = clientSocket;
		}

		public void run() {
			InputStream ins = null;
			try {
				ins = _client.getInputStream();
			} catch (IOException e1) {
				MDILog.e(e1);
			}
			if (ins == null) {
				MDILog.e("Worker can NOT get stream!");
				return;
			}
			int offset = 0;
			while (!Thread.currentThread().isInterrupted()) {
				try {
					int readCount = ins.read(_buf, offset, _buf.length - offset);

					final int availableCount = offset + readCount;

					int unprocessed = cut(_buf, availableCount);
					if (unprocessed > 0 && availableCount > unprocessed) {
						int m = 0;
						for (int i = unprocessed; i < availableCount; i++) {
							_buf[m++] = _buf[unprocessed];
						}
						offset = availableCount - unprocessed;
					} else {
						offset = 0;
					}
				} catch (IOException e) {
					MDILog.e(e);
				} catch (Exception e) {
					MDILog.e(e);
				}
			}
			try {
				ins.close();
				_client.close();
			} catch (IOException e) {
				MDILog.e(e);
			}
		}

		private int cut(byte[] buf, int count) {
			int posStart = -1;
			int posEnd = -1;
			int lastEnd = -1;
			for (int i = 0; i < count && i < buf.length; i++) {
				switch (buf[i]) {
				case BYTE_START:
					posStart = i;
					break;
				case BYTE_END:
					posEnd = i;
					if (posStart >= 0 && posEnd >= 0) {
						lastEnd = i;
						process(buf, posStart + 1, posEnd - posStart - 1);
					}
					break;
				}
			}
			return lastEnd + 1;
		}


		private void process(byte[] buf, int offset, int length) {
			MessageBase newMsg = MessageBase.newInstance(buf, offset, length);

			if (_cachedMsg == null) {
				_cachedMsg = newMsg;
			} else if (_cachedMsg._timestamp == newMsg._timestamp) {
				_cachedMsg.copyFrom(newMsg);
			} else {
				// MDILog.d("publishing {}, {}", _cachedMsg, newMsg);
				_cachedMsg.publish();
				_cachedMsg = newMsg;
			}
		}

	}
}
