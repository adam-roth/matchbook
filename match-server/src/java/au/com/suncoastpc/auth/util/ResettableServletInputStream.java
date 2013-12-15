package au.com.suncoastpc.auth.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletInputStream;

import org.apache.log4j.lf5.util.StreamUtils;

public class ResettableServletInputStream extends ServletInputStream {
	private ByteArrayInputStream inputStream;
	
	public ResettableServletInputStream(ServletInputStream data) throws IOException {
		ByteArrayOutputStream temp = new ByteArrayOutputStream();
		StreamUtils.copy(data, temp);
		inputStream = new ByteArrayInputStream(temp.toByteArray());
	}
	
	public int available() throws IOException {
		return inputStream.available();
	}

	public void close() throws IOException {
		inputStream.close();
	}

	public void mark(int readlimit) {
		inputStream.mark(readlimit);
	}

	public boolean markSupported() {
		return inputStream.markSupported();
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return inputStream.read(b, off, len);
	}

	public int readLine(byte[] b, int off, int len) throws IOException {
		if (len <= 0) {
			return 0;
		}
		int count = 0, c;
		
		while ((c = read()) != -1) {
			b[off++] = (byte)c;
			count++;
			if (c == '\n' || count == len) {
				break;
			}
		}
		return count > 0 ? count : -1;
	}

	public void reset() throws IOException {
		inputStream.reset();
	}

	public long skip(long n) throws IOException {
		return inputStream.skip(n);
	}

	
	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		return inputStream.read();
	}
	
}
