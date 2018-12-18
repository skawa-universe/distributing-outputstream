package hu.skawa.distributingoutputstream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to create a <i>Distributing OutputStream</i>. <br>
 * Maintains an arbitrary number of {@link OutputStream} implementations, and forwards all writes synchronously to all streams.
 * <p>
 * Streams are, by default, closed with the distributor, unless they are explicitly marked as survivable, which exempts them from being closed. This allows a subset of streams to be used later on, once main distribution is finished.
 */
public class DistributingOutputStream extends OutputStream {
	/**
	 * Adds a target output stream to the distributor. Can only be called if the distributor is not yet marked as writeable!
	 *
	 * @param receiver             The output stream to be added. The added stream will receive all writes once the distributor is finalized, and will be closed along with the distributor unless marked as survivable.
	 * @param closeWithDistributor If set to {@code false}, the stream will survive closing the distributor. This enables the contents of the stream to be used later on.
	 *
	 * @throws IllegalStateException if the distributor is marked as {@code writeable}, as adding a new stream is unsafe.
	 */
	public void addReceiver(OutputStream receiver, boolean closeWithDistributor) throws IllegalStateException {
		if (!writeable) {
			receivers.put(receiver, closeWithDistributor);
		} else {
			throw new IllegalStateException("DistributingOutputStream cannot accept new receivers once finalized");
		}
	}

	/**
	 * Adds a target stream to the distributor that will be closed along with it.
	 *
	 * @param receiver The output stream to be added.
	 *
	 * @throws IllegalStateException if the distributor is marked as {@code writeable}, as adding a new stream is unsafe.
	 * @see DistributingOutputStream#addReceiver(OutputStream, boolean)
	 */
	public void addReceiver(OutputStream receiver) throws IllegalStateException {
		addReceiver(receiver, true);
	}

	/**
	 * Marks the distributor as writeable. When this is set, further streams may not be added as the distributor may have already been written to.
	 *
	 * @param writeable Whether the distributor may be written to. Setting this to {@code true} enables writing, but forbids adding further streams to the distributomvn r.
	 */
	public void setWriteable(boolean writeable) {
		this.writeable = writeable;
	}

	/**
	 * Writes the specified byte to this output stream. The general
	 * contract for <code>write</code> is that one byte is written
	 * to the output stream. The byte to be written is the eight
	 * low-order bits of the argument <code>b</code>. The 24
	 * high-order bits of <code>b</code> are ignored.
	 * <p>
	 * Subclasses of <code>OutputStream</code> must provide an
	 * implementation for this method.
	 *
	 * @param b the <code>byte</code>.
	 *
	 * @throws IOException           if an I/O error occurs. In particular, an <code>IOException</code> may be thrown if the output stream has been closed.
	 * @throws IllegalStateException if the distributor is marked as {@code writeable}, as adding a new stream is unsafe.
	 */
	@Override
	public synchronized void write(int b) throws IOException, IllegalStateException {
		if (writeable) {
			for (OutputStream os : receivers.keySet()) {
				os.write(b);
			}
		} else {
			throw new IllegalStateException("DistributingOutputStream cannot be written to before receivers finalized");
		}
	}

	/**
	 * @param b the data
	 *
	 * @throws IOException           if an I/O error occurs. In particular, an <code>IOException</code> may be thrown if the output stream has been closed.
	 * @throws IllegalStateException if the distributor is marked as {@code writeable}, as adding a new stream is unsafe.
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public synchronized void write(byte[] b) throws IOException, IllegalStateException {
		if (writeable) {
			for (OutputStream os : receivers.keySet()) {
				os.write(b);
			}
		} else {
			throw new IllegalStateException("DistributingOutputStream cannot be written to before receivers finalized");
		}
	}

	/**
	 * @param b   the data.
	 * @param off the start offset in the data.
	 * @param len the number of bytes to write.
	 *
	 * @throws IOException           if an I/O error occurs. In particular,
	 *                               an <code>IOException</code> is thrown if the output
	 *                               stream is closed.
	 * @throws IllegalStateException if the distributor is marked as {@code writeable}, as adding a new stream is unsafe.
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException, IllegalStateException {
		if (writeable) {
			for (OutputStream os : receivers.keySet()) {
				os.write(b, off, len);
			}
		} else {
			throw new IllegalStateException("DistributingOutputStream cannot be written to before receivers finalized");
		}
	}

	/**
	 * @throws IOException           if an I/O error occurs. In particular,
	 *                               an <code>IOException</code> is thrown if the output
	 *                               stream is closed.
	 * @throws IllegalStateException if the distributor is marked as {@code writeable}, as adding a new stream is unsafe.
	 * @see OutputStream#flush()
	 */
	@Override
	public synchronized void flush() throws IOException, IllegalStateException {
		if (writeable) {
			for (OutputStream os : receivers.keySet()) {
				os.flush();
			}
		} else {
			throw new IllegalStateException("DistributingOutputStream cannot flush streams before being finalized");
		}
	}

	/**
	 * Closes the distributor. All streams that were added with {@code closeWithDistributor} set to {@code true} will be closed along with it. This action also sets the {@link DistributingOutputStream#writeable} property to {@code false}
	 * , so further writes are not permitted.
	 *
	 * @throws IOException           if an I/O error occurs. In particular,
	 *                               an <code>IOException</code> is thrown if the output
	 *                               stream is closed.
	 * @throws IllegalStateException if the distributor is marked as {@code writeable}, as adding a new stream is unsafe.
	 * @see OutputStream#close()
	 */
	@Override
	public synchronized void close() throws IOException, IllegalStateException {
		if (writeable) {
			for (OutputStream os : receivers.keySet()) {
				if (receivers.get(os)) {
					os.close();
				}
			}
			writeable = false;
		} else {
			throw new IllegalStateException("DistributingOutputStream has been closed and must not be written to");
		}
	}

	/**
	 * Designates the distributor as writeable. When this is {@code false}, new streams may be added, but the distributor may not be written to. When {@code true}, the distributor can be written to and will forward writes to the
	 * contained streams, but no new streams may be added (attempting to do so will throw an @link {@link IllegalStateException}).
	 */
	private boolean writeable = false;

	Map<OutputStream, Boolean> receivers = new HashMap<OutputStream, Boolean>();

}
