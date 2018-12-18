package hu.skawa.distributingoutputstream;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class DistributingOutputStreamTest {

	@Test
	void testAddReceiverAfterWriteable() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			DistributingOutputStream dst = new DistributingOutputStream();
			dst.setWriteable(true);
			dst.addReceiver(fileOutputStream, true);
		});
	}

	@Test
	void testAddReceiverExpected() throws FileNotFoundException {
		DistributingOutputStream dst = new DistributingOutputStream();
		OutputStream test = OutputStream.nullOutputStream();
		dst.addReceiver(test, true);

		Assertions.assertTrue(dst.receivers.containsKey(test));
		Assertions.assertTrue(dst.receivers.get(test));
	}

	@Test
	void testAddReceiverWithDefaultExpected() throws FileNotFoundException {
		DistributingOutputStream dst = new DistributingOutputStream();
		OutputStream test = OutputStream.nullOutputStream();
		dst.addReceiver(test);

		Assertions.assertTrue(dst.receivers.containsKey(test));
		Assertions.assertTrue(dst.receivers.get(test));
	}

	@Test
	void testDistribution() throws IOException {
		FileOutputStream mockFos = EasyMock.mock(FileOutputStream.class);
		ByteArrayOutputStream mockBaos = EasyMock.mock(ByteArrayOutputStream.class);

		mockFos.write(EasyMock.anyInt());
		EasyMock.expectLastCall().once();
		mockBaos.write(EasyMock.anyInt());
		EasyMock.expectLastCall().once();

		DistributingOutputStream dst = new DistributingOutputStream();
		dst.addReceiver(mockBaos, true);
		dst.addReceiver(mockFos, true);
		dst.setWriteable(true);

		EasyMock.replay(mockFos, mockBaos);

		dst.write(128);

		EasyMock.verify(mockFos, mockBaos);
	}

	@Test
	void testDistributionBeforeWriteable() throws IOException {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			FileOutputStream mockFos = EasyMock.mock(FileOutputStream.class);
			ByteArrayOutputStream mockBaos = EasyMock.mock(ByteArrayOutputStream.class);

			mockFos.write(EasyMock.anyInt());
			EasyMock.expectLastCall().once();
			mockBaos.write(EasyMock.anyInt());
			EasyMock.expectLastCall().once();

			DistributingOutputStream dst = new DistributingOutputStream();
			dst.addReceiver(mockBaos, true);
			dst.addReceiver(mockFos, true);

			EasyMock.replay(mockFos, mockBaos);

			dst.write(128);

			EasyMock.verify(mockFos, mockBaos);
		});
	}

	@Test
	void testDistributionSurivingStream() throws IOException {
		FileOutputStream mockFos = EasyMock.mock(FileOutputStream.class);
		ByteArrayOutputStream mockBaos = EasyMock.mock(ByteArrayOutputStream.class);

		mockFos.write(EasyMock.anyInt());
		EasyMock.expectLastCall().times(2);
		mockBaos.write(EasyMock.anyInt());
		EasyMock.expectLastCall().once();
		mockBaos.close();
		EasyMock.expectLastCall().once();

		DistributingOutputStream dst = new DistributingOutputStream();
		dst.addReceiver(mockBaos, true);
		dst.addReceiver(mockFos, false);
		dst.setWriteable(true);

		EasyMock.replay(mockFos, mockBaos);

		dst.write(128);
		dst.close();

		mockFos.write(128);

		EasyMock.verify(mockFos, mockBaos);
	}

	@Test
	void testDistributionSurivingStreamInverse() throws IOException {
		ByteArrayOutputStream mockBaos = EasyMock.mock(ByteArrayOutputStream.class);
		OutputStream mockFos = OutputStream.nullOutputStream();

		mockBaos.write(EasyMock.anyInt());
		EasyMock.expectLastCall().once();
		mockBaos.close();
		EasyMock.expectLastCall().once();

		DistributingOutputStream dst = new DistributingOutputStream();
		dst.addReceiver(mockBaos, true);
		dst.addReceiver(mockFos, true);
		dst.setWriteable(true);

		EasyMock.replay(mockBaos);

		dst.write(128);
		dst.close();

		Assertions.assertThrows(IOException.class, () -> mockFos.write(128));

		EasyMock.verify(mockBaos);
	}

	private final FileOutputStream fileOutputStream = EasyMock.mock(FileOutputStream.class);
}